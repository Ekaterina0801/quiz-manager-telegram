package com.Quiz_manager.dto.response

import com.Quiz_manager.enums.Role

data class UserResponseDto (
    val id: Long,
    val username: String,
    val email: String,
    val password: String,
    val fullName: String,
    val role: Role
    )