package com.shoppingmallcoco.project.service.event;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shoppingmallcoco.project.dto.event.EventRequestDTO;
import com.shoppingmallcoco.project.dto.event.EventResponseDTO;
import com.shoppingmallcoco.project.entity.event.EventEntity;
import com.shoppingmallcoco.project.repository.event.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {
	
	private final EventRepository eventRepository;
	
	@Transactional
	public EventEntity createEvent(EventRequestDTO dto) {
		EventEntity event = EventEntity.builder()
									   .title(dto.getTitle())
									   .content(dto.getContent())
									   .thumbnailUrl(dto.getThumbnailUrl())
									   .startDate(dto.getStartDate())
									   .endDate(dto.getEndDate())
									   .viewCount(0)
									   .build();
		
		return eventRepository.save(event);
	}
	
	@Transactional(readOnly = true)
	public List<EventResponseDTO> getAllEvents() {
		
		return eventRepository.findAll().stream()
					 		  .map(EventResponseDTO::new)
					 		  .collect(Collectors.toList());
	}
	
	@Transactional
	public EventResponseDTO getEventById(Long eventNo) {
		EventEntity event = eventRepository.findById(eventNo)
										   .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다."));
		event.increaseViewCount();
		
		return new EventResponseDTO(event);
	}
	
	@Transactional
	public EventResponseDTO updateEvent(Long eventNo, EventRequestDTO dto) {
		EventEntity event = eventRepository.findById(eventNo)
										   .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다."));
		
		event.setTitle(dto.getTitle());
		event.setContent(dto.getContent());
		event.setThumbnailUrl(dto.getThumbnailUrl());
		event.setStartDate(dto.getStartDate());
		event.setEndDate(dto.getEndDate());
		
		return new EventResponseDTO(event);
	}
	
	@Transactional
	public void deleteEvent(Long eventNo) {
		eventRepository.deleteById(eventNo);
	}	
}
