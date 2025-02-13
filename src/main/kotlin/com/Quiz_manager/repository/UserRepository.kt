package com.Quiz_manager.repository

import com.Quiz_manager.domain.User
import com.Quiz_manager.enums.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>

    fun findByTelegramId(telegramId: String): User?

}
