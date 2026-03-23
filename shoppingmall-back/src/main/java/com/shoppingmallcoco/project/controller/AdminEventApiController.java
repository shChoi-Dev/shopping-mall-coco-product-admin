package com.shoppingmallcoco.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shoppingmallcoco.project.dto.auth.MemberResponseDto;
import com.shoppingmallcoco.project.dto.event.EventRequestDTO;
import com.shoppingmallcoco.project.dto.event.EventResponseDTO;
import com.shoppingmallcoco.project.entity.event.EventEntity;
import com.shoppingmallcoco.project.service.auth.MemberService;
import com.shoppingmallcoco.project.service.event.EventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventApiController {
	
	private final EventService eventService;
	private final MemberService memberService;
	
	private void checkAdminRole(Authentication authentication) {
		
		if (authentication == null || authentication.getName() == null) {
			throw new SecurityException("인증이 필요합니다.");
		}
		
		MemberResponseDto currentMember = memberService.getMemberByMemId(authentication.getName());
		
		if (currentMember.getRole() == null || 
				(!currentMember.getRole().equals("ADMIN") && !currentMember.getRole().equals("admin"))) {
			
			throw new SecurityException("관리자 권한이 필요합니다.");
		}
	}
	
	@PostMapping
	public ResponseEntity<EventResponseDTO> createEvent(@RequestBody EventRequestDTO dto, Authentication authentication){
		
		checkAdminRole(authentication);
		
		EventEntity created = eventService.createEvent(dto);
		
		return new ResponseEntity<>(new EventResponseDTO(created), HttpStatus.CREATED);
	}
	
	@PutMapping("/{eventNo}")
	public ResponseEntity<EventResponseDTO> updateEvent(@PathVariable Long eventNo, @RequestBody EventRequestDTO dto, Authentication authentication) {
		
		checkAdminRole(authentication);
		
		return ResponseEntity.ok(eventService.updateEvent(eventNo, dto));
	}
	
	@DeleteMapping("/{eventNo}")
	public ResponseEntity<String> deleteEvent(@PathVariable Long eventNo, Authentication authentication) {
		
		checkAdminRole(authentication);
		
		eventService.deleteEvent(eventNo);
		
		return ResponseEntity.ok("이벤트 삭제 성공");
	}
	
}
