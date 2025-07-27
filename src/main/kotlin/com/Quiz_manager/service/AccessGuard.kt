package com.Quiz_manager.service

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Registration
import com.Quiz_manager.domain.User
import com.Quiz_manager.enums.Role
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

@Component
class AccessGuard(private val userService: UserService) {

    fun checkCanUnregister(user: User, registration: Registration, event: Event) {
        val isAdmin = user.role == Role.ADMIN
        val isModerator = userService.isUserModerator(user.id, event.team.id)
        val isOwner = registration.registrant.id == user.id

        if (!(isAdmin || isModerator || isOwner)) {
            throw AccessDeniedException("Недостаточно прав для удаления участника")
        }
    }

    fun checkIsModerator(userId: Long, teamId: Long) {
        if (!userService.isUserModerator(userId, teamId)) {
            throw AccessDeniedException("Вы не являетесь модератором этой команды")
        }
    }
}
