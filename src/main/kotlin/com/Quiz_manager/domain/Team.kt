package com.Quiz_manager.domain

import jakarta.persistence.*

@Entity
data class Team(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    var name: String,

    val inviteCode: String,

    var chatId: String?,

    @OneToMany(mappedBy = "team", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    val teamMemberships: MutableList<TeamMembership> = mutableListOf()
) {

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as Team
        return id == other.id
    }

    final override fun hashCode(): Int = id?.hashCode() ?: 0

    @Override
    override fun toString(): String = "Team(id=$id, name='$name', inviteCode='$inviteCode')"
}

