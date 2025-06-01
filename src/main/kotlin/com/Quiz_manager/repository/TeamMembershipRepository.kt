package com.Quiz_manager.repository

import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.enums.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

@Repository
interface TeamMembershipRepository : JpaRepository<TeamMembership, Long> {

    fun findByTeamId(teamId: Long): List<TeamMembership>

    fun findByUserId(userId: Long): List<TeamMembership>

    fun findByTeamIdAndUserId(teamId: Long, userId: Long): TeamMembership?

    fun findByTeamIdAndRole(teamId: Long, role: Role): List<TeamMembership>?

    /**
     * Проверяет, существует ли членство пользователя в команде с определенной ролью.
     *
     * @param team команда
     * @param user пользователь
     * @param role роль пользователя в команде
     * @return true, если членство с данной ролью существует, иначе false
     */
    fun existsByTeamIdAndUserIdAndRole(teamId: Long, userId: Long, role: Role): Boolean

    fun existsByTeamIdAndUserId(teamId: Long, userId: Long): Boolean


    @Query("SELECT tm FROM TeamMembership tm WHERE tm.team.chatId = :chatId")
    fun findByTeamChatId(chatId: Long): List<TeamMembership>


    @Modifying
    @Query("delete from TeamMembership m where m.team.id   = :teamId and m.user.id   = :userId")
    fun deleteByTeamIdAndUserId(
        @Param("teamId") teamId: Long,
        @Param("userId") userId: Long
    ): Int
}
