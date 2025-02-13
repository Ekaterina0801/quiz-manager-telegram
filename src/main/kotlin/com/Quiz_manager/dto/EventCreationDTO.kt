package com.Quiz_manager.dto

import org.springframework.web.multipart.MultipartFile
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class EventCreationDTO(
    val name: String,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val dateTime: LocalDateTime,

    val location: String,
    val description: String?,
    val linkToAlbum: String?,
    val teamResult: String?,

    val teamId: Long,
    val userId: Long,

    var imageFile: MultipartFile? = null
)
