package com.shoppingmallcoco.project.dto.event;

import java.time.LocalDateTime;

import com.shoppingmallcoco.project.entity.event.EventEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventResponseDTO {
	private Long eventNo;
	private String title;
	private String content;
	private String thumbnailUrl;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private int viewCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	// Entity -> DTO 변환 생성자
	public EventResponseDTO(EventEntity entity) {
		this.eventNo = entity.getEventNo();
		this.title = entity.getTitle();
		this.content = entity.getContent();
		this.thumbnailUrl = entity.getThumbnailUrl();
		this.startDate = entity.getEndDate();
		this.viewCount = entity.getViewCount();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
	}
}
