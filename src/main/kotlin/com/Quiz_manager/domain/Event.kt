package com.Quiz_manager.domain

import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
data class Event(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(columnDefinition = "TEXT COLLATE NOCASE")
    var name: String,
    var dateTime: LocalDateTime,
    var location: String,
    var description: String?,
    var posterUrl: String?,
    var linkToAlbum: String?,
    var teamResult: String?,
    @Column(name="isRegistrationOpen")
    var registrationOpen: Boolean,
    @Column(name="isHidden")
    var hidden: Boolean,
    var price: String?,

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    var team: Team,

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var registrations: MutableList<Registration> = mutableListOf(),
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
