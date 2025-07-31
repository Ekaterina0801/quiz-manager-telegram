package com.Quiz_manager.service

import io.micrometer.observation.Observation
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
) {
    fun sendPasswordResetEmail(to: String, username: String, resetUrl: String) {
        val helper = MimeMessageHelper(javaMailSender.createMimeMessage(), "UTF-8")
        helper.setTo(to)
        helper.setSubject("Восстановление пароля")
        helper.setFrom("ds.katrin@mail.ru")

        val context = Context().apply {
            setVariable("username", username)
            setVariable("resetUrl", resetUrl)
        }
        val htmlContent = templateEngine.process("email/password-reset", context)
        helper.setText(htmlContent, true)

        javaMailSender.send(helper.mimeMessage)
    }
}
