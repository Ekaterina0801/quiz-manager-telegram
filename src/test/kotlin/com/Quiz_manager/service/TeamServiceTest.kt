package com.Quiz_manager.service

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.TeamNotificationSettings
import com.Quiz_manager.domain.User
import com.Quiz_manager.enums.Role
import com.Quiz_manager.repository.TeamMembershipRepository
import com.Quiz_manager.repository.TeamNotificationSettingsRepository
import com.Quiz_manager.repository.TeamRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.junit.jupiter.api.Assertions.*

import java.util.Optional
import org.junit.jupiter.api.assertThrows

class TeamServiceTest {

    private val teamRepository = mock(TeamRepository::class.java)
    private val teamMembershipRepository = mock(TeamMembershipRepository::class.java)
    private val teamNotificationSettingsRepository = mock(TeamNotificationSettingsRepository::class.java)
    private val inviteCodeGeneratorService = mock(InviteCodeGeneratorService::class.java)
    private val telegramService = mock(TelegramService::class.java)
    private val userService = mock(UserService::class.java)

    private val teamService = spy(
        TeamService(
            teamRepository,
            teamMembershipRepository,
            teamNotificationSettingsRepository,
            inviteCodeGeneratorService,
            telegramService,
            userService
        )
    )




    @Test
    fun `should create team successfully`() {
        val teamName = "Test Team"
        val chatId = "123456"
        val inviteCode = "ABC123"

        val team = Team(id = 0L, name = teamName, inviteCode = inviteCode, chatId = chatId)

        `when`(inviteCodeGeneratorService.generateInviteCode()).thenReturn(inviteCode)
        `when`(teamRepository.existsByInviteCode(inviteCode)).thenReturn(false)
        `when`(teamRepository.save(any(Team::class.java))).thenReturn(team)

        doNothing().`when`(teamService).syncChatAdminsWithTeam(anyLong())


        val savedTeam = teamService.createTeam(teamName, chatId)

        assertNotNull(savedTeam)
        assertEquals(teamName, savedTeam.name)
        assertEquals(chatId, savedTeam.chatId)
        assertEquals(inviteCode, savedTeam.inviteCode)

        verify(teamRepository, times(1)).save(any(Team::class.java))
        verify(teamNotificationSettingsRepository, times(1)).save(any(TeamNotificationSettings::class.java))
        verify(teamService, times(1)).syncChatAdminsWithTeam(savedTeam.id)
    }



    @Test
    fun `should update team notification settings successfully`() {
        val team = Team(id = 1L, name = "Test Team", inviteCode = "ABC123", chatId = "123456")
        val currentUser = User(
            id = 1L,
            username = "user",
            firstName = "John",
            lastName = "Doe",
            telegramId = "123456789"
        )
        val currentSettings = TeamNotificationSettings(
            team = team,
            registrationNotificationEnabled = true,
            unregisterNotificationEnabled = true,
            eventReminderEnabled = true,
            registrationReminderHoursBeforeEvent = 24
        )
        val updatedSettings = TeamNotificationSettings(
            team = team,
            registrationNotificationEnabled = false,
            unregisterNotificationEnabled = false,
            eventReminderEnabled = false,
            registrationReminderHoursBeforeEvent = 12
        )

        `when`(teamNotificationSettingsRepository.findByTeam(team)).thenReturn(currentSettings)
        `when`(teamMembershipRepository.existsByTeamAndUserAndRole(team, currentUser, Role.ADMIN)).thenReturn(true)
        `when`(teamNotificationSettingsRepository.save(any(TeamNotificationSettings::class.java))).thenReturn(updatedSettings)

        val result = teamService.updateTeamNotificationSettings(team, updatedSettings, currentUser)

        assertNotNull(result)
        assertFalse(result.registrationNotificationEnabled)
        assertFalse(result.unregisterNotificationEnabled)
        assertFalse(result.eventReminderEnabled)

        verify(teamNotificationSettingsRepository, times(1)).save(any(TeamNotificationSettings::class.java))
    }

    @Test
    fun `should throw exception if user is not admin`() {
        val team = Team(id = 1L, name = "Test Team", inviteCode = "ABC123", chatId = "123456")
        val currentUser = User(
            id = 1L,
            username = "user",
            firstName = "John",
            lastName = "Doe",
            telegramId = "123456789"
        )
        val updatedSettings = TeamNotificationSettings(
            team = team,
            registrationNotificationEnabled = false,
            unregisterNotificationEnabled = false,
            eventReminderEnabled = false,
            registrationReminderHoursBeforeEvent = 12
        )

        val currentSettings = TeamNotificationSettings(
            team = team,
            registrationNotificationEnabled = true,
            unregisterNotificationEnabled = true,
            eventReminderEnabled = true,
            registrationReminderHoursBeforeEvent = 24
        )

        `when`(teamNotificationSettingsRepository.findByTeam(team)).thenReturn(currentSettings)
        `when`(teamMembershipRepository.existsByTeamAndUserAndRole(team, currentUser, Role.ADMIN)).thenReturn(false)

        val exception = assertThrows<IllegalAccessException> {
            teamService.updateTeamNotificationSettings(team, updatedSettings, currentUser)
        }

        assertEquals("Только администратор команды может изменять настройки", exception.message)
    }

    @Test
    fun `should get team by id successfully`() {
        val teamId = 1L
        val team = Team(id = teamId, name = "Test Team", inviteCode = "ABC123", chatId = "123456")

        `when`(teamRepository.findById(teamId)).thenReturn(Optional.of(team))

        val result = teamService.getTeamById(teamId)

        assertNotNull(result)
        assertEquals(teamId, result.id)
        assertEquals("Test Team", result.name)
    }

    @Test
    fun `should throw exception if team not found by id`() {
        val teamId = 1L
        `when`(teamRepository.findById(teamId)).thenReturn(Optional.empty())

        val exception = assertThrows<IllegalArgumentException> {
            teamService.getTeamById(teamId)
        }

        assertEquals("Команда не найдена", exception.message)
    }

    @Test
    fun `should add user to team successfully`() {
        val user = User(
            id = 1L,
            username = "user",
            firstName = "John",
            lastName = "Doe",
            telegramId = "123456789"
        )
        val team = Team(id = 1L, name = "Test Team", inviteCode = "ABC123", chatId = "123456")

        val teamMembership = TeamMembership(user = user, team = team, role = Role.USER)

        `when`(teamMembershipRepository.findByTeamAndUser(team, user)).thenReturn(null)
        `when`(teamMembershipRepository.save(any(TeamMembership::class.java))).thenReturn(teamMembership)

        val result = teamService.addUserToTeam(user, team, Role.USER)

        assertNotNull(result)
        assertEquals(user, result.user)
        assertEquals(team, result.team)

        verify(teamMembershipRepository, times(1)).save(any(TeamMembership::class.java))
    }

    @Test
    fun `should return team membership if user already in team`() {
        val user = User(
            id = 1L,
            username = "user",
            firstName = "John",
            lastName = "Doe",
            telegramId = "123456789"
        )
        val team = Team(id = 1L, name = "Test Team", inviteCode = "ABC123", chatId = "123456")

        val existingMembership = TeamMembership(user = user, team = team, role = Role.USER)

        `when`(teamMembershipRepository.findByTeamAndUser(team, user)).thenReturn(existingMembership)

        val result = teamService.addUserToTeam(user, team, Role.USER)

        assertNotNull(result)
        assertEquals(existingMembership, result)

        verify(teamMembershipRepository, times(1)).findByTeamAndUser(team, user)
        verify(teamMembershipRepository, never()).save(any(TeamMembership::class.java))
    }


    @Test
    fun `should remove user from team successfully`() {

            val user = User(
                id = 1L,
                username = "user",
                firstName = "John",
                lastName = "Doe",
                telegramId = "123456789"
            )
        val team = Team(id = 1L, name = "Test Team", inviteCode = "ABC123", chatId = "123456")

        val teamMembership = TeamMembership(user = user, team = team, role = Role.USER)

        `when`(teamMembershipRepository.findByTeamAndUser(team, user)).thenReturn(teamMembership)

        teamService.removeUserFromTeam(user, team)

        verify(teamMembershipRepository, times(1)).delete(teamMembership)
    }

    @Test
    fun `should throw exception if user not in team`() {

            val user = User(
                id = 1L,
                username = "user",
                firstName = "John",
                lastName = "Doe",
                telegramId = "123456789"
            )
        val team = Team(id = 1L, name = "Test Team", inviteCode = "ABC123", chatId = "123456")

        `when`(teamMembershipRepository.findByTeamAndUser(team, user)).thenReturn(null)

        val exception = assertThrows<IllegalArgumentException> {
            teamService.removeUserFromTeam(user, team)
        }

        assertEquals("Пользователь не состоит в данной команде", exception.message)
    }
}
