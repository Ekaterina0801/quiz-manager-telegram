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
    private val inviteCodeGeneratorService: InviteCodeGeneratorService, private val telegramService: TelegramService,
    private val userService: UserService, private val userRepository: UserRepository
) {
    /**
     * Создает новую команду с настройками по умолчанию.
     *
     * @param teamName название команды
     * @param chatId ID чата команды
     * @return созданная команда
     */
    @Transactional
    fun createTeam(teamName: String, chatId: String): TeamResponseDto {
        var inviteCode: String
        do {
            inviteCode = inviteCodeGeneratorService.generateInviteCode()
        } while (teamRepository.existsByInviteCode(inviteCode))

        val team = Team(id = 0L, name = teamName, inviteCode = inviteCode, chatId = chatId)
        val savedTeam = teamRepository.save(team)

        val defaultSettings = TeamNotificationSettings(
            team = savedTeam,
            registrationNotificationEnabled = true,
            unregisterNotificationEnabled = true,
            eventReminderEnabled = true,
            registrationReminderHoursBeforeEvent = 24
        )
        teamNotificationSettingsRepository.save(defaultSettings)
        syncChatAdminsWithTeam(savedTeam.id)
        return savedTeam.toDto()
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    fun syncAllTeamsAdmins() {
        val teams = teamRepository.findAll()
        teams.forEach { team ->
            syncChatAdminsWithTeam(team.id)
        }
    }

    /**
     * Синхронизирует администраторов чата с командой.
     * Назначает всех администраторов чата администраторами команды.
     *
     * @param teamId ID команды
     */
    @Transactional
    fun syncChatAdminsWithTeam(teamId: Long) {
        val team = teamRepository.findById(teamId).orElseThrow { IllegalArgumentException("Команда не найдена") }
        val chatId = team.chatId

        val chatAdmins = telegramService.getChatAdministrators(chatId)
        val adminIds = chatAdmins.map { it.id.toString() }.toSet()

        val existingAdmins = teamMembershipRepository.findByTeamIdAndRole(teamId, Role.ADMIN)
        val existingAdminIds = existingAdmins?.map { it.user.telegramId }?.toSet()


        chatAdmins.forEach { admin ->
            val user = userService.findOrCreateUser(admin.id.toString())

            teamMembershipRepository.findByTeamIdAndUserId(teamId, user.id)?.let { existingMembership ->
                if (existingMembership.role != Role.ADMIN) {
                    existingMembership.role = Role.ADMIN
                    teamMembershipRepository.save(existingMembership)
                }
            } ?: run {
                teamMembershipRepository.save(
                    TeamMembership(
                        user = user,
                        team = team,
                        role = Role.ADMIN
                    )
                )
            }
        }

        val removedAdmins = existingAdmins?.filter { it.user.telegramId !in adminIds }

        if (removedAdmins != null) {
            removedAdmins.forEach { membership ->
                membership.role = Role.USER
                teamMembershipRepository.save(membership)
            }
        }
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
        val teamMembership = teamMembershipRepository.findByTeamIdAndUserId(teamId, userId)
            ?: throw IllegalArgumentException("Пользователь не состоит в данной команде")
        teamMembershipRepository.delete(teamMembership)
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

}
