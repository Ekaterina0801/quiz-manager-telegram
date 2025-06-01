package com.Quiz_manager.dto.response

import com.Quiz_manager.enums.Role
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

data class TeamMembershipResponseDto (
    val id: Long? = null,
    val user: UserResponseDto,
    val teamId: Long,
    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER
)