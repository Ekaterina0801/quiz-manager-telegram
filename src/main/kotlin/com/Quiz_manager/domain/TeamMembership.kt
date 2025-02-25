package com.Quiz_manager.domain

import com.Quiz_manager.enums.Role
import jakarta.persistence.*
import org.hibernate.annotations.Cascade

@Entity
data class TeamMembership(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(optional = true)
    @JoinColumn(name = "team_id", nullable = true)
    var team: Team?,

    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER
) {

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as TeamMembership
        return id != null && id == other.id;
    }

    final override fun hashCode(): Int = id?.hashCode() ?: 0

    @Override
    override fun toString(): String = "TeamMembership(id=$id, user=${user.username}, team=${team!!.name}, role=$role)"
}
