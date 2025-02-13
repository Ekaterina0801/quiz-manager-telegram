package com.Quiz_manager.controller

import com.Quiz_manager.domain.User
import com.Quiz_manager.service.TeamService
import com.Quiz_manager.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
@RestController
@RequestMapping("/api/admins")
class AdminController(private val userService: UserService, private val teamService: TeamService) {

    /**
     * Назначить пользователя администратором в рамках команды.
     *
     * Этот метод позволяет назначить пользователя администратором для конкретной команды.
     * Для этого вызывается метод `assignAdminRole`, который назначает роль администратора указанному пользователю
     * в рамках выбранной команды.
     *
     * @param userId ID пользователя, которого необходимо назначить администратором.
     * @param teamId ID команды, в рамках которой назначается роль администратора.
     * @return ResponseEntity с кодом состояния 200 OK и сообщением об успешном назначении,
     * если операция прошла успешно. В случае ошибки возвращается код состояния 400 Bad Request с сообщением об ошибке.
     */
    @PostMapping("/makeAdmin/{userId}/{teamId}")
    fun makeAdmin(@PathVariable userId: Long, @PathVariable teamId: Long): ResponseEntity<String> {
        return try {
            val resultMessage = userService.assignAdminRole(userId, teamId)
            ResponseEntity.ok(resultMessage)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error assigning admin role: ${e.message}")
        }
    }

    /**
     * Удалить роль администратора у пользователя в рамках команды.
     *
     * Этот метод позволяет удалить роль администратора у указанного пользователя для конкретной команды.
     * Для этого вызывается метод `revokeAdminRole`, который удаляет роль администратора у пользователя
     * в рамках указанной команды.
     *
     * @param userId ID пользователя, у которого необходимо удалить роль администратора.
     * @param teamId ID команды, из которой удаляется роль администратора.
     * @return ResponseEntity с кодом состояния 200 OK и сообщением об успешном удалении роли администратора,
     * если операция прошла успешно. В случае ошибки возвращается код состояния 400 Bad Request с сообщением об ошибке.
     */
    @PostMapping("/removeAdmin/{userId}/{teamId}")
    fun removeAdmin(@PathVariable userId: Long, @PathVariable teamId: Long): ResponseEntity<String> {
        return try {
            val resultMessage = userService.revokeAdminRole(userId, teamId)
            ResponseEntity.ok(resultMessage)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error removing admin role: ${e.message}")
        }
    }

    /**
     * Получить всех пользователей с ролью администратора для команды.
     *
     * Этот метод позволяет получить всех пользователей, которые имеют роль администратора в рамках конкретной команды.
     *
     * @param teamId ID команды.
     * @return ResponseEntity с кодом состояния 200 OK и списком пользователей-администраторов.
     */
    @GetMapping("/getAdmins/{teamId}")
    fun getAdmins(@PathVariable teamId: Long): ResponseEntity<List<User>> {
        return try {
            val admins = userService.getAdminsByTeam(teamId)
            ResponseEntity.ok(admins)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyList())
        }
    }
}
