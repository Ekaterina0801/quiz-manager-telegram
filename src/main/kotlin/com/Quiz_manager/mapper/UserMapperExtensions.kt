package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Registration
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.response.UserResponseDto

fun User.toDto(): UserResponseDto {
    return UserResponseDto(
        id = this.id,
        username = this.username,
        firstName = this.firstName,
        lastName = this.lastName,
        telegramId = this.telegramId
    )
}

fun UserResponseDto.toEntity(
    registrations: List<Registration> = emptyList(),
    teamMemberships: List<TeamMembership> = emptyList()
): User {
    return User(
        id = this.id,
        username = this.username,
        firstName = this.firstName,
        lastName = this.lastName,
        telegramId = this.telegramId,
        registrations = registrations,
        teamMemberships = teamMemberships
    )
}
