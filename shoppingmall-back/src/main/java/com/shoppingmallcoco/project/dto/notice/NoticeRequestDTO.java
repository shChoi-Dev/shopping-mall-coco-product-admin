package com.shoppingmallcoco.project.dto.notice;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeRequestDTO {
	private String title;
	private String content; // 에디터에서 작성한 HTML 본문
}
