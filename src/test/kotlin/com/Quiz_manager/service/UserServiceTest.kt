package com.Quiz_manager.service

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.TelegramUser
import com.Quiz_manager.repository.TeamMembershipRepository
import com.Quiz_manager.repository.TeamRepository
import com.Quiz_manager.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.any
import org.mockito.kotlin.whenever
import java.util.*

class UserServiceTest {

    private lateinit var userService: UserService
    private lateinit var userRepository: UserRepository
    private lateinit var teamRepository: TeamRepository
    private lateinit var teamMembershipRepository: TeamMembershipRepository
    private lateinit var telegramService: TelegramService

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        teamRepository = mock()
        teamMembershipRepository = mock()
        telegramService = mock()

        userService = UserService(userRepository, teamRepository, teamMembershipRepository, telegramService)
    }

    @Test
    fun `getUserById should return user when user exists`() {
        // Arrange
        val userId = 1L
        val user = User(id = userId, firstName = "Ivan", lastName = "Ivanov", username = "testUser", telegramId = "123")
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        // Act
        val result = userService.getUserById(userId)

        // Assert
        assertEquals(user, result)
        verify(userRepository).findById(userId)
    }


    @Test
    fun `getUserById should throw exception when user does not exist`() {

        val userId = 1L
        whenever(userRepository.findById(userId)).thenReturn(null)

        assertThrows(Exception::class.java) {
            userService.getUserById(userId)
        }
        verify(userRepository).findById(userId)
    }

    @Test
    fun `findOrCreateUser should return existing user`() {

        val telegramId = "123"
        val user = User(id = 1L, firstName = "Ivan", lastName = "Ivanov", username = "testUser", telegramId = "123")
        whenever(userRepository.findByTelegramId(telegramId)).thenReturn(user)


        val result = userService.findOrCreateUser(telegramId)


        assertEquals(user, result)
        verify(userRepository).findByTelegramId(telegramId)
        verifyNoInteractions(telegramService)
    }

    @Test
    fun `findOrCreateUser should create new user when user does not exist`() {

        val telegramId = "123"
        val telegramUser = TelegramUser(id = 123, firstName = "John", lastName = "Doe", username = "johndoe")
        val newUser = User(id = 1L, username = "johndoe", firstName = "John", lastName = "Doe", telegramId = telegramId)

        whenever(userRepository.findByTelegramId(telegramId)).thenReturn(null)
        whenever(telegramService.getUserInfo(telegramId)).thenReturn(telegramUser)
        whenever(userRepository.save(any())).thenReturn(newUser)


        val result = userService.findOrCreateUser(telegramId)


        assertEquals(newUser, result)
        verify(userRepository).findByTelegramId(telegramId)
        verify(telegramService).getUserInfo(telegramId)
        verify(userRepository).save(any())
    }

    @Test
    fun `findOrCreateUser should throw exception when telegram user info is not available`() {

        val telegramId = "123"
        whenever(userRepository.findByTelegramId(telegramId)).thenReturn(null)
        whenever(telegramService.getUserInfo(telegramId)).thenReturn(null)


        assertThrows(IllegalStateException::class.java) {
            userService.findOrCreateUser(telegramId)
        }
        verify(userRepository).findByTelegramId(telegramId)
        verify(telegramService).getUserInfo(telegramId)
    }

    @Test
    fun `registerUserToTeam should return user when team is found`() {

        val telegramId = "123"
        val inviteCode = "ABC123"
        val user = User(id = 1L, firstName = "Ivan", lastName = "Ivanov", username = "testUser", telegramId = "123")
        val team = Team(id = 1L, name = "Test Team", inviteCode = inviteCode, chatId = "123")

        whenever(userRepository.findByTelegramId(telegramId)).thenReturn(user)
        whenever(teamRepository.findByInviteCode(inviteCode)).thenReturn(team)
        whenever(teamMembershipRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = userService.registerUserToTeam(telegramId, inviteCode)

        assertEquals(user, result)
        verify(userRepository).findByTelegramId(telegramId)
        verify(teamRepository).findByInviteCode(inviteCode)
        verify(teamMembershipRepository).save(any())
    }

    @Test
    fun `registerUserToTeam should return null when team is not found`() {

        val telegramId = "123"
        val inviteCode = "ABC123"
        val user = User(id = 1L, firstName = "Ivan", lastName = "Ivanov", username = "testUser", telegramId = "123")

        whenever(userRepository.findByTelegramId(telegramId)).thenReturn(user)
        whenever(teamRepository.findByInviteCode(inviteCode)).thenReturn(null)


        val result = userService.registerUserToTeam(telegramId, inviteCode)


        assertNull(result)
        verify(userRepository).findByTelegramId(telegramId)
        verify(teamRepository).findByInviteCode(inviteCode)
        verifyNoInteractions(teamMembershipRepository)
    }

    @Test
    fun `removeUserFromTeam should return success message when user is removed`() {

        val telegramId = "123"
        val inviteCode = "ABC123"
        val user = User(id = 1L, firstName = "Ivan", lastName = "Ivanov", username = "testUser", telegramId = "123")
        val team = Team(id = 1L, name = "Test Team", inviteCode = inviteCode, chatId = "123")
        val teamMembership = TeamMembership(id = 1L, user = user, team = team)

        whenever(userRepository.findByTelegramId(telegramId)).thenReturn(user)
        whenever(teamRepository.findByInviteCode(inviteCode)).thenReturn(team)
        whenever(teamMembershipRepository.findByTeamIdAndUserId(team.id, user.id)).thenReturn(teamMembership)


        val result = userService.removeUserFromTeam(telegramId, inviteCode)


        assertEquals("Пользователь успешно удален из команды", result)
        verify(userRepository).findByTelegramId(telegramId)
        verify(teamRepository).findByInviteCode(inviteCode)
        verify(teamMembershipRepository).findByTeamIdAndUserId(team.id, user.id)
        verify(teamMembershipRepository).delete(teamMembership)
    }

    @Test
    fun `removeUserFromTeam should return error message when user is not in team`() {

        val telegramId = "123"
        val inviteCode = "ABC123"
        val user = User(id = 1L, firstName = "Ivan", lastName = "Ivanov", username = "testUser", telegramId = "123")
        val team = Team(id = 1L, name = "Test Team", inviteCode = inviteCode, chatId = "123")

        whenever(userRepository.findByTelegramId(telegramId)).thenReturn(user)
        whenever(teamRepository.findByInviteCode(inviteCode)).thenReturn(team)
        whenever(teamMembershipRepository.findByTeamIdAndUserId(team.id, user.id)).thenReturn(null)


        val result = userService.removeUserFromTeam(telegramId, inviteCode)

        assertEquals("Пользователь не состоит в этой команде", result)
        verify(userRepository).findByTelegramId(telegramId)
        verify(teamRepository).findByInviteCode(inviteCode)
        verify(teamMembershipRepository).findByTeamIdAndUserId(team.id, user.id)
        verifyNoMoreInteractions(teamMembershipRepository)
    }
}