package com.Quiz_manager.mapper

import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.response.UserResponseDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface UserMapper {

    @Mapping(source = "fullname", target = "fullName")
    fun toDto(entity: User): UserResponseDto

    @Mapping(source = "fullName", target = "fullname")
    @Mapping(target = "registrations", ignore = true)
    @Mapping(target = "teamMemberships", ignore = true)
    fun toEntity(dto: UserResponseDto): User
}

