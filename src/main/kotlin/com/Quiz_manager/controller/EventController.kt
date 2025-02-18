package com.Quiz_manager.controller

import com.Quiz_manager.domain.Event
import com.Quiz_manager.dto.request.EventCreationDto
import com.Quiz_manager.dto.RegistrationData
import com.Quiz_manager.dto.response.EventResponseDto
import com.Quiz_manager.service.EventService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/events")
class EventController(private val eventService: EventService) {

    @GetMapping
    fun getAllEvents(): ResponseEntity<List<EventResponseDto>> = ResponseEntity.ok(eventService.getAllEvents())


    @GetMapping("/{eventId}")
    fun getEventById(@PathVariable eventId: Long): ResponseEntity<EventResponseDto> {
        val event = eventService.getEventById(eventId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(event)
    }

    @PostMapping
    fun createEvent(@ModelAttribute eventRequest: EventCreationDto): ResponseEntity<Any> {
        return try {
            val createdEvent = eventService.createEvent(eventRequest)
            ResponseEntity.ok(createdEvent)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        }
    }


    @PutMapping("/{eventId}")
    fun updateEvent(
        @PathVariable eventId: Long,
        eventData: EventCreationDto
    ): ResponseEntity<Any> {
        return try {
            val event = eventService.updateEvent(eventId, eventData)
            ResponseEntity.ok(event)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        }
    }


    @DeleteMapping("/{eventId}")
    fun deleteEvent(@PathVariable eventId: Long, @RequestParam("userId") userId: Long): ResponseEntity<String> {
        try {
            eventService.deleteEvent(eventId, userId)
            return ResponseEntity.ok("Event with id ${eventId} was deleted")

        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        }
    }

    @PostMapping("/{eventId}/register")
    fun registerForEvent(
        @PathVariable eventId: Long,
        @RequestBody registrationData: RegistrationData
    ): ResponseEntity<String> {
        return try {
            ResponseEntity.ok(eventService.registerForEvent(eventId, registrationData))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @PostMapping("/{eventId}/unregister")
    fun unregisterFromEvent(
        @PathVariable eventId: Long,
        @RequestParam registrationId: Long,
        @RequestParam telegramId: String
    ): ResponseEntity<String> {
        return try {
            ResponseEntity.ok(eventService.unregisterFromEvent(eventId, registrationId, telegramId))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }
}
