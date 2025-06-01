package com.Quiz_manager.service

import com.Quiz_manager.dto.response.TelegramSendResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate


@Component
class TelegramBotClient(
    private val restTemplate: RestTemplate,
    @Value("\${telegram.bot-token}") private val botToken: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val baseUrl = "https://api.telegram.org/bot$botToken"

    private fun buildUrl(method: String) = "$baseUrl/$method"

    /**
     * Sends a message via Telegram Bot API.
     *
     * @param chatId the target chat identifier (e.g. "@channelusername" or "-1001234567890")
     * @param text the message text
     * @param parseMode optional parse mode ("MarkdownV2", "HTML", etc.)
     */
    fun sendMessage(chatId: String, text: String, parseMode: String? = null): TelegramSendResponse {
        val url = buildUrl("sendMessage")
        val payload = mutableMapOf<String, Any>(
            "chat_id" to chatId,
            "text" to text
        )
        parseMode?.let { payload["parse_mode"] = it }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(payload, headers)

        return try {
            val resp: ResponseEntity<TelegramSendResponse> =
                restTemplate.postForEntity(url, request, TelegramSendResponse::class.java)
            resp.body ?: TelegramSendResponse(false, "Empty response", null)
        } catch (ex: Exception) {
            logger.error("Failed to send Telegram message to $chatId", ex)
            TelegramSendResponse(false, ex.message, null)
        }
    }

    /**
     * Ping the chat by sending a test message.
     * @return true if the API returns ok=true
     */
    fun pingChat(chatId: String): Boolean {
        val testText = "🔍 Проверка подключения к Telegram-чату"
        val resp = sendMessage(chatId, testText)
        if (!resp.ok) {
            logger.warn("Ping to Telegram chat $chatId failed: ${resp.description}")
        }
        return resp.ok
    }
}