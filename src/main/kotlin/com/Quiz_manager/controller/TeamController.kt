package com.Quiz_manager.controller

import com.Quiz_manager.domain.*
import com.Quiz_manager.dto.request.TeamNotificationSettingsCreationDto
import com.Quiz_manager.dto.response.EventResponseDto
import com.Quiz_manager.dto.response.TeamResponseDto
import com.Quiz_manager.dto.response.UserResponseDto
import com.Quiz_manager.enums.Role
import com.Quiz_manager.service.EventService
import com.Quiz_manager.service.TeamService
import com.Quiz_manager.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/teams")
class TeamController(private val teamService: TeamService, private val userService: UserService,
                    private val eventService: EventService
) {

    /**
     * Создать новую команду.
     *
     * @param teamName название команды.
     * @param chatId ID чата команды.
     * @return созданная команда.
     */
    @PostMapping
    fun createTeam(
        @RequestParam teamName: String,
        @RequestParam chatId: String?,
        @RequestParam userId: Long
    ): ResponseEntity<TeamResponseDto> {
        return ResponseEntity.ok(teamService.createTeam(teamName, chatId, userId))
    }

    @GetMapping
    fun getTeams(): ResponseEntity<List<TeamResponseDto>> {
        return ResponseEntity.ok(teamService.getTeams())
    }

    /**
     * Получить команду по ID.
     *
     * @param teamId ID команды.
     * @return команда.
     */
    @GetMapping("/{teamId}")
    fun getTeamById(@PathVariable teamId: Long): ResponseEntity<TeamResponseDto> {
        return ResponseEntity.ok(teamService.getTeamById(teamId))
    }

    /**
     * Получить команду по коду приглашения.
     *
     * @param inviteCode код приглашения.
     * @return команда.
     */
    @GetMapping("/invite/{inviteCode}")
    fun getTeamByInviteCode(@PathVariable inviteCode: String): ResponseEntity<TeamResponseDto> {
        val team = teamService.getTeamByCode(inviteCode)
        return if (team != null) {
            ResponseEntity.ok(team)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Добавить пользователя в команду.
     *
     * @param teamId ID команды.
     * @param userId ID пользователя.
     * @param role роль пользователя (ADMIN, MEMBER).
     * @return информация о членстве пользователя в команде.
     */
    @PostMapping("/{teamId}/addUser")
    fun addUserToTeam(
        @PathVariable teamId: Long,
        @RequestParam userId: Long,
        @RequestParam role: Role
    ): ResponseEntity<String> {
        teamService.addUserToTeam(userId, teamId, role)
        return ResponseEntity.ok("Пользователь успешно добавлен в команду")
    }

    @PutMapping("/{teamId}/updateUserRole")
    fun updateUserRole(
        @PathVariable teamId: Long,
        @RequestParam userId: Long,
        @RequestParam role: Role
    ): ResponseEntity<String> {
        val result = userService.updateUserRole(userId, teamId, role)
        return ResponseEntity.ok(result)
    }

    /**
     * Удалить пользователя из команды.
     *
     * @param teamId ID команды.
     * @param userId ID пользователя.
     */
    @DeleteMapping("/{teamId}/removeUser")
    fun removeUserFromTeam(
        @PathVariable teamId: Long,
        @RequestParam userId: Long
    ): ResponseEntity<String> {
        teamService.removeUserFromTeam(userId, teamId)
        return ResponseEntity.ok("Пользователь успешно удален из команды")
    }

    @PutMapping("/{teamId}")
    fun updateTeam(
        @PathVariable teamId: Long,
        @RequestParam(required = false) newName: String,
        @RequestParam(required = false) newChatId: String?,
        @RequestParam userId: Long
    ): ResponseEntity<TeamResponseDto> {
        val updated = teamService.updateTeam(teamId, newName, newChatId, userId)
        return ResponseEntity.ok(updated)
    }

    /**
     * Получить всех пользователей в команде.
     *
     * @param teamId ID команды.
     * @return список пользователей.
     */
    @GetMapping("/{teamId}/users")
    fun getAllUsersInTeam(@PathVariable teamId: Long): ResponseEntity<List<UserResponseDto>> {
        return ResponseEntity.ok(teamService.getAllUsersInTeam(teamId))
    }

    /**
     * Получить все команды, в которых состоит пользователь.
     *
     * @param userId ID пользователя.
     * @return список команд.
     */
    @GetMapping("/user/{userId}")
    fun getAllTeamsByUser(@PathVariable userId: Long): ResponseEntity<List<TeamResponseDto>> {
        return ResponseEntity.ok(teamService.getAllTeamsByUser(userId))
    }

    /**
     * Получить настройки уведомлений для команды.
     *
     * @param teamId ID команды.
     * @return настройки уведомлений.
     */
    @GetMapping("/{teamId}/notifications")
    fun getTeamNotificationSettings(@PathVariable teamId: Long): ResponseEntity<TeamNotificationSettings> {
        return ResponseEntity.ok(teamService.getTeamNotificationSettings(teamId))
    }

    @GetMapping("/{teamId}/events")
    fun getAllEventsByTeamId(
        @PathVariable teamId: Long,
        pageable: Pageable,
        @RequestParam(required = false) search: String?,
        principal: Principal
    ): ResponseEntity<Page<EventResponseDto>> {
        val currentUser = userService.getCurrentUser(principal)
        val page = eventService.getEventsByTeam(teamId, pageable, search, currentUser.id)
        return ResponseEntity.ok(page)
    }

    @DeleteMapping("/{teamId}")
    fun deleteTeamById(@PathVariable teamId: Long): ResponseEntity<String>
    {
        teamService.deleteTeamById(teamId)
        return ResponseEntity.ok("Команда с id ${teamId} успешно удалена")
    }

    /**
     * Обновить настройки уведомлений команды.
     *
     * @param teamId ID команды.
     * @param updatedSettings обновленные настройки уведомлений.
     * @param userId ID пользователя.
     * @return обновленные настройки.
     */
    @PutMapping("/{teamId}/notifications")
    fun updateTeamNotificationSettings(
        @PathVariable teamId: Long,
        @RequestBody updatedSettings: TeamNotificationSettings,
        @RequestParam userId: Long
    ): ResponseEntity<TeamNotificationSettingsCreationDto> {
        return ResponseEntity.ok(teamService.updateTeamNotificationSettings(teamId, updatedSettings, userId))
    }
}
