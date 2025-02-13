package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamNotificationSettings
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.TeamNotificationSettingsDTO
import com.Quiz_manager.dto.TelegramUser

fun TeamNotificationSettings.toDto(): TeamNotificationSettingsDTO {
    return TeamNotificationSettingsDTO(
        id = this.id,
        teamId = this.team.id ?: throw IllegalStateException("Team ID cannot be null"),
        registrationNotificationEnabled = this.registrationNotificationEnabled,
        unregisterNotificationEnabled = this.unregisterNotificationEnabled,
        eventReminderEnabled = this.eventReminderEnabled,
        registrationReminderHoursBeforeEvent = this.registrationReminderHoursBeforeEvent
    )
}

fun TeamNotificationSettingsDTO.toEntity(team: Team): TeamNotificationSettings {
    return TeamNotificationSettings(
        id = this.id,
        team = team,
        registrationNotificationEnabled = this.registrationNotificationEnabled,
        unregisterNotificationEnabled = this.unregisterNotificationEnabled,
        eventReminderEnabled = this.eventReminderEnabled,
        registrationReminderHoursBeforeEvent = this.registrationReminderHoursBeforeEvent
    )
}

