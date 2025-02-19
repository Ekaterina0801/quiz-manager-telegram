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
    private val userWaitingForTeamName = mutableMapOf<String, String>() // userId -> chatId
    private val userWaitingForDeletionConfirmation = mutableMapOf<String, Long>() // userId -> teamId

    override fun getUpdatesConsumer(): LongPollingUpdateConsumer = this

    final override fun getBotToken(): String = botToken

    override fun consume(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val messageText: String = update.message.text.lowercase()
            val chatId: String = update.message.chatId.toString()
            val userId: String = update.message.from.id.toString() // <-- теперь берём ID пользователя правильно!

            when {
                messageText == "/старт" -> handleTeamCreation(userId, chatId)
                messageText == "/удалить_команду" -> handleDeleteTeamRequest(userId, chatId)
                messageText == "/игры" -> handleGetEvents(userId)
                messageText == "/инфо" -> handleInfoCommand(chatId)
                userWaitingForTeamName.containsKey(userId) -> handleNewTeamName(userId, chatId, messageText)
                userWaitingForDeletionConfirmation.containsKey(userId) -> handleDeleteConfirmation(userId, chatId, messageText)
            }
        }
    }


    /**
     * Проверяет, есть ли уже команда в чате. Если есть - выдаёт сообщение и ничего не делает.
     */
    private fun handleTeamCreation(userId: String, chatId: String) {
        val existingTeam = teamService.getTeamByChatId(chatId)
        if (existingTeam != null) {
            sendMessage(chatId, "В чате уже есть команда: ${existingTeam.name}")
            return
        }

        if (userWaitingForTeamName.containsKey(userId)) {
            sendMessage(chatId, "Вы уже вводите название команды. Пожалуйста, отправьте название")
            return
        }

        sendMessage(chatId, "Введите название вашей новой команды:")
        userWaitingForTeamName[userId] = chatId
    }

    /**
     * Создаёт команду, если её нет, иначе сообщает, что такая команда уже существует.
     */
    private fun handleNewTeamName(userId: String, chatId: String, teamName: String) {
        userWaitingForTeamName.remove(userId)

        val existingTeam = teamService.getTeamByChatId(chatId)
        if (existingTeam != null && existingTeam.name.equals(teamName, ignoreCase = true)) {
            sendMessage(chatId, "Команда с таким названием уже есть в этом чате!")
            return
        }

        try {
            val newTeam = teamService.createTeam(teamName, chatId)
            sendMessage(chatId, "Команда \"$teamName\" успешно создана! 🎉Код приглашения в команду для приложения: ${newTeam.inviteCode}")
        } catch (e: Exception) {
            sendMessage(chatId, "Ошибка создания команды: ${e.message}")
        }
    }

    /**
     * Запрашивает подтверждение удаления команды, если пользователь — админ.
     */
    private fun handleDeleteTeamRequest(userId: String, chatId: String) {
        val team = teamService.getTeamByChatId(chatId)
        val user = userService.getUserByTelegramId(userId)
        if (team == null) {
            sendMessage(chatId, "В этом чате нет зарегистрированной команды")
            return
        }

        if (!userService.isUserAdmin(user!!.id, team.id)) {
            sendMessage(chatId, "Удалить команду может только её администратор")
            return
        }

        sendMessage(chatId, "Вы действительно хотите удалить команду \"${team.name}\"? Отправьте \"да\" для подтверждения.")
        userWaitingForDeletionConfirmation[userId] = team.id
    }

    /**
     * Подтверждает удаление команды. Если пользователь вводит "да", команда удаляется.
     */
    private fun handleDeleteConfirmation(userId: String, chatId: String, messageText: String) {
        val teamId = userWaitingForDeletionConfirmation.remove(userId) ?: return

        if (messageText == "да") {
            try {
                teamService.deleteTeamById(teamId)
                sendMessage(chatId, "Команда успешно удалена! ❌")
            } catch (e: Exception) {
                sendMessage(chatId, "Ошибка удаления команды: ${e.message}")
            }
        } else {
            sendMessage(chatId, "Удаление отменено.")
        }
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
            val team = user.teamMemberships.first().team
            val events = eventService.getEventsByTeamId(team.id)
            sendMessage(chatId, formatEventsMessage(events))
        } else {
            val teamNames = user.teamMemberships.map { it.team.name }
            sendMessage(chatId, "Вы состоите в нескольких командах. Выберите команду:\n" + teamNames.joinToString("\n"))
            userWaitingForTeamName[user.id.toString()] = chatId
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

    private fun handleInfoCommand(chatId: String) {
        val infoMessage = """
        🤖 Доступные команды бота:
        
        🔹 `/старт` — Создать команду.
        🔹 `/удалить_команду` — Удалить команду (только админ, требует подтверждения).
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
