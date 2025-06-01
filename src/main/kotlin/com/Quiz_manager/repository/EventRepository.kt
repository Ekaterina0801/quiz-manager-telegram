package com.Quiz_manager.repository

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Team
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface EventRepository : JpaRepository<Event, Long> {


    fun findByDateTime(date: LocalDate): List<Event>


    fun findByTeamId(teamId: Long, pageable: Pageable): Page<Event>
    @Query(
        value = """
      SELECT * 
      FROM event 
      WHERE team_id = :teamId
        AND name LIKE '%' || :search || '%' COLLATE NOCASE
      """,
        countQuery = """
      SELECT count(*) 
      FROM event 
      WHERE team_id = :teamId
        AND name LIKE '%' || :search || '%' COLLATE NOCASE
      """,
        nativeQuery = true
    )
    fun searchByNameIgnoreCaseNative(
        @Param("teamId") teamId: Long,
        @Param("search") search: String,
        pageable: Pageable
    ): Page<Event>

    fun findByNameIgnoreCase(name: String): List<Event>


    fun findByDateTimeBetween(start: LocalDate, end: LocalDate): List<Event>

    fun findByTeamAndDateTime(team: Team, date: LocalDate): List<Event>

    fun findByTeamIdAndDateTimeBetween(
        teamId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Event>

}
