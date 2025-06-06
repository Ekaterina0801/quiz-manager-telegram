package com.Quiz_manager.domain

import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
data class Event(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(columnDefinition = "TEXT COLLATE NOCASE")
    val name: String,
    val dateTime: LocalDateTime,
    val location: String,
    val description: String?,
    val posterUrl: String?,
    val linkToAlbum: String?,
    val teamResult: String?,
    val isRegistrationOpen: Boolean,
    val isHidden: Boolean,
    val price: String?,

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    val registrations: MutableList<Registration> = mutableListOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as Event
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String {
        return "Event(id=$id, name='$name')"
    }
}
