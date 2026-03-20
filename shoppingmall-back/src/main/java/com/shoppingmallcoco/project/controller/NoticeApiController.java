package com.shoppingmallcoco.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shoppingmallcoco.project.dto.notice.NoticeResponseDTO;
import com.shoppingmallcoco.project.service.notice.NoticeService;

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
    public ResponseEntity<NoticeResponseDTO> getNoticeDetail(@PathVariable Long noticeNo) {
        return ResponseEntity.ok(noticeService.getNoticeById(noticeNo));
    }
}
