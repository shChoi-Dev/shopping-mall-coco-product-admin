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
import com.shoppingmallcoco.project.dto.notice.NoticeRequestDTO;
import com.shoppingmallcoco.project.dto.notice.NoticeResponseDTO;
import com.shoppingmallcoco.project.entity.notice.NoticeEntity;
import com.shoppingmallcoco.project.service.auth.MemberService;
import com.shoppingmallcoco.project.service.notice.NoticeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeApiController {
	
	private final NoticeService noticeService;
	private final MemberService memberService;
	
	// 관리자 권한 검증 헬퍼 메서드
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
	
	// POST 관리자 : 공지사항 작성
	@PostMapping
	public ResponseEntity<NoticeResponseDTO> createNotice(@RequestBody NoticeRequestDTO dto, Authentication authentication) {
		checkAdminRole(authentication);
		NoticeEntity created = noticeService.createNotice(dto);
		return new ResponseEntity<>(new NoticeResponseDTO(created), HttpStatus.CREATED);
	}
	
	// PUT 관리자: 공지사항 수정
    @PutMapping("/{noticeNo}")
    public ResponseEntity<NoticeResponseDTO> updateNotice(@PathVariable Long noticeNo, @RequestBody NoticeRequestDTO dto, Authentication authentication) {
        checkAdminRole(authentication);
        NoticeResponseDTO updated = noticeService.updateNotice(noticeNo, dto);
        return ResponseEntity.ok(updated);
    }

    // DELETE 관리자: 공지사항 삭제
    @DeleteMapping("/{noticeNo}")
    public ResponseEntity<String> deleteNotice(@PathVariable Long noticeNo, Authentication authentication) {
        checkAdminRole(authentication);
        noticeService.deleteNotice(noticeNo);
        return ResponseEntity.ok("공지사항 삭제 성공");
    }
}
