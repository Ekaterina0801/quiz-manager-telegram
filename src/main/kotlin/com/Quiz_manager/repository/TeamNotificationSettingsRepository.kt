package com.Quiz_manager.repository

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamNotificationSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamNotificationSettingsRepository : JpaRepository<TeamNotificationSettings, Long> {

    fun findByTeamId(teamId: Long): TeamNotificationSettings?

    fun deleteByTeamId(teamId: Long)
}
