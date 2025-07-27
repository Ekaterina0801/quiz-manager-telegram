package com.Quiz_manager.mapper

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.response.TeamMembershipResponseDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.*

@Mapper(componentModel = "spring", uses = [UserMapper::class], unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface TeamMembershipMapper {

    @Mapping(source = "dto.id", target = "id")
    @Mapping(source = "dto.role", target = "role")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "team", source = "team")
    fun toEntity(dto: TeamMembershipResponseDto, user: User, team: Team): TeamMembership

    @Mapping(source = "user", target = "user")
    @Mapping(source = "team.id", target = "teamId")
    fun toDto(teamMembership: TeamMembership): TeamMembershipResponseDto
}
