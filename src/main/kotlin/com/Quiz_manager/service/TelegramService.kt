package com.Quiz_manager.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TelegramService(
    private val telegramClient: TelegramBotClient,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(TelegramService::class.java)
        private const val TEST_MESSAGE = "🔍 Проверка подключения к Telegram-чаналу"
    }

    /**
     * Отправляет текстовое сообщение в Telegram-чат (канал или группу).
     *
     * @param rawChatId — ID чата или канала в формате строкового идентификатора (например "@my_channel" или "-123456789")
     * @param text      — тело сообщения (MarkdownV2 поддерживается)
     */
    fun sendMessageToChat(rawChatId: String?, text: String) {
        val chatId = normalizeChatId(rawChatId)
        if (chatId=="")
            return
        try {
            val resp = telegramClient.sendMessage(
                chatId    = chatId,
                text      = text,
                parseMode = "HTML"
            )
            if (!resp.ok) {
                logger.error("Telegram notification failed to $chatId: ${resp.description}")
            }
        } catch (ex: Exception) {
            logger.error("Exception while sending Telegram message to $chatId", ex)
        }
    }

    /**
     * Шлёт тестовое сообщение для проверки доступности чата.
     *
     * @param rawChatId — ID чата или канала
     * @return true, если бот успешно доставил сообщение (ответ ok=true), иначе false
     */
    fun pingChat(rawChatId: String?): Boolean {
        val chatId = normalizeChatId(rawChatId)
        return try {
            val resp = telegramClient.sendMessage(
                chatId    = chatId,
                text      = TEST_MESSAGE,
                parseMode = "HTML"
            )
            if (!resp.ok) {
                logger.warn("Ping to Telegram-chat $chatId failed: ${resp.description}")
            }
            resp.ok
        } catch (ex: Exception) {
            logger.error("Exception while pinging Telegram-chat $chatId", ex)
            false
        }
    }

    private fun normalizeChatId(rawChatId: String?): String {
        val id = rawChatId?.takeIf { it.isNotBlank() } ?: ""
        return id
    }
}
