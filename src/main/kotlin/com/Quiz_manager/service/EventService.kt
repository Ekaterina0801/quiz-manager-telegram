package com.Quiz_manager.service

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Registration
import com.Quiz_manager.dto.RegistrationData
import com.Quiz_manager.dto.request.EventCreationDto
import com.Quiz_manager.dto.response.EventResponseDto
import com.Quiz_manager.enums.Role
import com.Quiz_manager.mapper.toResponseDto
import com.Quiz_manager.repository.*
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.format.DateTimeFormatter

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val registrationRepository: RegistrationRepository,
    private val userService: UserService,
    private val cloudinaryService: CloudinaryService,
    private val teamNotificationSettingsRepository: TeamNotificationSettingsRepository,
    private val chatId: String="",
    private val teamRepository: TeamRepository, private val whatsAppNotificationService: WhatsAppNotificationService,
    private val telegramService: TelegramService
) {

    private val logger = LoggerFactory.getLogger(EventService::class.java)

    fun getAllEvents(): List<EventResponseDto> = eventRepository.findAll().map { x->x.toResponseDto() }

    fun getEventById(eventId: Long): EventResponseDto? =
        eventRepository.findById(eventId).orElse(null)?.toResponseDto()

    fun getEventsByTeam(
        teamId: Long,
        pageable: Pageable,
        search: String?
    ): Page<EventResponseDto> {
        val page = if (search.isNullOrBlank()) {
            eventRepository.findByTeamId(teamId, pageable)
        } else {
            eventRepository.searchByNameIgnoreCaseNative(teamId, search, pageable)
        }
        return page.map { it.toResponseDto() }
    }


    @Transactional
    fun createEvent(eventData: EventCreationDto): Event {
        userService.isUserModerator(eventData.userId, eventData.teamId)
        val imageUrl = uploadImageIfPresent(eventData.imageFile)
        val team = teamRepository.findById(eventData.teamId).orElse(null)
        val event = Event(
            id = null,
            name = eventData.name,
            description = eventData.description,
            dateTime = eventData.dateTime,
            posterUrl = imageUrl,
            linkToAlbum = null,
            teamResult = null,
            location = eventData.location,
            team = team,
            isRegistrationOpen = true,
            isHidden = false,
            price = eventData.price
        )

        return eventRepository.save(event)
    }

    @Transactional
    fun registerForEvent(eventId: Long, registrationData: RegistrationData): Registration {
        val event = eventRepository.findById(eventId).orElseThrow { Exception("Event not found") }
        val registrant = userService.getUserById(registrationData.userId)

        if (isUserAlreadyRegistered(eventId, registrationData.fullName)) {
            throw Exception("You are already registered for this event")
        }

        val registration = Registration(event = event, registrant = registrant, fullName = registrationData.fullName)
        registrationRepository.save(registration)

        dispatchNotification(event, registration.fullName, "registration")
        //sendEventNotification(event, registrationData.fullName, "registration")
        return registration
    }

    @Transactional
    fun unregisterFromEvent(eventId: Long, registrationId: Long, userId: Long): String {
        val event = eventRepository.findById(eventId)
            .orElseThrow { IllegalArgumentException("Event with id=$eventId not found") }

        val registration = registrationRepository.findById(registrationId)
            .orElseThrow { IllegalArgumentException("Registration with id=$registrationId not found") }

        val user = userService.getUserById(userId)

        val isModerator = userService.isUserModerator(user.id, event.team.id)
        val isAdmin     = user.role == Role.ADMIN
        val isOwner     = registration.registrant.id == userId

        if (!(isOwner || isModerator || isAdmin)) {
            throw AccessDeniedException("Access denied: you are not allowed to unregister this participant")
        }

        event.registrations.remove(registration)
        eventRepository.save(event)
        registrationRepository.delete(registration)

        dispatchNotification(event, registration.fullName, "unregistration")
        return "Unregistration successful"
    }

    private fun uploadImageIfPresent(imageFile: MultipartFile?): String? {
        return imageFile?.let {
            val tempFile = File(System.getProperty("java.io.tmpdir") + "/" + it.originalFilename)
            it.transferTo(tempFile)
            cloudinaryService.uploadImage(tempFile)
        }
    }

    private fun isUserAlreadyRegistered(eventId: Long, fullName: String): Boolean {
        var p = registrationRepository.findByEventIdAndFullName(eventId, fullName);
        return registrationRepository.findByEventIdAndFullName(eventId, fullName) != null
    }

    @Transactional
    fun deleteEvent(eventId: Long, userId: Long) {
        val event = eventRepository.findById(eventId).orElseThrow { Exception("Event not found") }
        userService.isUserModerator(userId, event.team.id)
        eventRepository.delete(event)
    }

    @Transactional
    fun updateEvent(eventId: Long, updatedEventData: EventCreationDto): Event {
        val event = eventRepository.findById(eventId).orElseThrow { Exception("Event not found") }
        userService.isUserModerator(updatedEventData.userId, event.team.id)

        val imageUrl = uploadImageIfPresent(updatedEventData.imageFile) ?: event.posterUrl

        val updatedEvent = event.copy(
            name = updatedEventData.name,
            description = updatedEventData.description,
            dateTime = updatedEventData.dateTime,
            posterUrl = imageUrl,
            linkToAlbum = updatedEventData.linkToAlbum,
            teamResult = updatedEventData.teamResult,
            location = updatedEventData.location,
            isRegistrationOpen = updatedEventData.isRegistrationOpen,
            price = updatedEventData.price

        )

        return eventRepository.save(updatedEvent)
    }

    @Transactional
    private fun dispatchNotification(
        event: Event,
        participantFullName: String? = null,
        notificationType: String
    ) {
        val settings = teamNotificationSettingsRepository.findByTeamId(event.team.id) ?: return
        val groupId = event.team.chatId ?: return

        when (notificationType) {
            "registration"   -> if (settings.registrationNotificationEnabled)
                sendRegistrationNotification(event, participantFullName!!, groupId, true)
            "unregistration" -> if (settings.unregisterNotificationEnabled)
                sendRegistrationNotification(event, participantFullName!!, groupId, false)
            "summary"        -> sendEventSummary(event, groupId)
        }
    }

    fun sendRegistrationNotification(
        event: Event,
        participantFullName: String,
        groupId: String,
        isRegistration: Boolean
    ) {
        val emoji = if (isRegistration) "✅" else "❌"
        val action = if (isRegistration) "Регистрация" else "Отмена регистрации"
        val formattedDate = event.dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))

        val text = buildString {
            append("$emoji *$action на мероприятие!* \n\n")
            append("📌 *Мероприятие:* ${event.name}\n")
            append("👤 *Участник:* $participantFullName\n")
            append("📅 *Когда:* $formattedDate\n")
            append("📍 *Где:* ${event.location}\n")
            event.linkToAlbum?.let { append("🖼️ *Альбом:* $it\n") }
        }

        telegramService.sendMessageToChat(groupId, text)
    }

    fun sendEventSummary(event: Event, groupId: String) {
        val formattedDate = event.dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        val participants = event.registrations
            .joinToString("\n") { "- ${it.fullName}" }
            .ifBlank { "— нет зарегистрированных участников" }

        val text = buildString {
            append("📌 Что: ${event.name}\n")
            append("📍 Где: ${event.location}\n\n")
            append("📅 Когда: $formattedDate\n")
            append("\uD83E\uDD11 Стоимость: ${event.price} руб. \n")
            append("👥 Участники:\n$participants\n")
        }

        telegramService.sendMessageToChat(groupId, text)
    }

}
