package com.shoppingmallcoco.project.dto.notice;

import java.time.LocalDateTime;

import com.shoppingmallcoco.project.entity.notice.NoticeEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeResponseDTO {
	private Long noticeNo;
	private String title;
	private String content;
	private int viewCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	// Entity를 DTO로 변환하는 생성자
	public NoticeResponseDTO(NoticeEntity entity) {
		this.noticeNo = entity.getNoticeNo();
		this.title = entity.getTitle();
		this.content = entity.getContent();
		this.viewCount = entity.getViewCount();
		this.createdAt = entity.getCreatedAt();
		this.updatedAt = entity.getUpdatedAt();
	}
	
}
