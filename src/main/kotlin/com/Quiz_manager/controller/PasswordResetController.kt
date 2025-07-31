package com.Quiz_manager.controller

import com.Quiz_manager.service.EmailService
import com.Quiz_manager.service.PasswordResetTokenService
import com.Quiz_manager.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class PasswordResetController(
    private val userService: UserService,
    private val passwordResetTokenService: PasswordResetTokenService,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder
) {
    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestParam email: String): ResponseEntity<out Any> {
        val user = userService.findByEmail(email)
            ?: return ResponseEntity
                .badRequest()
                .body("Пользователь с таким email не найден")

        val token = passwordResetTokenService.createToken(user)
        val resetUrl = "http://localhost:3000/reset-password?token=${token.token}"

        // Передаём логин пользователя в письмо
        emailService.sendPasswordResetEmail(user.email, user.username, resetUrl)

        return ResponseEntity.ok(mapOf("message" to "Письмо с инструкциями отправлено на ваш email"))

    }


    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestParam token: String,
        @RequestParam newPassword: String
    ): ResponseEntity<out Any> {
        val user = passwordResetTokenService.validateToken(token)
            ?: return ResponseEntity.badRequest().body("Недействительный или просроченный токен")

        user.password = passwordEncoder.encode(newPassword)
        userService.updateUser(user.id, fullname = null, role = null)

        return ResponseEntity.ok(mapOf("message" to "Пароль успешно изменен"))

    }
}