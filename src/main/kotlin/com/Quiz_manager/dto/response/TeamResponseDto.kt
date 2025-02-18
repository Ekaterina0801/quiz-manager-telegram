package com.Quiz_manager.dto.response

data class TeamResponseDto (
    val id: Long,
    val name: String,

    val inviteCode: String,

    val chatId: String,

    val teamMemberships: MutableList<TeamMembershipResponseDto> = mutableListOf()
)
