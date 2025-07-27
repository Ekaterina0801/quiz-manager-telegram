package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.dto.response.TeamResponseDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring", uses = [TeamMembershipMapper::class])
interface TeamMapper {

    fun toDto(entity: Team): TeamResponseDto

    @Mapping(target = "teamMemberships", expression = "java(new java.util.ArrayList<>(teamMemberships))")
    fun toEntity(dto: TeamResponseDto, teamMemberships: List<TeamMembership>): Team
}