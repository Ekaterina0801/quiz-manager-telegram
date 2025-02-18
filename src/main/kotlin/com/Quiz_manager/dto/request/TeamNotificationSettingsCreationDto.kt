package com.Quiz_manager.dto.request

data class TeamNotificationSettingsCreationDto(
    val id: Long? = null,
    val teamId: Long,
    val registrationNotificationEnabled: Boolean = true,
    val unregisterNotificationEnabled: Boolean = true,
    val eventReminderEnabled: Boolean = true,
    val registrationReminderHoursBeforeEvent: Int = 24
)
