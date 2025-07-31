package com.Quiz_manager.dto.request

import org.springframework.web.multipart.MultipartFile
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
data class EventCreationDto(

    var name: String? = null,

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    var dateTime: LocalDateTime? = null,

    var location: String? = null,
    var description: String? = null,
    var linkToAlbum: String? = null,
    var teamResult: String? = null,

    var teamId: Long? = null,
    var userId: Long? = null,
    var isRegistrationOpen: Boolean? = null,
    var imageFile: MultipartFile? = null,
    var isHidden: Boolean = false,
    var price: String? = null,
    var limitOfRegistrations: Long? = null
)
