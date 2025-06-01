package com.Quiz_manager.domain

import jakarta.persistence.*
import jakarta.persistence.GenerationType.*
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
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as TeamNotificationSettings

        return id != null && id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id )"
    }
}
