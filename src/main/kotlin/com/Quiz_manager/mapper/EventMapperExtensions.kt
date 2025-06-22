package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Team
import com.Quiz_manager.dto.request.EventCreationDto
import com.Quiz_manager.dto.response.EventResponseDto
import com.Quiz_manager.service.CloudinaryService


object CloudinaryServiceSingleton {
    var cloudinaryService: CloudinaryService? = null
}

fun EventCreationDto.toEntity(team: Team): Event {
    val imageUrl = CloudinaryServiceSingleton.cloudinaryService?.let { imageFile.uploadImageIfPresent(cloudinaryService = it) }

    return Event(
        name = this.name!!,
        isRegistrationOpen = this.isRegistrationOpen!!,
        dateTime = this.dateTime!!,
        location = this.location!!,
        description = this.description,
        posterUrl = imageUrl,
        linkToAlbum = this.linkToAlbum,
        teamResult = this.teamResult,
        team = team,
        isHidden = this.isHidden,
        price = this.price
    )
}




fun Event.toResponseDto(): EventResponseDto {
    return EventResponseDto(
        id = this.id,
        name = this.name,
        dateTime = this.dateTime,
        location = this.location,
        description = this.description,
        posterUrl = this.posterUrl,
        linkToAlbum = this.linkToAlbum,
        teamResult = this.teamResult,
        teamId = this.team.id!!,
        isRegistrationOpen = this.isRegistrationOpen,
        price = this.price ?: "",
        registrations = this.registrations.map { it.toDto() }.toMutableList()
    )
}


