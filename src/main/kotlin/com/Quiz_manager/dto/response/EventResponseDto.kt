package com.Quiz_manager.dto.response
import java.time.LocalDateTime

data class EventResponseDto (
    val id: Long? = null,
    val name: String,
    val dateTime: LocalDateTime,
    val location: String,
    val description: String?,
    val posterUrl: String?,
    val linkToAlbum: String?,
    val teamResult: String?,
    val isRegistrationOpen: Boolean,
    val teamId: Long,
    val price: String?,
    val registrations: MutableList<RegistrationResponseDto> = mutableListOf(),
)