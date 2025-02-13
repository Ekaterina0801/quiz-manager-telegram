package com.Quiz_manager.controller

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.User
import com.Quiz_manager.enums.Role
import com.Quiz_manager.service.TelegramService
import com.Quiz_manager.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val telegramService: TelegramService
) {

    /**
     * Создает пользователя по Telegram ID.
     */
    @PostMapping("/create/{telegramId}")
    fun createUserByTelegramId(@PathVariable telegramId: String): ResponseEntity<User> {
        return try {
            val user = userService.createUserByTelegramId(telegramId)
            ResponseEntity.ok(user)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Получает пользователя по его ID.
     */
    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: Long): ResponseEntity<User> {
        return try {
            ResponseEntity.ok(userService.getUserById(userId))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    /**
     * Получает пользователя по Telegram ID.
     */
    @GetMapping("/telegram/{telegramId}")
    fun getUserByTelegramId(@PathVariable telegramId: String): ResponseEntity<User?> {
        return ResponseEntity.ok(userService.getUserByTelegramId(telegramId))
    }


    /**
     * Получает всех пользователей.
     */
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<User>> {
        return ResponseEntity.ok(userService.getAllUsers())
    }

    /**
     * Удаляет пользователя по ID.
     */
    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<String> {
        return try {
            ResponseEntity.ok(userService.deleteUser(userId))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ошибка: ${e.message}")
        }
    }

    /**
     * Обновляет информацию о пользователе.
     */
    @PutMapping("/{userId}/update")
    fun updateUserInfo(
        @PathVariable userId: Long,
        @RequestParam firstName: String?,
        @RequestParam lastName: String?
    ): ResponseEntity<User> {
        return try {
            ResponseEntity.ok(userService.updateUserInfo(userId, firstName, lastName))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    /**
     * Получает список всех команд, в которых состоит пользователь.
     */
    @GetMapping("/{telegramId}/teams")
    fun getTeamsByUser(@PathVariable telegramId: String): ResponseEntity<List<Team>> {
        return ResponseEntity.ok(userService.getTeamsByUser(telegramId))
    }

    /**
     * Добавляет пользователя в команду.
     */
    @PostMapping("/{telegramId}/join")
    fun registerUserToTeam(
        @PathVariable telegramId: String,
        @RequestParam inviteCode: String
    ): ResponseEntity<User?> {
        return ResponseEntity.ok(userService.registerUserToTeam(telegramId, inviteCode))
    }

    /**
     * Удаляет пользователя из команды.
     */
    @DeleteMapping("/{telegramId}/leave")
    fun removeUserFromTeam(
        @PathVariable telegramId: String,
        @RequestParam inviteCode: String
    ): ResponseEntity<String> {
        return ResponseEntity.ok(userService.removeUserFromTeam(telegramId, inviteCode))
    }

    /**
     * Получает всех участников команды.
     */
    @GetMapping("/team/{teamId}/members")
    fun getAllMembersOfTeam(@PathVariable teamId: Long): ResponseEntity<List<User>> {
        return ResponseEntity.ok(userService.getAllMembersOfTeam(teamId))
    }

    /**
     * Проверяет, является ли пользователь администратором команды.
     */
    @GetMapping("/{userId}/team/{teamId}/isAdmin")
    fun isUserAdmin(
        @PathVariable userId: Long,
        @PathVariable teamId: Long
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok(userService.isUserAdmin(userId, teamId))
    }

    /**
     * Получает роль пользователя в команде.
     */
    @GetMapping("/{userId}/team/{teamId}/role")
    fun getUserRoleInTeam(
        @PathVariable userId: Long,
        @PathVariable teamId: Long
    ): ResponseEntity<Role> {
        return ResponseEntity.ok(userService.getUserRoleInTeam(userId, teamId))
    }

    /**
     * Обновляет роль пользователя в команде.
     */
    @PutMapping("/{userId}/team/{teamId}/updateRole")
    fun updateUserRole(
        @PathVariable userId: Long,
        @PathVariable teamId: Long,
        @RequestParam newRole: Role
    ): ResponseEntity<String> {
        return ResponseEntity.ok(userService.updateUserRole(userId, teamId, newRole))
    }

    /**
     * Назначает пользователя администратором команды.
     */
    @PutMapping("/{userId}/team/{teamId}/assignAdmin")
    fun assignAdminRole(
        @PathVariable userId: Long,
        @PathVariable teamId: Long
    ): ResponseEntity<String> {
        return ResponseEntity.ok(userService.assignAdminRole(userId, teamId))
    }

    /**
     * Убирает у пользователя права администратора.
     */
    @PutMapping("/{userId}/team/{teamId}/revokeAdmin")
    fun revokeAdminRole(
        @PathVariable userId: Long,
        @PathVariable teamId: Long
    ): ResponseEntity<String> {
        return ResponseEntity.ok(userService.revokeAdminRole(userId, teamId))
    }

    /**
     * Получает всех администраторов команды.
     */
    @GetMapping("/team/{teamId}/admins")
    fun getAdminsByTeam(@PathVariable teamId: Long): ResponseEntity<List<User>?> {
        return ResponseEntity.ok(userService.getAdminsByTeam(teamId))
    }
}

