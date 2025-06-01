package com.Quiz_manager.service

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.User
import com.Quiz_manager.enums.Role
import com.Quiz_manager.repository.TeamMembershipRepository
import com.Quiz_manager.repository.TeamRepository
import com.Quiz_manager.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.nio.file.AccessDeniedException
import java.security.Principal


@Service
class UserService(
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository,
    private val teamMembershipRepository: TeamMembershipRepository,
) {

    fun getUserById(userId: Long): User {
        return userRepository.findById(userId).orElseThrow { Exception("Пользователь с ID $userId не найден") }
    }

    @Throws(DataIntegrityViolationException::class)
    fun findByUsername(username: String): User? {
        val user = userRepository.findByUsername(username)
        return user
    }

    fun getCurrentUser(principal: Principal?): User {
        val username = principal?.name
            ?: throw UsernameNotFoundException("Нет аутентифицированного пользователя")
        return userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Пользователь $username не найден")
    }

    @Throws(DataIntegrityViolationException::class)
    fun findByEmail(email: String): User? {
        val user = userRepository.findByEmail(email)
        return user
    }

    fun findOrCreateUser(user: User): User {
        if (userRepository.findByUsername(user.username)!=null) {
            throw DataIntegrityViolationException("Пользователь с таким именем уже существует")
        }
        if (userRepository.findByEmail(user.username)!=null) {
            throw DataIntegrityViolationException("Пользователь с таким email уже существует")
        }
        return userRepository.save(user)
    }


    fun getAllMembersOfTeam(teamId: Long): List<User> {
        return teamMembershipRepository.findByTeamId(teamId).map { it.user }
    }

    fun isUserModerator(userId: Long, teamId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("Пользователь с ID $userId не найден") }

        if (user.role == Role.ADMIN) {
            return true
        }

        val team = teamRepository.findById(teamId)
            .orElseThrow { EntityNotFoundException("Команда с ID $teamId не найдена") }

        val membership = teamMembershipRepository.findByTeamIdAndUserId(teamId, userId)
            ?: throw AccessDeniedException("Пользователь ${user.username} не состоит в команде ${team.name}")

        return membership.role == Role.MODERATOR
    }

    fun isUserAdmin(userId: Long): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("Пользователь с ID $userId не найден") }
        return user.role == Role.ADMIN
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

    fun getModeratorsByTeam(teamId: Long): List<User>? {
        val team = teamRepository.findById(teamId).orElseThrow { Exception("Команда с ID $teamId не найдена") }
        val moderators = teamMembershipRepository.findByTeamIdAndRole(teamId, Role.ADMIN)
            ?.map { it.user }

        return moderators
    }

    /**
     * Регистрирует пользователя в команду по inviteCode.
     *
     * @param userId идентификатор пользователя
     * @param inviteCode код для присоединения к команде
     * @return пользователь или null, если команда не найдена
     */
    fun registerUserToTeam(userId: Long, inviteCode: String): User? {
        val user = getUserById(userId)
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
     * @param userId идентификатор пользователя
     * @param inviteCode код команды
     * @return сообщение об успешном удалении или ошибке
     */
    fun removeUserFromTeam(userId: Long, inviteCode: String): String {
        val user = getUserById(userId)
        val team = teamRepository.findByInviteCode(inviteCode) ?: return "Команда не найдена"

        val teamMembership = teamMembershipRepository.findByTeamIdAndUserId(team.id, user.id)
            ?: return "Пользователь не состоит в этой команде"

        teamMembershipRepository.delete(teamMembership)
        return "Пользователь успешно удален из команды"
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
     * Обновляет информацию о пользователе.
     *
     * @param userId идентификатор пользователя
     * @param firstName новое имя пользователя
     * @param lastName новая фамилия пользователя
     * @return обновленный пользователь
     * @throws Exception если пользователь не найден
     */
    fun updateUser(userId: Long, fullname: String?, role: Role?): User {
        val user = userRepository.findById(userId).orElseThrow { Exception("Пользователь не найден") }
        if (fullname!=null)
            user.fullname = fullname
        if (role!=null)
            user.role = role
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
     * Получает все команды, в которых состоит пользователь.
     *
     * @param userId идентификатор пользователя
     * @return список команд, в которых состоит пользователь
     */
    fun getTeamsByUser(userId: Long): List<Team> {
        val user = getUserById(userId)
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
