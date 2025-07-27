package com.Quiz_manager.service

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.TeamNotificationSettings
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.request.TeamNotificationSettingsCreationDto
import com.Quiz_manager.dto.response.TeamResponseDto
import com.Quiz_manager.dto.response.UserResponseDto
import com.Quiz_manager.enums.Role
import com.Quiz_manager.mapper.TeamMapper
import com.Quiz_manager.mapper.TeamMembershipMapper
import com.Quiz_manager.mapper.TeamNotificationSettingsMapper
import com.Quiz_manager.mapper.UserMapper

import com.Quiz_manager.repository.*
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val teamMembershipRepository: TeamMembershipRepository,
    private val teamNotificationSettingsRepository: TeamNotificationSettingsRepository,
    private val inviteCodeGeneratorService: InviteCodeGeneratorService,
    private val userService: UserService,
    private val teamMapper: TeamMapper,
    private val teamNotificationSettingsMapper: TeamNotificationSettingsMapper,
    private val teamMembershipMapper: TeamMembershipMapper, private val userMapper: UserMapper
) {

    @Transactional
    fun createTeam(teamName: String, chatId: String?, creatorUserId: Long): TeamResponseDto {
        var inviteCode: String
        do {
            inviteCode = inviteCodeGeneratorService.generateInviteCode()
        } while (teamRepository.existsByInviteCode(inviteCode))

        val team = Team(
            id = 0L,
            name = teamName,
            inviteCode = inviteCode,
            chatId = chatId
        )
        val savedTeam = teamRepository.save(team)

        val defaultSettings = TeamNotificationSettings(
            team = savedTeam,
            registrationNotificationEnabled = true,
            unregisterNotificationEnabled = true,
            eventReminderEnabled = true,
            registrationReminderHoursBeforeEvent = 24
        )
        teamNotificationSettingsRepository.save(defaultSettings)

        val creator = userService.getUserById(creatorUserId)
        val membership = TeamMembership(
            team = savedTeam,
            user = creator,
            role = Role.MODERATOR
        )
        teamMembershipRepository.save(membership)

        return teamMapper.toDto(savedTeam)
    }

    @Transactional
    fun updateTeamNotificationSettings(
        teamId: Long,
        updatedSettings: TeamNotificationSettings,
        currentUserId: Long
    ): TeamNotificationSettingsCreationDto {
        val currentSettings = teamNotificationSettingsRepository.findByTeamId(teamId)
            ?: throw IllegalArgumentException("Настройки уведомлений не найдены")

        val isAdmin = teamMembershipRepository.existsByTeamIdAndUserIdAndRole(teamId, currentUserId, Role.ADMIN)
        if (!isAdmin) throw IllegalAccessException("Только администратор может изменять настройки")

        currentSettings.apply {
            registrationNotificationEnabled = updatedSettings.registrationNotificationEnabled
            unregisterNotificationEnabled = updatedSettings.unregisterNotificationEnabled
            eventReminderEnabled = updatedSettings.eventReminderEnabled
            registrationReminderHoursBeforeEvent = updatedSettings.registrationReminderHoursBeforeEvent
        }

        return teamNotificationSettingsMapper.toDto(teamNotificationSettingsRepository.save(currentSettings))
    }

    fun getTeamNotificationSettings(teamId: Long): TeamNotificationSettings {
        return teamNotificationSettingsRepository.findByTeamId(teamId)
            ?: throw IllegalArgumentException("Настройки уведомлений не найдены")
    }

    fun getTeamById(teamId: Long): TeamResponseDto {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Команда не найдена") }
        return teamMapper.toDto(team)
    }

    fun getTeamByCode(inviteCode: String): TeamResponseDto? {
        return teamRepository.findByInviteCode(inviteCode)?.let { teamMapper.toDto(it) }
    }

    fun getTeams(): List<TeamResponseDto> {
        return teamRepository.findAll().map { teamMapper.toDto(it) }
    }

    @Transactional
    fun deleteTeamById(teamId: Long) {
        val team = teamRepository.findById(teamId)
            .orElseThrow { EntityNotFoundException("Team not found") }

        team.teamMemberships.forEach { it.team = null }
        team.teamMemberships.clear()
        teamRepository.delete(team)
    }

    @Transactional
    fun addUserToTeam(userId: Long, teamId: Long, role: Role?): TeamMembership {
        val user = userService.getUserById(userId)
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Команда не найдена") }

        val existing = teamMembershipRepository.findByTeamIdAndUserId(teamId, userId)
        if (existing != null) return existing

        val teamMembership = TeamMembership(
            user = user,
            team = team,
            role = role ?: Role.USER
        )
        return teamMembershipRepository.save(teamMembership)
    }

    @Transactional
    fun removeUserFromTeam(userId: Long, teamId: Long) {
        val removed = teamMembershipRepository.deleteByTeamIdAndUserId(teamId, userId)
        if (removed == 0) throw IllegalArgumentException("Пользователь не состоит в команде")
    }

    fun getAllUsersInTeam(teamId: Long): List<UserResponseDto> {
        return teamMembershipRepository.findByTeamId(teamId).map { userMapper.toDto(it.user) }
    }

    fun getAllTeamsByUser(userId: Long): List<TeamResponseDto> {
        return teamMembershipRepository.findByUserId(userId)
            .mapNotNull { it.team?.let { team -> teamMapper.toDto(team) } }
    }

    @Transactional
    fun renameTeam(teamId: Long, newName: String): TeamResponseDto {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Команда не найдена") }

        team.name = newName
        return teamMapper.toDto(teamRepository.save(team))
    }

    @Transactional
    fun updateTeam(teamId: Long, newName: String, newChatId: String?, currentUserId: Long): TeamResponseDto {
        val isModerator = teamMembershipRepository
            .existsByTeamIdAndUserIdAndRole(teamId, currentUserId, Role.MODERATOR)
        if (!isModerator) throw IllegalAccessException("Только модератор может редактировать команду")

        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Команда не найдена") }

        team.name = newName
        team.chatId = newChatId
        return teamMapper.toDto(teamRepository.save(team))
    }
}
