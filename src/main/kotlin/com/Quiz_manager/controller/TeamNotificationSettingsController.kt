package com.Quiz_manager.controller

import com.Quiz_manager.dto.request.TeamNotificationSettingsCreationDto
import com.Quiz_manager.service.TeamNotificationSettingsService
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
@RestController
@RequestMapping("/api/team-notifications")
class TeamNotificationSettingsController(
    private val teamNotificationSettingsService: TeamNotificationSettingsService
) {

    private val logger = LoggerFactory.getLogger(TeamNotificationSettingsController::class.java)

    /**
     * Получает настройки уведомлений для команды.
     */
    @GetMapping("/{teamId}")
    fun getSettingsForTeam(@PathVariable teamId: Long): ResponseEntity<TeamNotificationSettingsCreationDto> {
        return try {
            val settings = teamNotificationSettingsService.getSettingsForTeam(teamId)
            ResponseEntity.ok(settings)
        } catch (e: EntityNotFoundException) {
            logger.warn("Настройки уведомлений не найдены для команды $teamId")
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Ошибка при получении настроек уведомлений для команды $teamId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Создает или обновляет настройки уведомлений для команды.
     */
    @PostMapping("/{teamId}")
    fun createOrUpdateSettings(
        @PathVariable teamId: Long,
        @RequestBody settingsDTO: TeamNotificationSettingsCreationDto
    ): ResponseEntity<TeamNotificationSettingsCreationDto> {
        return try {
            val updatedSettings = teamNotificationSettingsService.createOrUpdateSettings(teamId, settingsDTO)
            ResponseEntity.ok(updatedSettings)
        } catch (e: EntityNotFoundException) {
            logger.warn("Команда $teamId не найдена")
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Ошибка при сохранении настроек уведомлений для команды $teamId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Удаляет настройки уведомлений для команды.
     */
    @DeleteMapping("/{teamId}")
    fun deleteSettings(@PathVariable teamId: Long): ResponseEntity<Void> {
        return try {
            teamNotificationSettingsService.deleteSettingsForTeam(teamId)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            logger.warn("Настройки уведомлений не найдены для команды $teamId")
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Ошибка при удалении настроек уведомлений для команды $teamId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
