package com.Quiz_manager.service

import com.Quiz_manager.dto.request.TeamNotificationSettingsCreationDto
import com.Quiz_manager.mapper.toDto
import com.Quiz_manager.mapper.toEntity
import com.Quiz_manager.repository.TeamNotificationSettingsRepository
import com.Quiz_manager.repository.TeamRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TeamNotificationSettingsService(
    private val teamNotificationSettingsRepository: TeamNotificationSettingsRepository,
    private val teamRepository: TeamRepository
) {

    private val logger = LoggerFactory.getLogger(TeamNotificationSettingsService::class.java)

    /**
     * Получает настройки уведомлений для команды.
     */
    fun getSettingsForTeam(teamId: Long): TeamNotificationSettingsCreationDto {
        val team = teamRepository.findById(teamId)
            .orElseThrow { EntityNotFoundException("Команда с id $teamId не найдена") }

        val settings = teamNotificationSettingsRepository.findByTeamId(teamId)
            ?: throw EntityNotFoundException("Настройки уведомлений не найдены для команды с id $teamId")

        return settings.toDto()
    }

    /**
     * Создает или обновляет настройки уведомлений для команды.
     */
    @Transactional
    fun createOrUpdateSettings(teamId: Long, settingsDTO: TeamNotificationSettingsCreationDto): TeamNotificationSettingsCreationDto {
        val team = teamRepository.findById(teamId)
            .orElseThrow { EntityNotFoundException("Команда с id $teamId не найдена") }

        val updatedSettings = teamNotificationSettingsRepository.findByTeamId(teamId)?.apply {
            registrationNotificationEnabled = settingsDTO.registrationNotificationEnabled
            unregisterNotificationEnabled = settingsDTO.unregisterNotificationEnabled
            eventReminderEnabled = settingsDTO.eventReminderEnabled
            registrationReminderHoursBeforeEvent = settingsDTO.registrationReminderHoursBeforeEvent
        } ?: teamNotificationSettingsRepository.save(settingsDTO.toEntity(team))

        return updatedSettings.toDto()
    }

    /**
     * Удаляет настройки уведомлений для команды.
     */
    @Transactional
    fun deleteSettingsForTeam(teamId: Long) {

        val settings = teamNotificationSettingsRepository.findByTeamId(teamId)
            ?: throw EntityNotFoundException("Настройки уведомлений не найдены для команды с id $teamId")

        teamNotificationSettingsRepository.delete(settings)
        logger.info("Удалены настройки уведомлений для команды с id $teamId")
    }
}
