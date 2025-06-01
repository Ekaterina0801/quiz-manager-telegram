package com.Quiz_manager.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service


@Service
class WhatsAppNotificationService(
    private val greenApiClient: GreenApiRestClient,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(WhatsAppNotificationService::class.java)
        private const val TEST_MESSAGE = "🔍 Проверка подключения к WhatsApp-группе"
    }

    /**
     * Отправляет уведомление о событии в WhatsApp-группу.
     * @param rawGroupId  — код группы (из URL invite-link или уже с суффиксом @g.us)
     * @param text        — сам текст сообщения (Markdown-стиль поддерживается)
     */
    fun sendMessageToGroup(rawGroupId: String, text: String) {
        val chatId = normalizeGroupId(rawGroupId)

        val response = greenApiClient.sendMessage(
            chatId = chatId,
            text   = text
        )
        if (!response.success) {
            logger.error("WhatsApp notification failed to $chatId: ${response.errorMessage}")
        }
    }

    /**
     * Проверка доставки: шлёт в группу короткое тест-сообщение.
     * @return true, если Green API вернул success=true, иначе false
     */
    fun pingGroup(rawGroupId: String): Boolean {
        val chatId = normalizeGroupId(rawGroupId)

        return try {
            val resp = greenApiClient.sendMessage(
                chatId = chatId,
                text   = TEST_MESSAGE
            )
            if (!resp.success) {
                logger.warn("Ping to WhatsApp-group $chatId failed: ${resp.errorMessage}")
            }
            resp.success
        } catch (ex: Exception) {
            logger.error("Exception while pinging WhatsApp-group $chatId", ex)
            false
        }
    }

    private fun normalizeGroupId(rawGroupId: String): String =
        if (rawGroupId.endsWith("@g.us")) rawGroupId else "$rawGroupId@g.us"
}
