package com.Quiz_manager.dto.response

import com.Quiz_manager.dto.TelegramUser
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TelegramUserResponse(
    val ok: Boolean,
    val result: TelegramUser?
)