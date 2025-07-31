package com.Quiz_manager.shedulers

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.TeamNotificationSettings
import com.Quiz_manager.repository.EventRepository
import com.Quiz_manager.repository.RegistrationRepository
import com.Quiz_manager.repository.TeamNotificationSettingsRepository
import com.Quiz_manager.service.EventService
import com.Quiz_manager.service.TelegramService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
@Service
class EventReminderScheduler(
    private val eventRepository: EventRepository,
    private val teamNotificationSettingsRepository: TeamNotificationSettingsRepository,
    private val eventService: EventService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val moscowZone = ZoneId.of("Europe/Moscow")

    @Scheduled(cron = "0 * * * * *")
    fun sendReminders() {
        val now = currentMoscowTime()

        teamNotificationSettingsRepository.findAll().forEach { settings ->
            if (!settings.eventReminderEnabled) return@forEach

            val offsets = setOf(
                settings.registrationReminderHoursBeforeEvent.toLong(),
                1L
            )
            offsets.forEach { hoursBefore ->
                checkAndSendReminder(now, settings, hoursBefore)
            }
        }
    }

    private fun checkAndSendReminder(now: LocalDateTime, settings: TeamNotificationSettings, hoursBefore: Long) {
        val reminderTime = now.plusHours(hoursBefore)
        val start = reminderTime.withSecond(0).withNano(0)
        val end = start.plusMinutes(1)

        val events = eventRepository.findByTeamIdAndDateTimeBetween(
            settings.team.id,
            start,
            end
        )

        events.forEach { event ->
            sendSummary(event)
            logger.info("Sent ${hoursBefore} hour(s) before reminder for event ${event.id}")
        }
    }

    private fun currentMoscowTime(): LocalDateTime =
        LocalDateTime.now(ZoneOffset.UTC)
            .atZone(ZoneOffset.UTC)
            .withZoneSameInstant(moscowZone)
            .toLocalDateTime()

    private fun sendSummary(event: Event) {
        event.team.chatId?.let { chatId ->
            try {
                eventService.sendEventSummary(event.id!!)
                logger.info("Successfully sent reminder for event ${event.id} to chat $chatId")
            } catch (ex: Exception) {
                logger.error("Failed to send reminder for event ${event.id}", ex)
            }
        } ?: logger.warn("No chatId for team ${event.team.id}, cannot send reminder")
    }
}
