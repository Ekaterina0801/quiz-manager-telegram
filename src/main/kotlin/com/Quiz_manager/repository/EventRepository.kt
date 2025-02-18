package com.Quiz_manager.repository

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Team
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface EventRepository : JpaRepository<Event, Long> {


    fun findByDateTime(date: LocalDate): List<Event>


    fun findByTeamId(teamId: Long): List<Event>


    fun findByNameIgnoreCase(name: String): List<Event>


    fun findByDateTimeBetween(start: LocalDate, end: LocalDate): List<Event>

    fun findByTeamAndDateTime(team: Team, date: LocalDate): List<Event>

}
