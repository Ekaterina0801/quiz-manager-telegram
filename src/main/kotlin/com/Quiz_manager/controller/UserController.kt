package com.Quiz_manager.controller

import com.Quiz_manager.domain.Team
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.response.UserResponseDto
import com.Quiz_manager.enums.Role
import com.Quiz_manager.mapper.UserMapper
import com.Quiz_manager.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal


@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService, private val userMapper: UserMapper
) {



    @GetMapping("/me")
    fun getCurrentUser(principal: Principal?): ResponseEntity<UserResponseDto> {
        val user = userService.getCurrentUser(principal)
        return ResponseEntity.ok(userMapper.toDto(user))
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
        @RequestParam fullname: String?,
        @RequestParam role: Role?
    ): ResponseEntity<User> {
        return try {
            ResponseEntity.ok(userService.updateUser(userId, fullname, role))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    /**
     * Получает список всех команд, в которых состоит пользователь.
     */
    @GetMapping("/{userId}/teams")
    fun getTeamsByUser(@PathVariable userId: Long): ResponseEntity<List<Team>> {
        return ResponseEntity.ok(userService.getTeamsByUser(userId))
    }

    /**
     * Добавляет пользователя в команду.
     */
    @PostMapping("/{userId}/join")
    fun registerUserToTeam(
        @PathVariable userId: Long,
        @RequestParam inviteCode: String
    ): ResponseEntity<User?> {
        return ResponseEntity.ok(userService.registerUserToTeam(userId, inviteCode))
    }

    /**
     * Удаляет пользователя из команды.
     */
    @DeleteMapping("/{userId}/leave")
    fun removeUserFromTeam(
        @PathVariable userId: Long,
        @RequestParam inviteCode: String
    ): ResponseEntity<String> {
        return ResponseEntity.ok(userService.removeUserFromTeam(userId, inviteCode))
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
        return ResponseEntity.ok(userService.isUserModerator(userId, teamId))
    }

    @GetMapping("/{userId}/isAdmin")
    fun isUserMainAdmin(
        @PathVariable userId: Long
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok(userService.isUserAdmin(userId))
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


}

