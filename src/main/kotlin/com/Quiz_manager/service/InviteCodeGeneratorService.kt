package com.Quiz_manager.service

import org.springframework.stereotype.Service

@Service
class InviteCodeGeneratorService {
    fun generateInviteCode(): String {
        return (1..6)
            .map { ('A'..'Z').random() }
            .joinToString("")
    }
}