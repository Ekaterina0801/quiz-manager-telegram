package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Registration
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.response.UserResponseDto

fun User.toDto(): UserResponseDto {
    return UserResponseDto(
        id = this.id,
        username = this.username,
        fullName = this.fullname,
        password = this.password,
        email = this.email,
        role = this.role
    )
}

fun UserResponseDto.toEntity(
    registrations: List<Registration> = emptyList(),
    teamMemberships: List<TeamMembership> = emptyList()
): User {
    return User(
        id = this.id,
        username = this.username,
        fullname = this.fullName,
        password = this.password,
        email = this.email,
        role = this.role,
        registrations = registrations
    )
}
