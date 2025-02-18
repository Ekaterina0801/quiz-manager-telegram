package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamNotificationSettings
import com.Quiz_manager.dto.request.TeamNotificationSettingsCreationDto

fun TeamNotificationSettings.toDto(): TeamNotificationSettingsCreationDto {
    return TeamNotificationSettingsCreationDto(
        id = this.id,
        teamId = this.team.id ?: throw IllegalStateException("Team ID cannot be null"),
        registrationNotificationEnabled = this.registrationNotificationEnabled,
        unregisterNotificationEnabled = this.unregisterNotificationEnabled,
        eventReminderEnabled = this.eventReminderEnabled,
        registrationReminderHoursBeforeEvent = this.registrationReminderHoursBeforeEvent
    )
}

fun TeamNotificationSettingsCreationDto.toEntity(team: Team): TeamNotificationSettings {
    return TeamNotificationSettings(
        id = this.id,
        team = team,
        registrationNotificationEnabled = this.registrationNotificationEnabled,
        unregisterNotificationEnabled = this.unregisterNotificationEnabled,
        eventReminderEnabled = this.eventReminderEnabled,
        registrationReminderHoursBeforeEvent = this.registrationReminderHoursBeforeEvent
    )
}

