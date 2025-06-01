package com.Quiz_manager.dto.response

data class TelegramSendResponse(
    val ok: Boolean,
    val description: String?,
    val result: Any?
)