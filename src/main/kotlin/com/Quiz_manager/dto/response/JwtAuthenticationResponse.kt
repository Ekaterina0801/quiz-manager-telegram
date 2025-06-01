package com.Quiz_manager.dto.response

data class JwtAuthenticationResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val refreshTokenExpiresAt: Long = 0
)
