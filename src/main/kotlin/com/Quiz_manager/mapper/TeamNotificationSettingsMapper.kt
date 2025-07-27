package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamNotificationSettings
import com.Quiz_manager.dto.request.TeamNotificationSettingsCreationDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface TeamNotificationSettingsMapper {

    @Mapping(source = "dto.id", target = "id")
    @Mapping(source = "team", target = "team")
    @Mapping(source = "dto.registrationNotificationEnabled", target = "registrationNotificationEnabled")
    @Mapping(source = "dto.unregisterNotificationEnabled", target = "unregisterNotificationEnabled")
    @Mapping(source = "dto.eventReminderEnabled", target = "eventReminderEnabled")
    @Mapping(source = "dto.registrationReminderHoursBeforeEvent", target = "registrationReminderHoursBeforeEvent")
    fun toEntity(dto: TeamNotificationSettingsCreationDto, team: Team): TeamNotificationSettings

    @Mapping(source = "team.id", target = "teamId")
    fun toDto(entity: TeamNotificationSettings): TeamNotificationSettingsCreationDto
}
