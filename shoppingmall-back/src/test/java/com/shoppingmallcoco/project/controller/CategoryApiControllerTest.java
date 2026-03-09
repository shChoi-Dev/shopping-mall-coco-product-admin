package com.shoppingmallcoco.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.shoppingmallcoco.project.entity.product.CategoryEntity;
import com.shoppingmallcoco.project.repository.product.CategoryRepository;
import com.shoppingmallcoco.project.util.JwtUtil;

@WebMvcTest(CategoryApiController.class) // Controller 계층만 로드하여 테스트
@AutoConfigureMockMvc(addFilters = false) // 스프링 시큐리티 필터를 비활성화하여 불필요한 인증/인가 에러(401, 403) 방지
class CategoryApiControllerTest {

	@Autowired
	private MockMvc mockMvc; // 가짜 HTTP 요청(GET, POST 등)을 보내는 핵심 객체
	
	@MockitoBean
    private CategoryRepository categoryRepository; // Controller가 사용하는 Repository를 가짜로 띄움
	
	@MockitoBean
	private JwtUtil jwtUtil; // Security Filter 의존성 에러 해결을 위해 JwtUtil을 Mock으로 등록
	
	@MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext; // JPA Auditing 에러를 피하기 위해 가짜 빈을 등록
	
	@Test
	@WithMockUser // 임시 인증 사용자 추가
	@DisplayName("카테고리 전체 목록 조회 API (GET /api/categories) 성공 테스트")
	void getAllCategories_Success() throws Exception {
		// 준비
		CategoryEntity entity1 = new CategoryEntity();
        entity1.setCategoryNo(1L);
        entity1.setCategoryName("스킨케어");

        CategoryEntity entity2 = new CategoryEntity();
        entity2.setCategoryNo(2L);
        entity2.setCategoryName("메이크업");
		
        List<CategoryEntity> expectedEntities = List.of(entity1, entity2);
		
        // categoryRepository의 메서드 호출 시 Mock Entity 리스트 반환 설정
		given(categoryRepository.findAll(any(Sort.class))).willReturn(expectedEntities);
		
		// 실행 및 검증
		mockMvc.perform(get("/api/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // HTTP 상태 코드가 200(OK) 인지?
                .andExpect(jsonPath("$.size()").value(2)) // 반환된 JSON 배열 크기가 2인지?
                .andExpect(jsonPath("$[0].categoryName").value("스킨케어")) // 첫 번째 요소 이름 검증
                .andExpect(jsonPath("$[1].categoryName").value("메이크업")); // 두 번째 요소 이름 검증
	}

}
