package com.Quiz_manager.service

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.TeamNotificationSettings
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.request.TeamNotificationSettingsCreationDto
import com.Quiz_manager.dto.response.TeamResponseDto
import com.Quiz_manager.dto.response.UserResponseDto
import com.Quiz_manager.enums.Role
import com.Quiz_manager.mapper.toDto
import com.Quiz_manager.mapper.toEntity
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
    private val userService: UserService, private val userRepository: UserRepository
) {
    @Transactional
    fun createTeam(teamName: String, chatId: String?, creatorUserId: Long): TeamResponseDto {
        // 1) сгенерировать уникальный inviteCode
        var inviteCode: String
        do {
            inviteCode = inviteCodeGeneratorService.generateInviteCode()
        } while (teamRepository.existsByInviteCode(inviteCode))

        // 2) сохранить команду
        val team = Team(
            id = 0L,
            name = teamName,
            inviteCode = inviteCode,
            chatId = chatId
        )
        val savedTeam = teamRepository.save(team)

        // 3) создать настройки уведомлений по умолчанию
        val defaultSettings = TeamNotificationSettings(
            team = savedTeam,
            registrationNotificationEnabled = true,
            unregisterNotificationEnabled = true,
            eventReminderEnabled = true,
            registrationReminderHoursBeforeEvent = 24
        )
        teamNotificationSettingsRepository.save(defaultSettings)

        // 4) добавить создателя в состав команды с ролью MODERATOR
        val creator = userService.getUserById(creatorUserId)
        val membership = TeamMembership(
            team = savedTeam,
            user = creator,
            role = Role.MODERATOR
        )
        teamMembershipRepository.save(membership)

        return savedTeam.toDto()
    }


    /**
     * Обновляет настройки уведомлений для команды.
     *
     * @param team команда
     * @param updatedSettings новые настройки
     * @return обновленные настройки
     */
    @Transactional
    fun updateTeamNotificationSettings(teamId: Long, updatedSettings: TeamNotificationSettings, currentUserId: Long): TeamNotificationSettingsCreationDto {
        val currentSettings = teamNotificationSettingsRepository.findByTeamId(teamId)
            ?: throw IllegalArgumentException("Настройки уведомлений для этой команды не найдены")


        val isAdmin = teamMembershipRepository.existsByTeamIdAndUserIdAndRole(teamId, currentUserId, Role.ADMIN)
        if (!isAdmin) {
            throw IllegalAccessException("Только администратор команды может изменять настройки")
        }


        currentSettings.apply {
            registrationNotificationEnabled = updatedSettings.registrationNotificationEnabled
            unregisterNotificationEnabled = updatedSettings.unregisterNotificationEnabled
            eventReminderEnabled = updatedSettings.eventReminderEnabled
            registrationReminderHoursBeforeEvent = updatedSettings.registrationReminderHoursBeforeEvent
        }

        return teamNotificationSettingsRepository.save(currentSettings).toDto()
    }


    /**
     * Получает настройки уведомлений для команды.
     *
     * @param team команда
     * @return настройки уведомлений
     */
    @Transactional
    fun getTeamNotificationSettings(teamId: Long): TeamNotificationSettings {
        return teamNotificationSettingsRepository.findByTeamId(teamId)
            ?: throw IllegalArgumentException("Настройки уведомлений для этой команды не найдены")
    }

    /**
     * Получает команду по ID.
     *
     * @param teamId ID команды
     * @return команда
     */
    fun getTeamById(teamId: Long): TeamResponseDto {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Команда не найдена") }

        return team.toDto()
    }

    fun getTeamByChatId(chatId: String): TeamResponseDto? {
        return teamRepository.findByChatId(chatId)?.toDto()
    }

    @Transactional
    fun deleteTeamById(teamId: Long) {
        val team = teamRepository.findById(teamId).orElseThrow { EntityNotFoundException("Team not found") }
        team.teamMemberships.forEach { it.team = null }
        team.teamMemberships.clear()
        //teamMembershipRepository.deleteAll(team.teamMemberships)

        teamRepository.delete(team)
    }




    /**
     * Получает команду по коду приглашения.
     *
     * @param inviteCode код приглашения команды
     * @return команда или null, если команда не найдена
     */
    fun getTeamByCode(inviteCode: String): TeamResponseDto? {
        return teamRepository.findByInviteCode(inviteCode)?.toDto()
    }


    fun getTeams(): List<TeamResponseDto>? {
        return teamRepository.findAll().map { x->x.toDto() }
    }


    /**
     * Добавляет пользователя в команду с указанной ролью.
     *
     * @param user пользователь
     * @param team команда
     * @param role роль пользователя в команде
     * @return объект TeamMembership
     */
    @Transactional
    fun addUserToTeam(userId: Long, teamId: Long, role: Role?): TeamMembership {
        val user = userService.getUserById(userId)
        val team = getTeamById(teamId).toEntity()
        val existingMembership = teamMembershipRepository.findByTeamIdAndUserId(teamId, userId)
        if (existingMembership != null) {
            return existingMembership
        }
        val teamMembership: TeamMembership = if (role==null)
            TeamMembership(user = user, team = team)
        else
            TeamMembership(user = user, team = team, role = role)
        return teamMembershipRepository.save(teamMembership)
    }

    /**
     * Удаляет пользователя из команды.
     *
     * @param user пользователь
     * @param team команда
     */
    @Transactional
    fun removeUserFromTeam(userId: Long, teamId: Long) {
        val removedCount = teamMembershipRepository.deleteByTeamIdAndUserId(teamId, userId)
        if (removedCount == 0) {
            throw IllegalArgumentException("Пользователь с ID $userId не состоит в команде $teamId")
        }
    }

    /**
     * Получает всех пользователей в команде.
     *
     * @param team команда
     * @return список пользователей в команде
     */
    fun getAllUsersInTeam(teamId: Long): List<UserResponseDto> {
        return teamMembershipRepository.findByTeamId(teamId).map { it.user.toDto() }
    }

    /**
     * Получает все команды, в которых состоит пользователь.
     *
     * @param user пользователь
     * @return список команд, в которых состоит пользователь
     */
    fun getAllTeamsByUser(userId: Long): List<TeamResponseDto> {
        return teamMembershipRepository.findByUserId(userId).map { it.team!!.toDto() }
    }

    @Transactional
    fun renameTeam(teamId: Long, newName: String): TeamResponseDto {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Команда не найдена: $teamId") }
        team.name = newName
        val saved = teamRepository.save(team)
        return saved.toDto()
    }

    @Transactional
    fun updateTeam(
        teamId: Long,
        newName: String,
        newChatId: String?,
        currentUserId: Long
    ): TeamResponseDto {

        val isModerator = teamMembershipRepository
            .existsByTeamIdAndUserIdAndRole(teamId, currentUserId, Role.MODERATOR)
        if (!isModerator) {
            throw IllegalAccessException("Только модератор может редактировать команду")
        }

        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Команда не найдена: $teamId") }

        team.name = newName
        team.chatId = newChatId
        val saved = teamRepository.save(team)

        return saved.toDto()
    }



}
