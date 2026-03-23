package com.shoppingmallcoco.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shoppingmallcoco.project.dto.event.EventResponseDTO;
import com.shoppingmallcoco.project.service.event.EventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventApiController {
	
	private final EventService eventService;
	
	@GetMapping
	public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
		return ResponseEntity.ok(eventService.getAllEvents());
	}
	
	@GetMapping("/{eventNo}")
	public ResponseEntity<EventResponseDTO> getEventDetail(@PathVariable Long eventNo) {
        return ResponseEntity.ok(eventService.getEventById(eventNo));
	}
}
