package com.Quiz_manager.domain
import com.Quiz_manager.dto.response.EventResponseDto
import com.Quiz_manager.service.EventService
import com.Quiz_manager.service.TeamService
import com.Quiz_manager.service.TelegramService
import com.Quiz_manager.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.BotSession
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.TelegramClient


@Component
class MyTelegramBot(
    @Value("\${telegram.bot_token}") private val botToken: String,
    private val eventService: EventService,
    @Lazy
    private val teamService: TeamService,
    private val userService: UserService,
    @Lazy
    private val telegramService: TelegramService
) : SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private val telegramClient: TelegramClient = OkHttpTelegramClient(getBotToken())
    private val userWaitingForTeamName = mutableSetOf<String>()

    override fun getUpdatesConsumer(): LongPollingUpdateConsumer = this

    final override fun getBotToken(): String = botToken

    override fun consume(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val messageText: String = update.message.text
            val chatId: String = update.message.chatId.toString()
            val userId: String = update.message.from.id.toString()

            when (messageText) {
                "/старт" -> handleTeamCreation(chatId)
                "/игры" -> handleGetEvents(chatId)
                "/инфо" -> handleInfoCommand(chatId)
                else -> return
            }
        }
    }


    private fun handleTeamCreation(chatId: String) {
        sendMessage(chatId, "Введите название команды:")
        userWaitingForTeamName.add(chatId)
    }



    private fun handleGetEvents(chatId: String) {
        val user = userService.getUserByTelegramId(chatId)
        if (user == null) {
            sendMessage(chatId, "Вы не зарегистрированы")
            return
        }

        if (user.teamMemberships.isEmpty()) {
            sendMessage(chatId, "Вы не состоите ни в одной команде")
            return
        }

        if (user.teamMemberships.size == 1) {
            // Если только одна команда
            val team = user.teamMemberships.first().team
            val events = eventService.getEventsByTeamId(team.id)
            val eventsMessage = formatEventsMessage(events)
            sendMessage(chatId, eventsMessage)
        } else {
            // Если несколько команд, предложим выбрать команду
            val teamNames = user.teamMemberships.map { it.team.name }
            val teamsMessage = "Вы состоите в нескольких командах. Выберите команду:\n" +
                    teamNames.joinToString("\n") { it }

            sendMessage(chatId, teamsMessage)
            userWaitingForTeamName.add(chatId)
        }
    }




    fun sendMessage(chatId: String, text: String) {
        val message = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .build()
        try {
            telegramClient.execute(message)
        } catch (e: TelegramApiException) {
            println("Ошибка отправки сообщения: ${e.message}")
        }
    }


    private fun formatEventsMessage(events: List<EventResponseDto>): String {
        return if (events.isEmpty()) {
            "На ближайшее время нет запланированных игр"
        } else {
            "Вот список предстоящих игр вашей команды:\n" +
                    events.joinToString("\n") { "${it.dateTime}: ${it.name}" }
        }
    }

    /**
    * Отправляет пользователю список доступных команд бота.
    */
    private fun handleInfoCommand(chatId: String) {
        val infoMessage = """
        🤖 Доступные команды бота:
        
        🔹 `/старт` — Создать команду.
        🔹 `/игры` — Посмотреть список ближайших игр.
        🔹 `/инфо` — Показать этот список команд.
        
        ⚠ Примечание: Команды работают в чате вашей команды.
    """.trimIndent()

        sendMessage(chatId, infoMessage)
    }

    @AfterBotRegistration
    fun afterRegistration(botSession: BotSession) {
        println("Бот успешно зарегистрирован! Статус: ${botSession.isRunning}")
    }
}
