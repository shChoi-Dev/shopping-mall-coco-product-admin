package com.shoppingmallcoco.project.entity.notice;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notice_no")
	private Long noticeNo; // 공지사항 글 번호 (PK)
	
	@Column(nullable = false, length = 200)
	private String title; // 글 제목
	
	@Column(columnDefinition = "CLOB", nullable = false)
	private String content; // 글 내용
	
	@Column(name = "view_count", nullable = false)
	private int viewCount; // 조회수
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt; // 작성일
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt; // 수정일
	
	// 조회수 증가 편의 메서드
	public void increaseViesCount() {
		this.viewCount++;
	}

}
