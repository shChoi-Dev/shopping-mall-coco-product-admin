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
	private Long noticeNo; // 공지사항 글 번호 (PK)
	
	@Column(nullable = false, length = 200)
	private String title; // 글 제목
	
	@Column(columnDefinition = "TEXT", nullable = false)
	private String content; // 글 내용
	
	@Column(nullable = false)
	private int viewCount; // 조회수
	
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt; // 작성일
	
	@UpdateTimestamp
	private LocalDateTime updatedAt; // 수정일
	
	// 조회수 증가 편의 메서드
	public void increaseViesCount() {
		this.viewCount++;
	}

}
