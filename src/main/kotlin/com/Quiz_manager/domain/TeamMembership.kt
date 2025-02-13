package com.Quiz_manager.domain

import com.Quiz_manager.enums.Role
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import jakarta.persistence.*

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
data class TeamMembership(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER
) {

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as TeamMembership
        return id != null && id == other.id
    }

    final override fun hashCode(): Int = id?.hashCode() ?: 0

    @Override
    override fun toString(): String = "TeamMembership(id=$id, user=${user.username}, team=${team.name}, role=$role)"
}
