package com.Quiz_manager.controller

import com.Quiz_manager.dto.TelegramUser
import com.Quiz_manager.service.TelegramService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.telegram.telegrambots.meta.api.objects.Update

@RestController
@RequestMapping("/api/telegram")
class TelegramController(private val telegramService: TelegramService) {

    private val logger = LoggerFactory.getLogger(TelegramController::class.java)

    /**
     * Отправляет сообщение в Telegram-чат.
     *
     * @param chatId ID чата
     * @param message Текст сообщения
     * @return Статус операции
     */
    @PostMapping("/sendMessage")
    private fun sendMessage(
        @RequestParam chatId: String,
        @RequestParam message: String
    ): ResponseEntity<String> {
        return try {
            telegramService.sendMessageToChannel(chatId, message)
            ResponseEntity.ok("Сообщение отправлено в чат $chatId")
        } catch (e: Exception) {
            logger.error("Ошибка при отправке сообщения в Telegram", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка отправки сообщения")
        }
    }

    /**
     * Получает информацию о пользователе Telegram по его ID.
     *
     * @param telegramId ID пользователя Telegram
     * @return Информация о пользователе
     */
    @GetMapping("/getUserInfo")
    fun getUserInfo(@RequestParam telegramId: String): ResponseEntity<TelegramUser?> {
        return try {
            val userInfo = telegramService.getUserInfo(telegramId)
            if (userInfo != null) {
                ResponseEntity.ok(userInfo)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Ошибка при получении информации о пользователе Telegram", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }




    /**
     * Обрабатывает входящее сообщение из Telegram Webhook.
     *
     * @param update Объект обновления от Telegram
     * @return HTTP 200 OK
     */
    @PostMapping("/webhook")
    fun handleTelegramUpdate(@RequestBody update: Update): ResponseEntity<String> {
        return try {
            val user = telegramService.findOrCreateUser(update)
            logger.info("Обработан новый пользователь: ${user.username}")
            ResponseEntity.ok("Update обработан")
        } catch (e: Exception) {
            logger.error("Ошибка обработки обновления Telegram", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка обработки обновления")
        }
    }
}

