package com.Quiz_manager.dto.response
import com.fasterxml.jackson.annotation.JsonProperty
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
    @JsonProperty("isRegistrationOpen")
    val registrationOpen: Boolean,
    val teamId: Long,
    val price: String?,
    var isRegistered: Boolean = false,
    @JsonProperty("isHidden")
    val hidden: Boolean = false,
    val registrations: MutableList<RegistrationResponseDto> = mutableListOf(),
)