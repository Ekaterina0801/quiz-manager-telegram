package com.Quiz_manager.service

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Registration
import com.Quiz_manager.dto.RegistrationData
import com.Quiz_manager.dto.request.EventCreationDto
import com.Quiz_manager.dto.response.EventResponseDto
import com.Quiz_manager.enums.Role
import com.Quiz_manager.mapper.EventMapper
import com.Quiz_manager.repository.*
import com.Quiz_manager.utils.uploadImageIfPresent
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
@Service
class EventService(
    private val eventRepository: EventRepository,
    private val registrationRepository: RegistrationRepository,
    private val userService: UserService,
    private val cloudinaryService: CloudinaryService,
    private val teamNotificationSettingsRepository: TeamNotificationSettingsRepository,
    private val teamRepository: TeamRepository,
    private val telegramService: TelegramService,
    private val eventMapper: EventMapper
) {
    private val logger = LoggerFactory.getLogger(EventService::class.java)

    fun getAllEvents(): List<EventResponseDto> =
        eventRepository.findAll().map { eventMapper.toResponseDto(it) }

    fun getEventById(eventId: Long): EventResponseDto? =
        eventRepository.findById(eventId).orElse(null)?.let { eventMapper.toResponseDto(it) }

    fun getEventsByTeam(
        teamId: Long,
        pageable: Pageable,
        search: String?,
        currentUserId: Long
    ): Page<EventResponseDto> {
        val page = if (search.isNullOrBlank()) {
            eventRepository.findByTeamId(teamId, pageable)
        } else {
            val searchWithWildcards = "%${search.trim().lowercase()}%"

            eventRepository.searchByNameIgnoreCaseNative(teamId, searchWithWildcards, pageable)
            //eventRepository.searchByNameIgnoreCaseNative(teamId, search, pageable)
        }

        return page.map { event ->
            val dto = eventMapper.toResponseDto(event)
            dto.isRegistered = event.registrations.any { it.registrant.id == currentUserId }
            dto
        }
    }

    @Transactional
    fun createEvent(dto: EventCreationDto): Event {
        userService.isUserModerator(dto.userId!!, dto.teamId!!)

        val team = teamRepository.findById(dto.teamId!!).orElseThrow { Exception("Team not found") }
        val imageUrl = dto.imageFile.uploadImageIfPresent(cloudinaryService)

        val event = eventMapper.toEntity(dto, team)
        event.posterUrl = imageUrl

        return eventRepository.saveAndFlush(event)
    }


    @Transactional
    fun updateEvent(eventId: Long, dto: EventCreationDto): EventResponseDto {
        val existing = eventRepository.findById(eventId).orElseThrow { Exception("Event not found") }
        userService.isUserModerator(dto.userId!!, existing.team.id)

        val imageUrl = dto.imageFile.uploadImageIfPresent(cloudinaryService)

        eventMapper.updateEventFromDto(dto, existing, posterUrl = imageUrl)

        if (imageUrl != null) {
            existing.posterUrl = imageUrl
        }

        return eventMapper.toResponseDto(eventRepository.save(existing))
    }


    @Transactional
    fun deleteEvent(eventId: Long, userId: Long) {
        val event = eventRepository.findById(eventId).orElseThrow { Exception("Event not found") }
        userService.isUserModerator(userId, event.team.id)
        eventRepository.delete(event)
    }

    @Transactional
    fun registerForEvent(eventId: Long, data: RegistrationData): Registration {
        val event = eventRepository.findById(eventId).orElseThrow { Exception("Event not found") }
        val user = userService.getUserById(data.userId)

        if (registrationRepository.findByEventIdAndFullName(eventId, data.fullName) != null) {
            throw Exception("You are already registered for this event")
        }

        val registration = Registration(event = event, registrant = user, fullName = data.fullName)
        registrationRepository.save(registration)

        dispatchNotification(event, data.fullName, "registration")
        return registration
    }

    @Transactional
    fun unregisterFromEvent(eventId: Long, registrationId: Long, userId: Long): String {
        val event = eventRepository.findById(eventId)
            .orElseThrow { IllegalArgumentException("Event not found") }

        val registration = registrationRepository.findById(registrationId)
            .orElseThrow { IllegalArgumentException("Registration not found") }

        val user = userService.getUserById(userId)
        val isModerator = userService.isUserModerator(user.id, event.team.id)
        val isAdmin = user.role == Role.ADMIN
        val isOwner = registration.registrant.id == userId

        if (!(isOwner || isModerator || isAdmin)) {
            throw AccessDeniedException("Access denied")
        }

        event.registrations.remove(registration)
        eventRepository.save(event)
        registrationRepository.delete(registration)

        dispatchNotification(event, registration.fullName, "unregistration")
        return "Unregistration successful"
    }

    @Transactional
    private fun dispatchNotification(event: Event, participant: String?, type: String) {
        val settings = teamNotificationSettingsRepository.findByTeamId(event.team.id) ?: return
        val chatId = event.team.chatId ?: return

        when (type) {
            "registration" -> if (settings.registrationNotificationEnabled)
                sendRegistrationNotification(event, participant ?: "", chatId, true)

            "unregistration" -> if (settings.unregisterNotificationEnabled)
                sendRegistrationNotification(event, participant ?: "", chatId, false)

            "summary" -> sendEventSummary(event.id!!)
        }
    }

    fun sendRegistrationNotification(event: Event, participant: String, chatId: String, isRegistration: Boolean) {
        val emoji = if (isRegistration) "✅" else "❌"
        val action = if (isRegistration) "Регистрация" else "Отмена регистрации"
        val date = event.dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))

        val text = buildString {
            append("$emoji *$action на мероприятие!*\n\n")
            append("📌 *Мероприятие:* ${event.name}\n")
            append("👤 *Участник:* $participant\n")
            append("📅 *Когда:* $date\n")
            append("📍 *Где:* ${event.location}\n")
            event.linkToAlbum?.let { append("🖼️ *Альбом:* $it\n") }
        }

        telegramService.sendMessageToChat(chatId, text)
    }

    fun sendEventSummary(eventId: Long) {
        val event = eventRepository.findById(eventId).orElse(null) ?: return
        val chatId = event.team.chatId ?: return

        val formattedDate = formatEventDate(event.dateTime)

        val participantsList = formatParticipants(event.registrations)

        val summaryText = buildEventSummaryText(
            event,
            formattedDate,
            participantsList,
            event.limitOfRegistrations?.toInt()
        )

        telegramService.sendMessageToChat(chatId, summaryText)
    }

    private fun formatEventDate(dateTime: LocalDateTime): String =
        dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))


    private fun formatParticipants(registrations: List<Registration>): List<String> =
        if (registrations.isEmpty()) {
            listOf("— нет зарегистрированных участников")
        } else {
            registrations.map { it.fullName }
        }

    private fun buildEventSummaryText(
        event: Event,
        formattedDate: String,
        participants: List<String>,
        limit: Int? = null
    ): String {
        return buildString {
            appendLine("📌 Что: ${event.name}")
            appendLine("📍 Где: ${event.location}")
            appendLine()
            appendLine("📅 Когда: $formattedDate")
            appendLine("💰 Стоимость: ${event.price} ₽")
            appendLine("👥 Участники:")
            appendLine()

            val mainList = if (limit != null) participants.take(limit) else participants
            val reserveList = if (limit != null) participants.drop(limit) else emptyList()


            mainList.forEachIndexed { index, name ->
                appendLine("${index + 1}. $name")
            }

            if (reserveList.isNotEmpty()) {
                appendLine()
                appendLine("🔄 Резерв:")
                appendLine()
                reserveList.forEachIndexed { idx, name ->
                    appendLine("${mainList.size + idx + 1}. $name")
                }
            }
        }
    }

}
