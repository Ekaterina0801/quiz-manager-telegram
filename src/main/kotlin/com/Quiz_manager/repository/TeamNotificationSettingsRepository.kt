package com.Quiz_manager.repository

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamNotificationSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamNotificationSettingsRepository : JpaRepository<TeamNotificationSettings, Long> {

    /**
     * Находит настройки уведомлений по команде.
     *
     * @param team команда
     * @return настройки уведомлений для команды, если они существуют
     */
    fun findByTeam(team: Team): TeamNotificationSettings?


    /**
     * Находит все настройки уведомлений для всех команд.
     *
     * @return список всех настроек уведомлений
     */
    fun findAllBy(): List<TeamNotificationSettings>

    /**
     * Удаляет настройки уведомлений по команде.
     *
     * @param team команда
     */
    fun deleteByTeam(team: Team)
}
