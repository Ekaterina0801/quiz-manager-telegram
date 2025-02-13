package com.Quiz_manager.domain
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
    @Value("\${telegram.bot_username}") private val botUsername: String,
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

            when {
                messageText.startsWith("/register_team") -> handleTeamRegister(chatId, messageText, userId)
                messageText.startsWith("/start_team") -> handleTeamCreation(chatId)
                messageText.startsWith("/events") -> handleGetEvents(chatId)
                else -> handleUserInput(update)
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

    private fun handleUserInput(update: Update) {
        val chatId = update.message.chatId.toString()
        val userId = update.message.from.id.toString()
        val userMessage = update.message.text.trim()

        // Проверяем, ожидает ли пользователь ввода названия команды
        if (userWaitingForTeamName.contains(chatId)) {
            val existingTeam = teamService.getTeamByTelegramId(chatId)

            if (existingTeam != null) {
                sendMessage(chatId, "В этом чате уже зарегистрирована команда '${existingTeam.name}'.")
                userWaitingForTeamName.remove(chatId)
                return
            }

            // Проверяем, является ли пользователь администратором группы/канала
            val isAdmin = telegramService.isUserAdmin(chatId, userId)
            if (!isAdmin) {
                sendMessage(chatId, "Только администратор группы/канала может зарегистрировать команду.")
                return
            }
            val newTeam = teamService.createTeam(userMessage, chatId)

            sendMessage(chatId, "Команда '${newTeam.name}' успешно зарегистрирована! Участники могут присоединиться через приложение. Код приглашения: ${newTeam.inviteCode}")
            userWaitingForTeamName.remove(chatId)
        } else {
            val user = userService.getUserByTelegramId(chatId)

            if (user == null || user.teamMemberships.isEmpty()) {
                val existingTeam = teamService.getTeamByTelegramId(chatId)

                if (existingTeam == null) {
                    sendMessage(chatId, "Вы администратор, но не зарегистрированы в команде. Пожалуйста, введите название вашей команды.")
                    userWaitingForTeamName.add(chatId)
                } else {
                    sendMessage(chatId, "Вы не состоите в команде")
                }
            }
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


    private fun handleTeamRegister(chatId: String, messageText: String, userId: String) {
        val parts = messageText.split(" ", limit = 2)
        if (parts.size < 2) {
            sendMessage(chatId, "Использование: /register_team <Название команды>")
            return
        }

        val teamName = parts[1].trim()

        val existingTeam = teamService.getTeamByTelegramId(chatId)
        if (existingTeam != null) {
            sendMessage(chatId, "В этом чате уже зарегистрирована команда '${existingTeam.name}'.")
            return
        }


        val newTeam = teamService.createTeam(teamName, chatId)

        sendMessage(chatId, "Команда '$teamName' успешно зарегистрирована! Участники могут присоединиться через команду: /join_team ${newTeam.inviteCode}")
    }


    private fun formatEventsMessage(events: List<Event>): String {
        return if (events.isEmpty()) {
            "На ближайшее время нет запланированных мероприятий."
        } else {
            "Вот список предстоящих мероприятий вашей команды:\n" +
                    events.joinToString("\n") { "${it.dateTime}: ${it.name}" }
        }
    }

    @AfterBotRegistration
    fun afterRegistration(botSession: BotSession) {
        println("Бот успешно зарегистрирован! Статус: ${botSession.isRunning}")
    }
}
