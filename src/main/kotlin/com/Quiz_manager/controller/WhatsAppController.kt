package com.Quiz_manager.controller

import com.Quiz_manager.service.TelegramService
import com.Quiz_manager.service.WhatsAppNotificationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/whatsapp")
class WhatsAppController(
    private val whatsAppNotificationService: WhatsAppNotificationService, private val telegramService: TelegramService
) {
    @GetMapping("/ping/{groupId}")
    fun ping(@PathVariable groupId: String): ResponseEntity<String> {
        return if (telegramService.pingChat(groupId)) {
            ResponseEntity.ok("✅ Пинг успешен, группа $groupId доступна")
        } else {
            ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body("❌ Не удалось отправить сообщение в группу $groupId")
        }
    }
}
