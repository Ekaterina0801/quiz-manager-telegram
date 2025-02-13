package com.Quiz_manager.repository

import com.Quiz_manager.domain.Event
import com.Quiz_manager.domain.Team
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface EventRepository : JpaRepository<Event, Long> {

    // Find events by date
    fun findByDateTime(date: LocalDate): List<Event>

    // Find events by team
    fun findByTeamId(teamId: Long): List<Event>

    // Find events by name (case insensitive)
    fun findByNameIgnoreCase(name: String): List<Event>

    // Find events between a start and end date (useful for fetching upcoming events within a time range)
    fun findByDateTimeBetween(start: LocalDate, end: LocalDate): List<Event>

    // Find events by team and a specific date (e.g., to get events for a team on a specific date)
    fun findByTeamAndDateTime(team: Team, date: LocalDate): List<Event>

}
