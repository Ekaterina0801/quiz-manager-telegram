package com.Quiz_manager.service

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Registration
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.request.EventCreationDto
import com.Quiz_manager.dto.RegistrationData
import com.Quiz_manager.dto.response.EventResponseDto
import com.Quiz_manager.mapper.toEntity
import com.Quiz_manager.mapper.toResponseDto
import com.Quiz_manager.repository.*
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val registrationRepository: RegistrationRepository,
    private val userService: UserService,
    private val cloudinaryService: CloudinaryService,
    @Lazy
    private val telegramService: TelegramService,
    private val teamNotificationSettingsRepository: TeamNotificationSettingsRepository,
    @Value("\${telegram.chat_id}") private val chatId: String, private val teamRepository: TeamRepository
) {

    private val logger = LoggerFactory.getLogger(EventService::class.java)

    fun getAllEvents(): List<EventResponseDto> = eventRepository.findAll().map { x->x.toResponseDto() }

    fun getEventById(eventId: Long): EventResponseDto? =
        eventRepository.findById(eventId).orElse(null)?.toResponseDto()


    @Transactional
    fun createEvent(eventData: EventCreationDto): Event {
        checkAdminAccess(eventData.userId, eventData.teamId)
        val imageUrl = uploadImageIfPresent(eventData.imageFile)
        val team = teamRepository.findById(eventData.teamId).orElse(null)
        //val event = eventData.toEntity(team)
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
            isRegistrationOpen = true
        )

        return eventRepository.save(event)
    }

    @Transactional
    fun registerForEvent(eventId: Long, registrationData: RegistrationData): String {
        val event = eventRepository.findById(eventId).orElseThrow { Exception("Event not found") }
        val registrant = findOrCreateRegistrant(registrationData.telegramId)

        if (isUserAlreadyRegistered(eventId, registrationData.fullName)) {
            throw Exception("You are already registered for this event")
        }

        val registration = Registration(event = event, registrant = registrant, fullName = registrationData.fullName)
        registrationRepository.save(registration)

        sendEventNotification(event, registrationData.fullName, "registration")
        return "Registration successful"
    }

    @Transactional
    fun unregisterFromEvent(eventId: Long, registrationId: Long, telegramId: String): String {
        val event = eventRepository.findById(eventId).orElseThrow { Exception("Event not found") }
        val registration = registrationRepository.findById(registrationId).orElseThrow()
        val user = userRepository.findByTelegramId(telegramId)
            ?: throw Exception("User not found")

        if (registration.registrant.telegramId != telegramId &&
            !userService.isUserAdmin(user.id, event.team.id)
        ) {
            throw Exception("Access denied: you are not allowed to unregister this participant")
        }
        event.registrations.remove(registration)
        eventRepository.save(event)
        registrationRepository.delete(registration)
        sendEventNotification(event, registration.fullName, "unregistration")
        return "Unregistration successful"
    }

    private fun checkAdminAccess(userId: Long, teamId: Long) {
        if (!userService.isUserAdmin(userId, teamId)) {
            throw Exception("Access denied. Only admins can perform this action.")
        }
    }

    private fun uploadImageIfPresent(imageFile: MultipartFile?): String? {
        return imageFile?.let {
            val tempFile = File(System.getProperty("java.io.tmpdir") + "/" + it.originalFilename)
            it.transferTo(tempFile)
            cloudinaryService.uploadImage(tempFile)
        }
    }

    private fun isUserAlreadyRegistered(eventId: Long, fullName: String): Boolean {
        return registrationRepository.findByEventIdAndFullName(eventId, fullName) != null
    }

    private fun findOrCreateRegistrant(telegramId: String): User {
        return userRepository.findByTelegramId(telegramId) ?: telegramService.getUserInfo(telegramId)?.let {
            val newUser = User(
                id = 0,
                username = it.username ?: "unknown",
                firstName = it.firstName,
                lastName = it.lastName ?: "",
                telegramId = telegramId
            )
            userRepository.save(newUser)
        } ?: throw Exception("Failed to fetch user info from Telegram")
    }

    @Transactional
    fun deleteEvent(eventId: Long, userId: Long) {
        val event = eventRepository.findById(eventId).orElseThrow { Exception("Event not found") }
        checkAdminAccess(userId, event.team.id)

        eventRepository.delete(event)
    }

    @Transactional
    fun updateEvent(eventId: Long, updatedEventData: EventCreationDto): Event {
        val event = eventRepository.findById(eventId).orElseThrow { Exception("Event not found") }
        checkAdminAccess(updatedEventData.userId, event.team.id)

        val imageUrl = uploadImageIfPresent(updatedEventData.imageFile) ?: event.posterUrl

        val updatedEvent = event.copy(
            name = updatedEventData.name,
            description = updatedEventData.description,
            dateTime = updatedEventData.dateTime,
            posterUrl = imageUrl,
            linkToAlbum = updatedEventData.linkToAlbum,
            teamResult = updatedEventData.teamResult,
            location = updatedEventData.location,
            isRegistrationOpen = updatedEventData.isRegistrationOpen

        )

        return eventRepository.save(updatedEvent)
    }

    @Transactional
    private fun sendEventNotification(event: Event, participantFullName: String, notificationType: String) {
        val teamNotificationSettings = teamNotificationSettingsRepository.findByTeamId(event.team.id)
        if ((notificationType == "registration" && teamNotificationSettings!!.registrationNotificationEnabled) ||
            (notificationType == "unregistration" && teamNotificationSettings!!.unregisterNotificationEnabled)
        ) {
            val actionMessage = when (notificationType) {
                "registration" -> "✅ Регистрация на мероприятие!"
                "unregistration" -> "❌ Отмена регистрации!"
                else -> return
            }

            val message = "$actionMessage \n\n" +
                    "📌 Мероприятие: ${event.name}\n" +
                    "👤 Участник: $participantFullName\n\n" +
                    "📅 Дата: ${event.dateTime}\n\n"

            telegramService.sendMessageToChannel(chatId, message)
        }
    }

    fun getEventsByTeamId(teamId: Long): List<EventResponseDto> {
        return eventRepository.findByTeamId(teamId).map { x->x.toResponseDto() }
    }
}
