package com.Quiz_manager.domain

import jakarta.persistence.*
import jakarta.persistence.GenerationType.*
import org.hibernate.Hibernate
import org.hibernate.proxy.HibernateProxy

@Entity
data class TeamNotificationSettings(
    @Id @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    val team: Team,

    var registrationNotificationEnabled: Boolean = true,
    var unregisterNotificationEnabled: Boolean = true,
    var eventReminderEnabled: Boolean = true,
    var registrationReminderHoursBeforeEvent: Int = 24
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as TeamNotificationSettings
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id )"
    }
}
