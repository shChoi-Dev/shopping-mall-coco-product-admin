package com.shoppingmallcoco.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shoppingmallcoco.project.dto.event.EventResponseDTO;
import com.shoppingmallcoco.project.service.event.EventService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
	public ResponseEntity<EventResponseDTO> getEventDetail(@PathVariable Long eventNo, 
															HttpServletRequest request,
															HttpServletResponse response) {
		
		Cookie[] cookies = request.getCookies();
		boolean isNewView = true; // 기본적으로 새로운 조회로 가정
		String cookieName = "eventView_" + eventNo; // 쿠키 이름 (예: eventView_1)
		
		// 유저가 가져온 쿠키를 검사
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(cookieName)) {
					isNewView = false;
					break;
				}
			}
		}
		
		// 처음 보는 유저일 경우?
		if (isNewView) {
			Cookie newCookie = new Cookie(cookieName, "viewed");
			newCookie.setMaxAge(60 * 60 * 24);
			newCookie.setPath("/");
			response.addCookie(newCookie);
		}
		
		// 검사 결과(isNewView)를 Service로 넘기기
        return ResponseEntity.ok(eventService.getEventById(eventNo, isNewView));
	}
}
