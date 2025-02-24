package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.response.TeamMembershipResponseDto

fun TeamMembership.toDto(): TeamMembershipResponseDto {
    return TeamMembershipResponseDto(
        id = this.id,
        userId = this.user.id,
        teamId = this.team!!.id,
        role = this.role
    )
}

fun TeamMembershipResponseDto.toEntity(user: User, team: Team): TeamMembership {
    return TeamMembership(
        id = this.id,
        user = user,
        team = team,
        role = this.role
    )
}
