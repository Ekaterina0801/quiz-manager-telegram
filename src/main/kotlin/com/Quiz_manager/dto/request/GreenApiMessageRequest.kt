package com.Quiz_manager.dto.request

import com.fasterxml.jackson.annotation.JsonProperty


class GreenApiMessageRequest {
    @JsonProperty("chatId")
    var chatId: String? = null

    @JsonProperty("message")
    var message: String? = null

    constructor()

    constructor(chatId: String?, message: String?) {
        this.chatId = chatId
        this.message = message
    }
}
