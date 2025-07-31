package com.Quiz_manager.repository

import com.Quiz_manager.domain.PasswordResetToken
import com.Quiz_manager.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {
    fun findByToken(token: String): PasswordResetToken?
    fun deleteByUser(user: User)
}