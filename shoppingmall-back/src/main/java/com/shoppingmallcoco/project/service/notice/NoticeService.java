package com.shoppingmallcoco.project.service.notice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shoppingmallcoco.project.dto.notice.NoticeRequestDTO;
import com.shoppingmallcoco.project.dto.notice.NoticeResponseDTO;
import com.shoppingmallcoco.project.entity.notice.NoticeEntity;
import com.shoppingmallcoco.project.repository.notice.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {
	
	private final NoticeRepository noticeRepository;
	
	// 공지사항 작성
	@Transactional
	public NoticeEntity createNotice(NoticeRequestDTO dto) {
		NoticeEntity notice = NoticeEntity.builder()
				                          .title(dto.getTitle())
										  .content(dto.getContent())
										  .viewCount(0)
										  .build();
		
		return noticeRepository.save(notice);
	}
	
	// 공지사항 전체 목록 조회(최신순)
	@Transactional(readOnly = true)
	public List<NoticeResponseDTO> getAllNotices(){
		return noticeRepository.findAll().stream()
										 .map(NoticeResponseDTO::new)
										 .collect(Collectors.toList());
	}
	
	// 공지사항 상세 조회 (조회수 1 증가)
	@Transactional
	public NoticeResponseDTO getNoticeById(Long noticeNo) {
		NoticeEntity notice = noticeRepository.findById(noticeNo)
				                  			  .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));
		
		notice.increaseViesCount(); // 조회수 증가
		return new NoticeResponseDTO(notice);
	}
	
	// 공지사항 수정
	@Transactional
	public NoticeResponseDTO updateNotice(Long noticeNo, NoticeRequestDTO dto) {
		NoticeEntity notice = noticeRepository.findById(noticeNo)
											  .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));
											  
		notice.setTitle(dto.getTitle());
		notice.setContent(dto.getContent());
		return new NoticeResponseDTO(notice);
	}
				
	// 공지사항 삭제
	@Transactional
	public void deleteNotice(Long noticeNo) {
		noticeRepository.deleteById(noticeNo);
	}
	
}
