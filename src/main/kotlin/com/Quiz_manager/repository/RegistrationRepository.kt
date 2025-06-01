package com.Quiz_manager.repository

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Registration
import com.Quiz_manager.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface RegistrationRepository : JpaRepository<Registration, Long> {
    fun findByEventIdAndFullName(eventId: Long, fullName: String): Registration?

    fun findByEventId(eventId: Long): List<Registration>
}
