package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Registration
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.response.RegistrationResponseDto
import org.mapstruct.*
@Mapper(componentModel = "spring")
interface RegistrationMapper {

    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "registrant.id", target = "registrantId")
    fun toDto(reg: Registration): RegistrationResponseDto

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "fullName", source = "dto.fullName")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "registrant", source = "registrant")
    fun toEntity(dto: RegistrationResponseDto, event: Event, registrant: User): Registration
}
