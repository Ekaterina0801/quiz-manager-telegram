package com.Quiz_manager.dto.response

data class UserResponseDto (
    val id: Long,
    val username: String,
    var firstName: String?,
    var lastName: String?,
    val telegramId: String, )