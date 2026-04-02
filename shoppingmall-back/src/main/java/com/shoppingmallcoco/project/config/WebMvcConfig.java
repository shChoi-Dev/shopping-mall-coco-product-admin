package com.shoppingmallcoco.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	// 클라우디너리 도입으로 인해 로컬 파일 서빙(ResourceHandler) 로직을 모두 삭제
	// 인터셉터나 다른 MVC 설정이 필요할 때 이 클래스를 다시 활용 예정
	
}
