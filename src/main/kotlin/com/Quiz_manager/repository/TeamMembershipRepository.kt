package com.Quiz_manager.repository

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.User
import com.Quiz_manager.enums.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.Query
@Repository
interface TeamMembershipRepository : JpaRepository<TeamMembership, Long> {

    fun findByTeam(team: Team): List<TeamMembership>

    fun findByUser(user: User): List<TeamMembership>

    fun findByTeamAndUser(team: Team, user: User): TeamMembership?

    fun findByTeamAndRole(team: Team, role: Role): List<TeamMembership>?

    /**
     * Проверяет, существует ли членство пользователя в команде с определенной ролью.
     *
     * @param team команда
     * @param user пользователь
     * @param role роль пользователя в команде
     * @return true, если членство с данной ролью существует, иначе false
     */
    fun existsByTeamAndUserAndRole(team: Team, user: User, role: Role): Boolean

    fun existsByTeamAndUser(team: Team, user: User): Boolean


    @Query("SELECT tm FROM TeamMembership tm WHERE tm.team.chatId = :chatId")
    fun findByTeamChatId(chatId: Long): List<TeamMembership>
}
