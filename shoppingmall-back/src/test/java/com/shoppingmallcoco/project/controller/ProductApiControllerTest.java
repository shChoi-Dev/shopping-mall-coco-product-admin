package com.shoppingmallcoco.project.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.shoppingmallcoco.project.entity.product.CategoryEntity;
import com.shoppingmallcoco.project.entity.product.ProductEntity;
import com.shoppingmallcoco.project.repository.auth.MemberRepository;
import com.shoppingmallcoco.project.service.product.ProductService;
import com.shoppingmallcoco.project.service.review.ReviewService;
import com.shoppingmallcoco.project.util.JwtUtil;

@WebMvcTest(ProductApiController.class)
@AutoConfigureMockMvc(addFilters = false) // 인증/인가 에러 방지
class ProductApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // 컨트롤러가 의존하는 ProductService를 Mock로 등록
    @MockitoBean
    private ProductService productService;
    
    // 컨트롤러가 의존하는 ReviewService를 Mock로 등록
    @MockitoBean
    private ReviewService reviewService;
    
    // 컨트롤러가 의존하는 MemberRepository를 Mock로 등록
    @MockitoBean
    private MemberRepository memberRepository;

    // 이전 트러블슈팅에서 배운 Security & JPA 의존성 에러 방지용 가짜 객체들
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser
    @DisplayName("상품 목록 페이징 및 검색 API 호출 성공 테스트")
    void getProductList_Success() throws Exception {
        // 준비 - Mock 카테고리 준비
        CategoryEntity category = new CategoryEntity();
        category.setCategoryNo(1L);
        category.setCategoryName("스킨케어");

        // Mock 상품 2개 준비
        ProductEntity p1 = new ProductEntity();
        p1.setPrdNo(1L);
        p1.setPrdName("수분 폭탄 크림");
        p1.setPrdPrice(15000);
        p1.setCategory(category);

        ProductEntity p2 = new ProductEntity();
        p2.setPrdNo(2L);
        p2.setPrdName("시카 진정 앰플");
        p2.setPrdPrice(20000);
        p2.setCategory(category);

        // Page 객체(Mock 페이징 결과) 만들기
        List<ProductEntity> productList = List.of(p1, p2);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ProductEntity> mockPage = new PageImpl<>(productList, pageRequest, 2);

        given(productService.getProductList(
                anyString(), isNull(), isNull(), isNull(), isNull(), anyString(), anyString(), anyInt(), anyInt()
        )).willReturn(mockPage);

        // 실행 및 검증 - param()을 이용해 실제 브라우저 주소창에 ?q=크림&page=1&size=10 을 치는 것과 같은 효과
        mockMvc.perform(get("/api/products")
                        .param("q", "크림")
                        .param("status", "SALE")
                        .param("sort", "newest")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 200 OK 코드 확인
                .andExpect(jsonPath("$.content.size()").value(2)) // 반환된 상품이 2개인지 확인
                .andExpect(jsonPath("$.content[0].prdName").value("수분 폭탄 크림")) // 첫 번째 상품명 검증
                .andExpect(jsonPath("$.totalElements").value(2)) // 총 개수 검증
                .andExpect(jsonPath("$.totalPages").value(1));   // 총 페이지 수 검증
    }
}
