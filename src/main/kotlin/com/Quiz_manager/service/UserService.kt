package com.Quiz_manager.service

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.User
import com.Quiz_manager.enums.Role
import com.Quiz_manager.repository.TeamMembershipRepository
import com.Quiz_manager.repository.TeamRepository
import com.Quiz_manager.repository.UserRepository
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service


@Service
class UserService(
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository,
    private val teamMembershipRepository: TeamMembershipRepository, @Lazy private val telegramService: TelegramService
) {

    fun getUserById(userId: Long): User {
        return userRepository.findById(userId).orElseThrow { Exception("Пользователь с ID $userId не найден") }
    }

    fun findOrCreateUser(telegramId: String): User {
        return userRepository.findByTelegramId(telegramId)
            ?: telegramService.getUserInfo(telegramId)?.let { telegramUser ->
                createUser(
                    username = telegramUser.username ?: telegramId,
                    firstName = telegramUser.firstName,
                    lastName = telegramUser.lastName,
                    telegramId = telegramId
                )
            } ?: throw IllegalStateException("Не удалось создать пользователя для Telegram ID: $telegramId")
    }


    fun getTeamById(teamId: Long): Team {
        return teamRepository.findById(teamId).orElseThrow { Exception("Команда с ID $teamId не найдена") }
    }

    fun getAllMembersOfTeam(teamId: Long): List<User> {
        return teamMembershipRepository.findByTeamId(teamId).map { it.user }
    }

    fun isUserAdmin(userId: Long, teamId: Long): Boolean {
        val user = userRepository.findById(userId).orElseThrow { Exception("Пользователь с ID $userId не найден") }
        val team = teamRepository.findById(teamId).orElseThrow { Exception("Команда с ID $teamId не найдена") }
        val teamMembership = teamMembershipRepository.findByTeamIdAndUserId(teamId, userId)
            ?: throw Exception("Пользователь ${user.username} не состоит в команде ${team.name}")

        return teamMembership.role == Role.ADMIN
    }

    fun assignAdminRole(userId: Long, teamId: Long): String {
        return updateUserRole(userId, teamId, Role.ADMIN)
    }

    fun revokeAdminRole(userId: Long, teamId: Long): String {
        return updateUserRole(userId, teamId, Role.USER)
    }

    fun getUserRoleInTeam(userId: Long, teamId: Long): Role {
        val user = userRepository.findById(userId).orElseThrow { Exception("Пользователь с ID $userId не найден") }
        val team = teamRepository.findById(teamId).orElseThrow { Exception("Команда с ID $teamId не найдена") }
        val teamMembership = teamMembershipRepository.findByTeamIdAndUserId(teamId, userId)
            ?: throw Exception("Пользователь ${user.username} не состоит в команде ${team.name}")

        return teamMembership.role
    }

    fun isUserInTeam(userId: Long, teamId: Long): Boolean {
        val user = userRepository.findById(userId).orElseThrow { Exception("Пользователь с ID $userId не найден") }
        val team = teamRepository.findById(teamId).orElseThrow { Exception("Команда с ID $teamId не найдена") }
        return teamMembershipRepository.existsByTeamIdAndUserId(teamId, userId)
    }

    fun getAdminsByTeam(teamId: Long): List<User>? {
        val team = teamRepository.findById(teamId).orElseThrow { Exception("Команда с ID $teamId не найдена") }


        val admins = teamMembershipRepository.findByTeamIdAndRole(teamId, Role.ADMIN)
            ?.map { it.user }

        return admins
    }



    /**
     * Регистрирует пользователя в команду по inviteCode.
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @param inviteCode код для присоединения к команде
     * @return пользователь или null, если команда не найдена
     */
    fun registerUserToTeam(telegramId: String, inviteCode: String): User? {
        val user = getUserByTelegramId(telegramId) ?: return null
        val team = teamRepository.findByInviteCode(inviteCode) ?: return null


        if (user.teamMemberships.any { it.team == team }) {
            return user
        }


        val teamMembership = TeamMembership(user = user, team = team)
        teamMembershipRepository.save(teamMembership)

        return user
    }

    /**
     * Удаляет пользователя из команды.
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @param inviteCode код команды
     * @return сообщение об успешном удалении или ошибке
     */
    fun removeUserFromTeam(telegramId: String, inviteCode: String): String {
        val user = getUserByTelegramId(telegramId) ?: return "Пользователь не найден"
        val team = teamRepository.findByInviteCode(inviteCode) ?: return "Команда не найдена"

        val teamMembership = teamMembershipRepository.findByTeamIdAndUserId(team.id, user.id)
            ?: return "Пользователь не состоит в этой команде"

        teamMembershipRepository.delete(teamMembership)
        return "Пользователь успешно удален из команды"
    }

    /**
     * Создает нового пользователя.
     *
     * @param username имя пользователя
     * @param firstName имя
     * @param lastName фамилия
     * @param telegramId Telegram ID пользователя
     * @return созданный пользователь
     */
    fun createUser(username: String, firstName: String?, lastName: String?, telegramId: String): User {
        return userRepository.save(
            User(
                id = 0L,
                username = username,
                firstName = firstName,
                lastName = lastName,
                telegramId = telegramId
            )
        )
    }

    fun createUserByTelegramId(telegramId: String): User {
        return userRepository.findByTelegramId(telegramId)
            ?: telegramService.getUserInfo(telegramId)?.let { telegramUser ->
                createUser(
                    username = telegramUser.username ?: telegramId,
                    firstName = telegramUser.firstName,
                    lastName = telegramUser.lastName,
                    telegramId = telegramId
                )
            } ?: throw IllegalStateException("Не удалось создать пользователя для Telegram ID: $telegramId")
    }


    /**
     * Удаляет пользователя.
     *
     * @param userId идентификатор пользователя
     * @return сообщение о результате операции
     * @throws Exception если пользователь не найден
     */
    fun deleteUser(userId: Long): String {
        val user = userRepository.findById(userId).orElseThrow { Exception("Пользователь не найден") }
        userRepository.delete(user)
        return "Пользователь ${user.username} был удален."
    }

    /**
     * Получает пользователя по Telegram ID.
     *
     * @param telegramId Telegram ID пользователя
     * @return пользователь или null, если не найден
     */
    fun getUserByTelegramId(telegramId: String): User? {
        return userRepository.findByTelegramId(telegramId)
    }

    /**
     * Обновляет информацию о пользователе.
     *
     * @param userId идентификатор пользователя
     * @param firstName новое имя пользователя
     * @param lastName новая фамилия пользователя
     * @return обновленный пользователь
     * @throws Exception если пользователь не найден
     */
    fun updateUserInfo(userId: Long, firstName: String?, lastName: String?): User {
        val user = userRepository.findById(userId).orElseThrow { Exception("Пользователь не найден") }
        user.firstName = firstName
        user.lastName = lastName
        return userRepository.save(user)
    }

    /**
     * Получает список всех пользователей.
     *
     * @return список всех пользователей
     */
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    /**
     * Находит пользователя по Telegram ID.
     *
     * @param telegramId Telegram ID пользователя
     * @return пользователь или null, если не найден
     */
    fun findByTelegramId(telegramId: String): User? {
        return userRepository.findByTelegramId(telegramId)
    }

    /**
     * Получает все команды, в которых состоит пользователь.
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @return список команд, в которых состоит пользователь
     */
    fun getTeamsByUser(telegramId: String): List<Team> {
        val user = getUserByTelegramId(telegramId) ?: return emptyList()
        return user.teamMemberships.map { it.team!! }
    }


    /**
     * Обновляет роль пользователя.
     *
     * @param userId идентификатор пользователя
     * @param newRole новая роль пользователя
     * @return сообщение о результате операции
     * @throws Exception если пользователь не найден
     */
    fun updateUserRole(userId: Long, teamId: Long, newRole: Role): String {
        val user = userRepository.findById(userId).orElseThrow {
            Exception("Пользователь с ID $userId не найден")
        }

        val team = teamRepository.findById(teamId).orElseThrow {
            Exception("Команда с ID $teamId не найдена")
        }

        val teamMembership = teamMembershipRepository.findByTeamIdAndUserId(teamId, userId)
            ?: throw Exception("Пользователь ${user.username} не состоит в команде ${team.name}")

        teamMembership.role = newRole
        teamMembershipRepository.save(teamMembership)

        return "Роль пользователя ${user.username} в команде ${team.name} обновлена на ${newRole.name}."
    }

}
