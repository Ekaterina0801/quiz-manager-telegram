package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Registration
import com.Quiz_manager.domain.Team
import com.Quiz_manager.dto.request.EventCreationDto
import com.Quiz_manager.dto.response.EventResponseDto
import com.Quiz_manager.dto.response.RegistrationResponseDto
import org.mapstruct.*


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.name", target = "name")
    @Mapping(source = "dto.description", target = "description")
    @Mapping(source = "dto.dateTime", target = "dateTime")
    @Mapping(source = "dto.linkToAlbum", target = "linkToAlbum")
    @Mapping(source = "dto.teamResult", target = "teamResult")
    @Mapping(source = "dto.location", target = "location")
    @Mapping(source = "dto.registrationOpen", target = "registrationOpen")
    @Mapping(source = "dto.hidden", target = "hidden")
    @Mapping(source = "dto.price", target = "price")
    @Mapping(source = "team", target = "team")
    @Mapping(target = "posterUrl", ignore = true)
    @Mapping(target = "registrations", expression = "java(new java.util.ArrayList<>())")
    fun toEntity(dto: EventCreationDto, team: Team): Event


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "dto.name", target = "name")
    @Mapping(source = "dto.description", target = "description")
    @Mapping(source = "dto.dateTime", target = "dateTime")
    @Mapping(source = "dto.linkToAlbum", target = "linkToAlbum")
    @Mapping(source = "dto.teamResult", target = "teamResult")
    @Mapping(source = "dto.location", target = "location")
    @Mapping(source = "dto.registrationOpen", target = "registrationOpen")
    @Mapping(source = "dto.hidden", target = "hidden")
    @Mapping(source = "dto.price", target = "price")
    @Mapping(target = "posterUrl", expression = "java(posterUrl)")
    fun updateEventFromDto(dto: EventCreationDto, @MappingTarget event: Event, posterUrl: String?): Event

    // Маппинг сущности в DTO
    @Mapping(source = "event.id", target = "id")
    @Mapping(source = "event.name", target = "name")
    @Mapping(source = "event.dateTime", target = "dateTime")
    @Mapping(source = "event.location", target = "location")
    @Mapping(source = "event.description", target = "description")
    @Mapping(source = "event.posterUrl", target = "posterUrl")
    @Mapping(source = "event.linkToAlbum", target = "linkToAlbum")
    @Mapping(source = "event.teamResult", target = "teamResult")
    @Mapping(source = "event.registrationOpen", target = "registrationOpen")
    @Mapping(source = "event.team.id", target = "teamId")
    @Mapping(source = "event.price", target = "price")
    @Mapping(source = "event.hidden", target = "hidden")
    @Mapping(source = "event.registrations", target = "registrations")
    fun toResponseDto(event: Event, @Context isRegistered: Boolean = false): EventResponseDto

    fun map(registrations: List<Registration>): MutableList<RegistrationResponseDto>
}
