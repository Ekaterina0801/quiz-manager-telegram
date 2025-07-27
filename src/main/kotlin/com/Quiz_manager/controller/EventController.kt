package com.Quiz_manager.controller

import com.Quiz_manager.domain.Event
import com.Quiz_manager.dto.RegistrationData
import com.Quiz_manager.dto.request.EventCreationDto
import com.Quiz_manager.dto.response.EventResponseDto
import com.Quiz_manager.service.EventService
import com.Quiz_manager.service.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.http.HttpHeaders
import java.security.Principal

@RestController
@RequestMapping("/api/events")
class EventController(private val eventService: EventService, private val userService: UserService) {


    @GetMapping
    fun getEvents(
        @RequestParam teamId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "100") size: Int,
        @RequestParam(defaultValue = "dateTime,desc") sort: String,
        @RequestParam(required = false) search: String?,
        principal: Principal
    ): ResponseEntity<List<EventResponseDto>> {
        val currentUser = userService.getCurrentUser(principal)
        val (sortProp, sortDir) = run {
            val parts = sort.split(",")
            val prop = parts[0]
            val dir  = if (parts.size > 1 && parts[1].equals("desc", ignoreCase = true))
                Sort.Direction.DESC
            else
                Sort.Direction.ASC
            prop to dir
        }

        val pageRequest = PageRequest.of(page, size, Sort.by(sortDir, sortProp))

        val dtoPage: Page<EventResponseDto> = eventService.getEventsByTeam(
            teamId  = teamId,
            pageable = pageRequest,
            search  = search,
            currentUserId=currentUser.id
        )

        val content       = dtoPage.content
        val totalElements = dtoPage.totalElements

        val fromIndex = (pageRequest.pageNumber * pageRequest.pageSize).toLong()
        val toIndex   = if (content.isEmpty()) fromIndex else fromIndex + content.size - 1L

        val contentRangeValue = "events $fromIndex-$toIndex/$totalElements"

        val headers = org.springframework.http.HttpHeaders().apply {
            add("Content-Range", contentRangeValue)
        }

        return ResponseEntity(content, headers, HttpStatus.OK)
    }

    @GetMapping("/{eventId}")
    fun getEventById(@PathVariable eventId: Long): ResponseEntity<EventResponseDto> {
        val dto = eventService.getEventById(eventId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(dto)
    }

    @PostMapping
    fun createEvent(@ModelAttribute eventRequest: EventCreationDto): ResponseEntity<Any> =
        try {
            val created = eventService.createEvent(eventRequest)
            ResponseEntity.ok(mapOf("message" to "ok"))

        } catch (ex: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.message)
        }

    @PutMapping(
        path = ["/{eventId}"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun updateEvent(
        @PathVariable eventId: Long,
        @ModelAttribute eventData: EventCreationDto
    ): ResponseEntity<Any> = try {
        val updated = eventService.updateEvent(eventId, eventData)
        ResponseEntity.ok(updated)
    } catch (ex: AccessDeniedException) {
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.message)
    }



    @DeleteMapping("/{eventId}")
    fun deleteEvent(
        @PathVariable eventId: Long,
        @RequestParam userId: Long
    ): ResponseEntity<Map<String, String>> =
        try {
            eventService.deleteEvent(eventId, userId)
            ResponseEntity.ok(mapOf("message" to "Event with id $eventId was deleted"))
        } catch (ex: AccessDeniedException) {
            ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to (ex.message ?: "Доступ запрещён")))
        } catch (ex: NoSuchElementException) {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to (ex.message ?: "Событие не найдено")))
        }

    @PostMapping("/{eventId}/register")
    fun registerForEvent(
        @PathVariable eventId: Long,
        @RequestBody registrationData: RegistrationData
    ): ResponseEntity<Any> =
        try {
            val reg = eventService.registerForEvent(eventId, registrationData)
            ResponseEntity.ok(reg)
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(ex.message)
        }

    @PostMapping("/{eventId}/unregister")
    fun unregisterFromEvent(
        @PathVariable eventId: Long,
        @RequestParam registrationId: Long,
        @RequestParam userId: Long
    ): ResponseEntity<Map<String, String>> =
        try {
            val msg = eventService.unregisterFromEvent(eventId, registrationId, userId)
            ResponseEntity.ok(mapOf("message" to msg))
        } catch (ex: AccessDeniedException) {
            ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to (ex.message ?: "Доступ запрещён")))
        } catch (ex: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to (ex.message ?: "Регистрация не найдена")))
        }

}
