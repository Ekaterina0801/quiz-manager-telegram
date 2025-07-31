package com.Quiz_manager.domain;

import jakarta.persistence.*
import java.time.Instant

@Entity
class PasswordResetToken(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        val token: String,

        @OneToOne(targetEntity = User::class, fetch = FetchType.EAGER)
        @JoinColumn(nullable = false, name = "user_id")
        val user: User,

        val expiryDate: Instant
) {
    fun isExpired(): Boolean {
        return expiryDate.isBefore(Instant.now())
    }
}
