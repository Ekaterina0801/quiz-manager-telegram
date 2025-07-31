package com.Quiz_manager.service

import com.Quiz_manager.domain.PasswordResetToken
import com.Quiz_manager.domain.User
import com.Quiz_manager.repository.PasswordResetTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Transactional  // Add class-level annotation or method-level as shown below
class PasswordResetTokenService(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val userService: UserService
) {
    companion object {
        private const val EXPIRATION_HOURS = 24L
    }

    @Transactional
    fun createToken(user: User): PasswordResetToken {
        // Explicitly flush the delete operation
        passwordResetTokenRepository.deleteByUser(user)
        passwordResetTokenRepository.flush()  // Force immediate execution

        val token = UUID.randomUUID().toString()
        val expiryDate = Instant.now().plus(EXPIRATION_HOURS, ChronoUnit.HOURS)

        return passwordResetTokenRepository.save(
            PasswordResetToken(
                token = token,
                user = user,
                expiryDate = expiryDate
            )
        )
    }

    @Transactional(readOnly = true)  // Optimize read-only operations
    fun validateToken(token: String): User? {
        val passToken = passwordResetTokenRepository.findByToken(token) ?: return null
        return if (passToken.isExpired()) null else passToken.user
    }
}