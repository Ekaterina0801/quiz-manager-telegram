package com.Quiz_manager.shedulers

import com.Quiz_manager.domain.Event
import com.Quiz_manager.repository.EventRepository
import com.Quiz_manager.repository.RegistrationRepository
import com.Quiz_manager.repository.TeamNotificationSettingsRepository
import com.Quiz_manager.service.EventService
import com.Quiz_manager.service.TelegramService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EventReminderScheduler(
    private val eventRepository: EventRepository,
    private val teamNotificationSettingsRepository: TeamNotificationSettingsRepository,
    private val registrationRepository: RegistrationRepository, private val telegramService: TelegramService, private val eventService: EventService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 * * * * *")
    fun sendNHrsBeforeReminders() {
        val now = LocalDateTime.now()
            .withSecond(0)
            .withNano(0)

        teamNotificationSettingsRepository.findAll().forEach { settings ->
            if (!settings.eventReminderEnabled) return@forEach

            val n = settings.registrationReminderHoursBeforeEvent.toLong()
            val start = now.plusHours(n)
            val end   = start.plusMinutes(1)

            val events = eventRepository.findByTeamIdAndDateTimeBetween(
                settings.team.id,
                start,
                end
            )
            events.forEach { sendSummary(it) }
        }
    }


    // 2) Напоминание за 1 час
    @Scheduled(cron = "0 0/1 * * * ?")
    fun sendOneHourBeforeReminders() {
        val now = LocalDateTime.now()
            .withSecond(0)
            .withNano(0)

        val start = now.plusHours(1)
        val end   = start.plusMinutes(1)

        teamNotificationSettingsRepository.findAll().forEach { settings ->
            if (!settings.eventReminderEnabled) return@forEach

            val events = eventRepository.findByTeamIdAndDateTimeBetween(
                settings.team.id,
                start,
                end
            )
            events.forEach { sendSummary(it) }
        }
    }



    private fun sendSummary(event: Event) {

        event.team.chatId
            ?.let { eventService.sendEventSummary(event, event.team.chatId!!) }
            ?.also { logger.info("Sent reminder for event ${event.id} to $it") }
    }
}
