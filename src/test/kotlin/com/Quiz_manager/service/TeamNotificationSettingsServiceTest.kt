package com.Quiz_manager.service

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamNotificationSettings
import com.Quiz_manager.dto.TeamNotificationSettingsDTO
import com.Quiz_manager.mapper.toEntity
import com.Quiz_manager.repository.TeamNotificationSettingsRepository
import com.Quiz_manager.repository.TeamRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.*

class TeamNotificationSettingsServiceTest {

    private val teamRepository = mock(TeamRepository::class.java)
    private val teamNotificationSettingsRepository = mock(TeamNotificationSettingsRepository::class.java)

    private val service = TeamNotificationSettingsService(teamNotificationSettingsRepository, teamRepository)

    @Test
    fun `should return settings for team`() {

        val teamId = 1L
        val team = Team(id = teamId, name = "Test Team", inviteCode = "ABC123", chatId = "123456")
        val settings = TeamNotificationSettings(
            team = team,
            registrationNotificationEnabled = true,
            unregisterNotificationEnabled = false,
            eventReminderEnabled = true,
            registrationReminderHoursBeforeEvent = 24
        )

        `when`(teamRepository.findById(teamId)).thenReturn(Optional.of(team))
        `when`(teamNotificationSettingsRepository.findByTeam(team)).thenReturn(settings)


        val result = service.getSettingsForTeam(teamId)

        assertNotNull(result)
        assertTrue(result.registrationNotificationEnabled)
        assertFalse(result.unregisterNotificationEnabled)
        assertEquals(24, result.registrationReminderHoursBeforeEvent)

        verify(teamRepository).findById(teamId)
        verify(teamNotificationSettingsRepository).findByTeam(team)
    }

    @Test
    fun `should throw exception if team not found`() {
        val teamId = 1L
        `when`(teamRepository.findById(teamId)).thenReturn(Optional.empty())

        val exception = assertThrows<EntityNotFoundException> {
            service.getSettingsForTeam(teamId)
        }

        assertEquals("Команда с id $teamId не найдена", exception.message)
    }

    @Test
    fun `should create or update settings successfully`() {
        val teamId = 1L
        val team = Team(id = teamId, name = "Test Team", inviteCode = "ABC123", chatId = "123456")
        val settingsDTO = TeamNotificationSettingsDTO(
            registrationNotificationEnabled = true,
            unregisterNotificationEnabled = false,
            eventReminderEnabled = true,
            registrationReminderHoursBeforeEvent = 12,
            teamId = teamId
        )
        val updatedSettings = settingsDTO.toEntity(team)

        `when`(teamRepository.findById(teamId)).thenReturn(Optional.of(team))
        `when`(teamNotificationSettingsRepository.findByTeam(team)).thenReturn(null)
        `when`(teamNotificationSettingsRepository.save(any(TeamNotificationSettings::class.java))).thenReturn(updatedSettings)


        val result = service.createOrUpdateSettings(teamId, settingsDTO)


        assertNotNull(result)
        assertTrue(result.registrationNotificationEnabled)
        assertFalse(result.unregisterNotificationEnabled)
        assertEquals(12, result.registrationReminderHoursBeforeEvent)

        verify(teamNotificationSettingsRepository).save(any(TeamNotificationSettings::class.java))
    }

    @Test
    fun `should update existing settings successfully`() {
        val teamId = 1L
        val team = Team(id = teamId, name = "Test Team", inviteCode = "ABC123", chatId = "123456")
        val existingSettings = TeamNotificationSettings(
            team = team,
            registrationNotificationEnabled = false,
            unregisterNotificationEnabled = false,
            eventReminderEnabled = false,
            registrationReminderHoursBeforeEvent = 24
        )
        val settingsDTO = TeamNotificationSettingsDTO(
            registrationNotificationEnabled = true,
            unregisterNotificationEnabled = true,
            eventReminderEnabled = true,
            registrationReminderHoursBeforeEvent = 10,
            teamId = teamId
        )

        `when`(teamRepository.findById(teamId)).thenReturn(Optional.of(team))
        `when`(teamNotificationSettingsRepository.findByTeam(team)).thenReturn(existingSettings)

        val result = service.createOrUpdateSettings(teamId, settingsDTO)

        assertNotNull(result)
        assertTrue(result.registrationNotificationEnabled)
        assertTrue(result.unregisterNotificationEnabled)
        assertTrue(result.eventReminderEnabled)
        assertEquals(10, result.registrationReminderHoursBeforeEvent)

        verify(teamNotificationSettingsRepository, never()).save(any())
    }

    @Test
    fun `should delete settings successfully`() {
        val teamId = 1L
        val team = Team(id = teamId, name = "Test Team", inviteCode = "ABC123", chatId = "123456")
        val settings = TeamNotificationSettings(
            team = team,
            registrationNotificationEnabled = true,
            unregisterNotificationEnabled = true,
            eventReminderEnabled = true,
            registrationReminderHoursBeforeEvent = 24
        )

        `when`(teamRepository.findById(teamId)).thenReturn(Optional.of(team))
        `when`(teamNotificationSettingsRepository.findByTeam(team)).thenReturn(settings)


        service.deleteSettingsForTeam(teamId)


        verify(teamNotificationSettingsRepository).delete(settings)
    }

    @Test
    fun `should throw exception when deleting settings if team not found`() {
        val teamId = 1L
        `when`(teamRepository.findById(teamId)).thenReturn(Optional.empty())

        val exception = assertThrows<EntityNotFoundException> {
            service.deleteSettingsForTeam(teamId)
        }

        assertEquals("Команда с id $teamId не найдена", exception.message)
        verify(teamNotificationSettingsRepository, never()).delete(any())
    }

    @Test
    fun `should throw exception when deleting settings if settings not found`() {
        val teamId = 1L
        val team = Team(id = teamId, name = "Test Team", inviteCode = "ABC123", chatId = "123456")

        `when`(teamRepository.findById(teamId)).thenReturn(Optional.of(team))
        `when`(teamNotificationSettingsRepository.findByTeam(team)).thenReturn(null)

        val exception = assertThrows<EntityNotFoundException> {
            service.deleteSettingsForTeam(teamId)
        }

        assertEquals("Настройки уведомлений не найдены для команды с id $teamId", exception.message)
        verify(teamNotificationSettingsRepository, never()).delete(any())
    }
}
