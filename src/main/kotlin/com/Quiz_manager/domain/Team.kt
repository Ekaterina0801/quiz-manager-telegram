package com.Quiz_manager.domain

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import jakarta.persistence.*

@Entity
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
data class Team(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val name: String,

    val inviteCode: String,

    val chatId: String,

    @OneToMany(mappedBy = "team", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
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

