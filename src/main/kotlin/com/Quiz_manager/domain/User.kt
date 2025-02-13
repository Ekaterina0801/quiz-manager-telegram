package com.Quiz_manager.domain


import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import jakarta.persistence.*

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val username: String,
    var firstName: String?,
    var lastName: String?,
    val telegramId: String,

    @OneToMany(mappedBy = "registrant", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val registrations: List<Registration> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val teamMemberships: List<TeamMembership> = mutableListOf()
) {

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as User
        return id == other.id
    }

    final override fun hashCode(): Int = id.hashCode()

    @Override
    override fun toString(): String = "User(id=$id, username='$username')"
}
