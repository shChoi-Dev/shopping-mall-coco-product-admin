package com.shoppingmallcoco.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmallcoco.project.dto.auth.MemberResponseDto;
import com.shoppingmallcoco.project.entity.product.CategoryEntity;
import com.shoppingmallcoco.project.service.auth.MemberService;
import com.shoppingmallcoco.project.service.product.CategoryService;
import com.shoppingmallcoco.project.util.JwtUtil;

@WebMvcTest(AdminCategoryApiController.class) // Security 필터는 켜둔 채로 Controller 계층만 로드
class AdminCategoryApiControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper; // Java 객체를 JSON 문자열로 변경
	
	// 컨트롤러가 의존하는 서비스들 Mock로 등록
	@MockitoBean
	private CategoryService categoryService;
	
	@MockitoBean
	private MemberService memberService; // 권한 검증에 사용 됨
	
	// Security & JPA 의존성 에러 방지용 가짜 객체
	@MockitoBean
	private JwtUtil jwtUtil;
	
	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;
	
	@Test
	@WithMockUser(username = "adminUser") // Mock 인증 사용자 이름 설정
	@DisplayName("관리자 권한으로 카테고리 생성 성공 테스트")
	void createCategory_Success_WithAdminh() throws Exception{
		// 준비 - checkAdminRole() 통과를 위해 MemberService가 "ADMIN" 역할을 가진 객체를 반환하도록 설정
		MemberResponseDto adminMember = new MemberResponseDto();
        adminMember.setRole("ADMIN");
        given(memberService.getMemberByMemId("adminUser")).willReturn(adminMember);
        
		// CategoryService가 새 카테고리를 저장하고 반환하도록 설정
        CategoryEntity mockCategory = new CategoryEntity();
        mockCategory.setCategoryNo(1L);
        mockCategory.setCategoryName("새 카테고리");
        given(categoryService.createCategory(anyString(), any())).willReturn(mockCategory);
		
        // POST 요청에 담아 보낼 JSON 데이터 만들기
        Map<String, String> request = new HashMap<>();
        request.put("categoryName", "새 카테고리");
		
		// 실행 및 검증
        mockMvc.perform(post("/api/admin/categories")
                .with(csrf()) // POST 요청 시 스프링 시큐리티의 CSRF 방어막을 통과하기 위한 가짜 토큰
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))) // Map을 JSON String으로 변환
                .andExpect(status().isCreated()) // HttpStatus.CREATED (201) 기대
                .andExpect(jsonPath("$.categoryName").value("새 카테고리"));
		
	}
	
	@Test
	@WithMockUser(username = "normalUser") // 일반 사용자 로그인 가정
	@DisplayName("일반 사용자로 카테고리 생성 시도 시 SecurityException 에러 발생 테스트")
	void createCategory_Fail_WithNormalUser() throws Exception {
		// 준비 - 일반 사용자 역할("USER") 설정
		MemberResponseDto normalMember = new MemberResponseDto();
        normalMember.setRole("USER");
        given(memberService.getMemberByMemId("normalUser")).willReturn(normalMember);

        Map<String, String> request = new HashMap<>();
        request.put("categoryName", "새 카테고리");

        // 실행 및 검증 - mockMvc.perform()을 실행했을 때 Exception이 터지는지 확인하고, 그 에러를 캐치
        Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(post("/api/admin/categories")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)));
		});
        
       // 캐치한 에러의 진짜 원인이 SecurityException인지, 메시지가 맞는지 검증
        assertTrue(exception.getCause() instanceof SecurityException);
        assertEquals("관리자 권한이 필요합니다.", exception.getCause().getMessage());
	}

}
