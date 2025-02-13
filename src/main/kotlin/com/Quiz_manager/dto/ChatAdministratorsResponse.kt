package com.Quiz_manager.dto

import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember

data class ChatAdministratorsResponse(
    val ok: Boolean,
    val result: List<ChatMember>
)