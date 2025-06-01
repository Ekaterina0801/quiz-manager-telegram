package com.Quiz_manager.service

import com.Quiz_manager.dto.response.GreenApiResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Component
class GreenApiRestClient(
    private val rest: RestTemplate,
    @Value("\${greenapi.base-url}") private val baseUrl: String,
    @Value("\${greenapi.instance-id}") private val instanceId: String,
    @Value("\${greenapi.token}") private val token: String
){

    fun sendMessage(chatId: String, text: String): GreenApiResponse {
        val url = "$baseUrl/waInstance$instanceId/sendMessage/$token"
        val body = mapOf(
            "chatId" to chatId,
            "message" to text
        )
        return try {
            rest.postForObject(url, body, GreenApiResponse::class.java)
                ?: GreenApiResponse(false, "Empty response")
        } catch (ex: HttpClientErrorException) {
            GreenApiResponse(false, ex.responseBodyAsString)
        }
    }
}
