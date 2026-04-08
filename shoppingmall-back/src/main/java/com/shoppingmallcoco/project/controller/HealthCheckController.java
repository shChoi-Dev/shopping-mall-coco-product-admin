package com.shoppingmallcoco.project.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class HealthCheckController {
	
	@GetMapping("/api/ping")
    public String ping() {
        return "pong!"; // 글자 반환
    }
	
}
