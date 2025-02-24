package com.Quiz_manager.controller

import com.Quiz_manager.domain.*
import com.Quiz_manager.dto.request.TeamNotificationSettingsCreationDto
import com.Quiz_manager.dto.response.EventResponseDto
import com.Quiz_manager.dto.response.TeamMembershipResponseDto
import com.Quiz_manager.dto.response.TeamResponseDto
import com.Quiz_manager.dto.response.UserResponseDto
import com.Quiz_manager.enums.Role
import com.Quiz_manager.mapper.toEntity
import com.Quiz_manager.service.EventService
import com.Quiz_manager.service.TeamService
import com.Quiz_manager.service.TelegramService
import com.Quiz_manager.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/teams")
class TeamController(private val teamService: TeamService, private val userService: UserService,
                     private val telegramService: TelegramService, private val eventService: EventService
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
        @RequestParam chatId: String
    ): ResponseEntity<TeamResponseDto> {
        return ResponseEntity.ok(teamService.createTeam(teamName, chatId))
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
        @RequestParam userId: String,
        @RequestParam role: Role
    ): ResponseEntity<TeamMembershipResponseDto> {
        return ResponseEntity.ok(telegramService.handleAddUserToTeam(userId, teamId, role))
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

    /**
     * Получить всех пользователей в команде.
     *
     * @param teamId ID команды.
     * @return список пользователей.
     */
    @GetMapping("/{teamId}/users")
    fun getAllUsersInTeam(@PathVariable teamId: Long): ResponseEntity<List<UserResponseDto>> {
        val team = teamService.getTeamById(teamId)
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
    fun getAllEventsByTeamId(@PathVariable teamId: Long): ResponseEntity<List<EventResponseDto>>{
        return ResponseEntity.ok(eventService.getEventsByTeamId(teamId))
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
