package com.shoppingmallcoco.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shoppingmallcoco.project.dto.notice.NoticeResponseDTO;
import com.shoppingmallcoco.project.service.notice.NoticeService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeApiController {

	private final NoticeService noticeService;

    // 공지사항 전체 목록 조회
    @GetMapping
    public ResponseEntity<List<NoticeResponseDTO>> getAllNotices() {
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    // 공지사항 상세 조회
    @GetMapping("/{noticeNo}")
    public ResponseEntity<NoticeResponseDTO> getNoticeDetail(
    		@PathVariable Long noticeNo, 
    		HttpServletRequest request, 
    		HttpServletResponse response) {
    	
    	Cookie[] cookies = request.getCookies();
    	boolean isNewView = true;
    	String cookieName = "noticeView_" + noticeNo; // 공지사항 전용 쿠키 이름
    	
    	if (cookies != null) {
    		for (Cookie cookie : cookies) {
    			if (cookie.getName().equals(cookieName)) {
    				isNewView = false;
    				break;
    			}
    		}
    	}
    	
    	if (isNewView) {
    		Cookie newCookie = new Cookie(cookieName, "viewed");
    		newCookie.setMaxAge(60 * 60 * 24); // 24시간 유지
    		newCookie.setPath("/");
    		response.addCookie(newCookie);
    	}
    	
        return ResponseEntity.ok(noticeService.getNoticeById(noticeNo, isNewView));
    }
}
