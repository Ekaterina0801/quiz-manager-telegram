package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Registration
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.response.RegistrationResponseDto

fun Registration.toDto(): RegistrationResponseDto {
    return RegistrationResponseDto(
        id = this.id,
        fullName = this.fullName,
        eventId = this.event.id!!,
        registrantId = this.registrant.id!!
    )
}

fun RegistrationResponseDto.toEntity(event: Event, registrant: User): Registration {
    return Registration(
        id = this.id,
        fullName = this.fullName,
        event = event,
        registrant = registrant
    )
}

