package com.shoppingmallcoco.project.entity.event;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "event_no")
	private Long eventNo; // 이벤트 글 번호 (PK)
	
	@Column(nullable = false, length = 200)
	private String title; // 이벤트 제목
	
	@Column(columnDefinition = "CLOB", nullable = false)
	private String content; // 이벤트 내용 (에디터 데이터)
	
	@Column(name = "thumbnail_url", length = 500)
	private String thumbnailUrl; // 목록에서 보여줄 배너(썸네일) 이미지 주소
	
	@Column(name = "start_date")
	private LocalDateTime startDate; // 이벤트 시작일
	
	@Column(name = "end_date")
	private LocalDateTime endDate; // 이벤트 종료일
	
	@Column(name = "view_count", nullable = false)
    private int viewCount; // 조회수

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 작성일

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일
    
    // 조회수 증가 메서드
    public void increaseViewCount() {
        this.viewCount++;
    }
	
}
