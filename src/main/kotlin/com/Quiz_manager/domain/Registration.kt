package com.Quiz_manager.domain

import jakarta.persistence.*
import org.hibernate.Hibernate
import org.hibernate.proxy.HibernateProxy

@Entity
data class Registration(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val fullName: String,

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "registrant_id", nullable = false)
    val registrant: User,

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Registration
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id )"
    }
}
