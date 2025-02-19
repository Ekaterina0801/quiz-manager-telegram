package com.Quiz_manager.service


import com.Quiz_manager.domain.MyTelegramBot
import com.Quiz_manager.domain.TeamMembership
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.ChatAdministratorsResponse
import com.Quiz_manager.dto.TelegramUser
import com.Quiz_manager.dto.response.TeamMembershipResponseDto
import com.Quiz_manager.dto.response.TelegramUserResponse
import com.Quiz_manager.dto.response.UserResponseDto
import com.Quiz_manager.enums.Role
import com.Quiz_manager.mapper.toDto
import com.Quiz_manager.mapper.toEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
class TelegramService(
    private val myTelegramBot: MyTelegramBot,
    @Lazy
    private val teamService: TeamService,
    private val userService: UserService
) {

    private val restTemplate = RestTemplate()

    @Value("\${telegram.bot_token}")
    private lateinit var botToken: String

    private val logger = LoggerFactory.getLogger(TelegramService::class.java)

    /**
     * Отправляет сообщение в Telegram-канал команды.
     *
     * @param chatId ID чата, в который будет отправлено сообщение
     * @param message Текст сообщения для отправки
     */
    fun sendMessageToChannel(chatId: String, message: String) {
        try {
            myTelegramBot.sendMessage(chatId, message)
        } catch (e: Exception) {
            logger.error("Ошибка при отправке сообщения в Telegram в чат $chatId", e)
        }
    }

    /**
     * Получает информацию о пользователе Telegram по его ID.
     *
     * @param telegramId ID пользователя в Telegram
     * @return объект TelegramUser с информацией о пользователе или null в случае ошибки
     */
    fun getUserInfo(telegramId: String): TelegramUser? {
        val url = "https://api.telegram.org/bot$botToken/getChat?chat_id=$telegramId"
        return try {
            val response = restTemplate.getForObject(url, TelegramUserResponse::class.java)
            logger.info("Получен ответ от Telegram API: $response")
            response?.result
        } catch (e: Exception) {
            logger.error("Ошибка при получении информации о пользователе Telegram с ID $telegramId", e)
            null
        }
    }

    /**
     * Получает список администраторов чата.
     *
     * @param chatId ID чата
     * @return список администраторов
     */
    fun getChatAdministrators(chatId: String): List<TelegramUser> {
        val url = "https://api.telegram.org/bot$botToken/getChatAdministrators?chat_id=$chatId"

        return try {
            val response = restTemplate.getForObject(url, ChatAdministratorsResponse::class.java)
            response?.result?.map { member ->
                TelegramUser(
                    id = member.user.id,
                    firstName = member.user.firstName,
                    lastName = member.user.lastName,
                    username = member.user.userName
                )
            } ?: emptyList()
        } catch (e: Exception) {
            logger.error("Ошибка при получении администраторов чата $chatId", e)
            emptyList()
        }
    }




    /**
     * Добавляет пользователя в команду с указанной ролью.
     *
     * @param telegramId ID чата в Telegram
     * @param teamId ID команды, в которую будет добавлен пользователь
     * @param role Роль пользователя в команде
     */
    fun handleAddUserToTeam(telegramId: String, teamId: Long, role: Role?): TeamMembershipResponseDto {
        val user = findOrCreateUser(telegramId).toEntity()
        return teamService.addUserToTeam(user.id, teamId, role).toDto()
    }



    /**
     * Находит или создает пользователя по его chatId.
     *
     * @param telegramId ID чата в Telegram
     * @return объект User
     */
    fun findOrCreateUser(telegramId: String): UserResponseDto {
        val existingUser = userService.findByTelegramId(telegramId)
        if (existingUser != null) {
            return existingUser.toDto()
        }

        val userInfo = getUserInfo(telegramId)

        val firstName = userInfo?.firstName ?: "Unknown"
        val lastName = userInfo?.lastName ?: "Unknown"

        return userService.createUser(
            username = userInfo?.username ?: telegramId,
            firstName = firstName,
            lastName = lastName,
            telegramId = telegramId
        ).toDto()
    }


    /**
     * Находит или создает пользователя по его Telegram ID (обновлено).
     *
     * @param update Обновление от Telegram
     * @return объект User
     */
    fun findOrCreateUser(update: Update): UserResponseDto {
        val chatId = update.message.chatId
        val telegramId = chatId.toString()

        val existingUser = userService.findByTelegramId(telegramId)

        if (existingUser != null) {
            return existingUser.toDto()
        }


        val firstName = update.message.from.firstName ?: "Unknown"
        val lastName = update.message.from.lastName ?: "Unknown"

        return userService.createUser(
            username = telegramId,
            firstName = firstName,
            lastName = lastName,
            telegramId = telegramId
        ).toDto()
    }

    fun isUserAdmin(chatId: String, userId: String): Boolean {
        val url = "https://api.telegram.org/bot$botToken/getChatAdministrators?chat_id=$chatId"

        return try {
            val response = restTemplate.getForObject(url, ChatAdministratorsResponse::class.java)
            val admins = response?.result ?: emptyList()

            val isAdmin = admins.any { it.user.id.toString() == userId }

            logger.info("Проверка администратора: пользователь $userId в чате $chatId - ${if (isAdmin) "Администратор" else "Не администратор"}")
            isAdmin
        } catch (e: Exception) {
            logger.error("Ошибка при проверке администраторских прав пользователя $userId в чате $chatId", e)
            false
        }
    }

}
