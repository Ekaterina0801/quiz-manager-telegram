package com.Quiz_manager.service

import com.Quiz_manager.domain.Event
import com.Quiz_manager.repository.EventRepository
import com.Quiz_manager.repository.TeamNotificationSettingsRepository
import jakarta.transaction.Transactional

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@EnableScheduling
class ReminderService(
    private val eventRepository: EventRepository,
    private val teamNotificationSettingsRepository: TeamNotificationSettingsRepository, private val telegramService: TelegramService
) {

    /**
     * Планировщик, который выполняется каждый час.
     * Проверяет, нужно ли отправить напоминание о предстоящем мероприятии для всех команд.
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    fun checkAndSendReminders() {
        val now = LocalDateTime.now()

        val events = eventRepository.findAll()

        for (event in events) {
            val team = event.team
            val settings = teamNotificationSettingsRepository.findByTeamId(team.id) ?: continue

            if (!settings.eventReminderEnabled) continue

            val reminderTime = event.dateTime.minusHours(settings.registrationReminderHoursBeforeEvent.toLong())

            if (now.isAfter(reminderTime) && now.isBefore(event.dateTime)) {
                sendEventReminder(event, team.chatId)
            }
        }
    }

    /**
     * Отправляет напоминание о мероприятии в Telegram-чат команды.
     *
     * @param event мероприятие
     * @param chatId ID Telegram-чата команды
     */
    private fun sendEventReminder(event: Event, chatId: String?) {
        val participants = event.registrations
            .joinToString(separator = "\n") { "👤 ${it.fullName}" }

        val message = """
        🔔 Напоминание о мероприятии! 🔔
        
        📌 Название: ${event.name}
        📅 Дата и время: ${event.dateTime}
        📍 Место: ${event.location}
        📍 Подробнее: ${event.description}
        👥 Количество участников: ${event.registrations.size}
        
        🏅 Участники:
        $participants
    """.trimIndent()

        //telegramService.sendMessageToChannel(chatId, message)
    }

}
