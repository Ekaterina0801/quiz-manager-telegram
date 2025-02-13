package com.Quiz_manager.repository

import com.Quiz_manager.domain.Team
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.Query


@Repository
interface TeamRepository : JpaRepository<Team, Long> {


    /**
     * Находит команду по имени.
     *
     * @param name имя команды
     * @return команда или null, если не найдена
     */
    fun findByName(name: String): Team?

    /**
     * Находит команду по ID чата.
     *
     * @param chatId ID чата команды
     * @return команда или null, если не найдена
     */
    @Query("SELECT t FROM Team t WHERE t.chatId = :chatId")
    fun findByChatId(chatId: String): Team?

    /**
     * Находит команду по коду приглашения.
     *
     * @param inviteCode код приглашения
     * @return команда или null, если не найдена
     */
    fun findByInviteCode(inviteCode: String): Team?

    /**
     * Проверяет, существует ли команда с данным кодом приглашения.
     *
     * @param inviteCode код приглашения
     * @return true, если команда с таким кодом существует
     */
    fun existsByInviteCode(inviteCode: String): Boolean
}

