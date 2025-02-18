package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.dto.response.TeamResponseDto

fun Team.toDto(): TeamResponseDto {
    return TeamResponseDto(
        id = this.id,
        name = this.name,
        inviteCode = this.inviteCode,
        chatId = this.chatId,
        teamMemberships = this.teamMemberships.map { it.toDto() }.toMutableList()
    )
}

fun TeamResponseDto.toEntity(teamMemberships: List<TeamMembership> = emptyList()): Team {
    return Team(
        id = this.id,
        name = this.name,
        inviteCode = this.inviteCode,
        chatId = this.chatId,
        teamMemberships = teamMemberships.toMutableList()
    )
}
