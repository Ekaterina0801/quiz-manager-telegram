package com.Quiz_manager.service

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Team
import com.Quiz_manager.dto.request.EventCreationDto
import com.Quiz_manager.repository.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class EventServiceTest {

    private lateinit var eventService: EventService

    private val eventRepository: EventRepository = mock(EventRepository::class.java)
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val registrationRepository: RegistrationRepository = mock(RegistrationRepository::class.java)
    private val userService: UserService = mock(UserService::class.java)
    private val cloudinaryService: CloudinaryService = mock(CloudinaryService::class.java)
    private val telegramService: TelegramService = mock(TelegramService::class.java)
    private val teamRepository: TeamRepository = mock(TeamRepository::class.java)
    private val teamNotificationSettingsRepository: TeamNotificationSettingsRepository = mock(TeamNotificationSettingsRepository::class.java)

    private val chatId = "123456789"

    @BeforeEach
    fun setUp() {
        eventService = EventService(
            eventRepository,
            userRepository,
            registrationRepository,
            userService,
            cloudinaryService,
            telegramService,
            teamNotificationSettingsRepository,
            chatId,
            teamRepository
        )
    }

    @Test
    fun `should create event successfully`() {
        val team = Team(
            id = 1L,
            name = "Test Team",
            inviteCode = "ABC123",
            chatId = "123456",
            teamMemberships = mutableListOf()
        )

        val imageFile = MockMultipartFile("image", "image.jpg", "image/jpeg", ByteArray(10))
        val userId = 1L
        val teamId = 1L

        val cloudinaryServiceMock = mock(CloudinaryService::class.java)
        val eventRepository = mock(EventRepository::class.java)
        val userRepository = mock(UserRepository::class.java)
        val registrationRepository = mock(RegistrationRepository::class.java)
        val userService = mock(UserService::class.java)
        val teamRepository = mock(TeamRepository::class.java)

        val eventData = EventCreationDto(
            name = "Test Event",
            description = "Description",
            dateTime = LocalDateTime.now(),
            imageFile = imageFile,
            linkToAlbum = null,
            teamResult = null,
            teamId = teamId,
            location = "Test",
            userId = userId,
            isRegistrationOpen = true
        )

        `when`(userService.isUserAdmin(userId, teamId)).thenReturn(true)
        `when`(teamRepository.findById(teamId)).thenReturn(Optional.of(team))

        val savedEvent = Event(
            id = 1L,
            name = eventData.name,
            description = eventData.description,
            dateTime = eventData.dateTime,
            posterUrl = null,
            linkToAlbum = eventData.linkToAlbum,
            teamResult = eventData.teamResult,
            team = team,
            location = eventData.location,
            isRegistrationOpen = true
        )

        `when`(eventRepository.save(any(Event::class.java))).thenReturn(savedEvent)

        val eventService = EventService(
            eventRepository, userRepository, registrationRepository, userService, cloudinaryServiceMock,
            telegramService, teamNotificationSettingsRepository, "chat_id", teamRepository
        )

        val event = eventService.createEvent(eventData)

        assertNotNull(event)
        assertEquals("Test Event", event.name)

        verify(eventRepository, times(1)).save(any(Event::class.java))
    }




    @Test
    fun `should delete event when user is admin`() {
        val team = Team(
            id = 1L,
            name = "Test Team",
            inviteCode = "ABC123",
            chatId = "123456",
            teamMemberships = mutableListOf()
        )
        val event = Event(
            id = 1L,
            name = "Test Event",
            description = "Description",
            dateTime = LocalDateTime.now(),
            posterUrl = null,
            linkToAlbum = null,
            teamResult = null,
            team = team,
            location = "Test",
            isRegistrationOpen = true
        )

        val eventId = 1L
        val userId = 1L

        `when`(eventRepository.findById(eventId)).thenReturn(Optional.of(event))
        `when`(userService.isUserAdmin(userId, team.id)).thenReturn(true)

        eventService.deleteEvent(eventId, userId)

        verify(eventRepository, times(1)).delete(event)
    }

    @Test
    fun `should not delete event when user is not admin`() {
        val team = Team(
            id = 1L,
            name = "Test Team",
            inviteCode = "ABC123",
            chatId = "123456",
            teamMemberships = mutableListOf()
        )
        val event = Event(
            id = 1L,
            name = "Test Event",
            description = "Description",
            dateTime = LocalDateTime.now(),
            posterUrl = null,
            linkToAlbum = null,
            teamResult = null,
            team = team,
            location = "Test",
            isRegistrationOpen = true
        )

        val eventId = 1L
        val userId = 2L

        `when`(eventRepository.findById(eventId)).thenReturn(Optional.of(event))
        `when`(userService.isUserAdmin(userId, team.id)).thenReturn(false)

        val exception = assertThrows(Exception::class.java) {
            eventService.deleteEvent(eventId, userId)
        }

        assertEquals("Access denied. Only admins can perform this action.", exception.message)
        verify(eventRepository, never()).delete(event)
    }

    @Test
    fun `should get event by id`() {
        val team = Team(
            id = 1L,
            name = "Test Team",
            inviteCode = "ABC123",
            chatId = "123456",
            teamMemberships = mutableListOf()
        )
        val event = Event(
            id = 1L,
            name = "Test Event",
            description = "Description",
            dateTime = LocalDateTime.now(),
            posterUrl = null,
            linkToAlbum = null,
            teamResult = null,
            team = team,
            location = "Test",
            isRegistrationOpen = true
        )

        val eventId = 1L

        `when`(eventRepository.findById(eventId)).thenReturn(Optional.of(event))

        val result = eventService.getEventById(eventId)

        assertNotNull(result)
        assertEquals("Test Event", result?.name)
    }
}
