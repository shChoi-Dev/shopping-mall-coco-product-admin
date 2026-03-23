package com.shoppingmallcoco.project.dto.event;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventRequestDTO {
	private String title;
	private String content;
	private String thumbnailUrl;		// 썸네일 URL
	private LocalDateTime startDate;	// 이벤트 시작일
	private LocalDateTime endDate;		// 이벤트 종료일
}
