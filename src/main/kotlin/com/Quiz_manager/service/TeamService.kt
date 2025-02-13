package com.Quiz_manager.service

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.TeamNotificationSettings
import com.Quiz_manager.domain.User
import com.Quiz_manager.enums.Role
import com.Quiz_manager.repository.*
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val teamMembershipRepository: TeamMembershipRepository,
    private val teamNotificationSettingsRepository: TeamNotificationSettingsRepository,
    private val inviteCodeGeneratorService: InviteCodeGeneratorService, private val telegramService: TelegramService,
    private val userService: UserService
) {
    /**
     * Создает новую команду с настройками по умолчанию.
     *
     * @param teamName название команды
     * @param chatId ID чата команды
     * @return созданная команда
     */
    fun createTeam(teamName: String, chatId: String): Team {
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
        return savedTeam
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

        val existingAdmins = teamMembershipRepository.findByTeamAndRole(team, Role.ADMIN)
        val existingAdminIds = existingAdmins?.map { it.user.telegramId }?.toSet()


        chatAdmins.forEach { admin ->
            val user = userService.findOrCreateUser(admin.id.toString())

            teamMembershipRepository.findByTeamAndUser(team, user)?.let { existingMembership ->
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
    fun updateTeamNotificationSettings(team: Team, updatedSettings: TeamNotificationSettings, currentUser: User): TeamNotificationSettings {

        val currentSettings = teamNotificationSettingsRepository.findByTeam(team)
            ?: throw IllegalArgumentException("Настройки уведомлений для этой команды не найдены")


        val isAdmin = teamMembershipRepository.existsByTeamAndUserAndRole(team, currentUser, Role.ADMIN)
        if (!isAdmin) {
            throw IllegalAccessException("Только администратор команды может изменять настройки")
        }


        currentSettings.apply {
            registrationNotificationEnabled = updatedSettings.registrationNotificationEnabled
            unregisterNotificationEnabled = updatedSettings.unregisterNotificationEnabled
            eventReminderEnabled = updatedSettings.eventReminderEnabled
            registrationReminderHoursBeforeEvent = updatedSettings.registrationReminderHoursBeforeEvent
        }

        // Сохраняем обновленные настройки
        return teamNotificationSettingsRepository.save(currentSettings)
    }


    /**
     * Получает настройки уведомлений для команды.
     *
     * @param team команда
     * @return настройки уведомлений
     */
    fun getTeamNotificationSettings(team: Team): TeamNotificationSettings {
        return teamNotificationSettingsRepository.findByTeam(team)
            ?: throw IllegalArgumentException("Настройки уведомлений для этой команды не найдены")
    }

    /**
     * Получает команду по ID.
     *
     * @param teamId ID команды
     * @return команда
     */
    fun getTeamById(teamId: Long): Team {
        return teamRepository.findById(teamId).orElseThrow { IllegalArgumentException("Команда не найдена") }
    }

    fun getTeamByTelegramId(telegramId: String): Team? {
        return teamRepository.findByChatId(telegramId)
    }

    /**
     * Получает команду по коду приглашения.
     *
     * @param inviteCode код приглашения команды
     * @return команда или null, если команда не найдена
     */
    fun getTeamByCode(inviteCode: String): Team? {
        return teamRepository.findByInviteCode(inviteCode)
    }


    fun getTeams(): List<Team>? {
        return teamRepository.findAll()
    }


    /**
     * Добавляет пользователя в команду с указанной ролью.
     *
     * @param user пользователь
     * @param team команда
     * @param role роль пользователя в команде
     * @return объект TeamMembership
     */
    fun addUserToTeam(user: User, team: Team, role: Role?): TeamMembership {
        val existingMembership = teamMembershipRepository.findByTeamAndUser(team, user)
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
    fun removeUserFromTeam(user: User, team: Team) {
        val teamMembership = teamMembershipRepository.findByTeamAndUser(team, user)
            ?: throw IllegalArgumentException("Пользователь не состоит в данной команде")
        teamMembershipRepository.delete(teamMembership)
    }

    /**
     * Получает всех пользователей в команде.
     *
     * @param team команда
     * @return список пользователей в команде
     */
    fun getAllUsersInTeam(team: Team): List<User> {
        return teamMembershipRepository.findByTeam(team).map { it.user }
    }

    /**
     * Получает все команды, в которых состоит пользователь.
     *
     * @param user пользователь
     * @return список команд, в которых состоит пользователь
     */
    fun getAllTeamsByUser(user: User): List<Team> {
        return teamMembershipRepository.findByUser(user).map { it.team }
    }

}
