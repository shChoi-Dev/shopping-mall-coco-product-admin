package com.shoppingmallcoco.project.service.product;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.shoppingmallcoco.project.entity.product.CategoryEntity;
import com.shoppingmallcoco.project.entity.product.ProductEntity;
import com.shoppingmallcoco.project.repository.product.CategoryRepository;
import com.shoppingmallcoco.project.repository.product.ProductRepository;
import com.shoppingmallcoco.project.service.review.IReviewService;

@DataJpaTest
@ActiveProfiles("test")
@Import(ProductService.class)
class ProductServiceSearchTest {

	@Autowired
	private ProductService productService;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@MockitoBean
	private IReviewService reviewService;
	
	private CategoryEntity testCategory;

	@BeforeEach
	void setUp() {
		testCategory = new CategoryEntity();
		testCategory.setCategoryName("스킨케어");
		categoryRepository.save(testCategory);
	}
	
	@Test
	@DisplayName("검색어 매핑 테스트: 건성 검색 시 dry 태그를 가진 상품이 조회 ")
	void getProductList_SearchKeywordMapping_Success() {
		// 준비 
		// 1번 상품 : 피부타입이 건성인 상품
		ProductEntity dryProduct = new ProductEntity();
		dryProduct.setPrdName("수분 폭탄 크림");
		dryProduct.setSkinType("dry"); // DB에는 영어로 저장
		dryProduct.setStatus("SALE");
		dryProduct.setIsDeleted("N");
		dryProduct.setCategory(testCategory);
		productRepository.save(dryProduct);
		
		// 2번 상품 : 피부타입이 지성인 상품
		ProductEntity oilyProduct = new ProductEntity();
		oilyProduct.setPrdName("산뜻한 로션");
		oilyProduct.setSkinType("oily");
		oilyProduct.setStatus("SALE");
		oilyProduct.setIsDeleted("N");
		oilyProduct.setCategory(testCategory);
		productRepository.save(oilyProduct);
		
		// 실행 - 사용자가 검색창에 "건성" 입력 가정(q = "건성")
		Page<ProductEntity> result = productService.getProductList(
				"건성", null, null, null, null, "SALE", "newest", 1, 10
		);
		
		// 검증 - 건성 태그를 가진 "수분 폭탄 크림" 1개만 조회
		assertEquals(1, result.getTotalElements());
		assertEquals("수분 폭탄 크림", result.getContent().get(0).getPrdName());
	}
	
	@Test
	@DisplayName("다중 조건 동적 쿼리 테스트: 피부고민(진정) + 카테고리 필터링")
	void getProductList_MultiFilter_Success() {
		// 준비
		ProductEntity p1 = new ProductEntity();
		p1.setPrdName("시카 진정 크림");
		p1.setSkinConcern("soothing");
		p1.setStatus("SALE");
		p1.setIsDeleted("N");
		p1.setCategory(testCategory);
		productRepository.save(p1);
		
		// 실행 - 카테고리 번호와 검색어를 동시에 필터링
		Page<ProductEntity> result = productService.getProductList(
				"진정", null, null, null, testCategory.getCategoryNo(), "SALE", "newest", 1, 10
		);
		
		// 검증
		assertEquals(1, result.getTotalElements());
		assertEquals("시카 진정 크림", result.getContent().get(0).getPrdName());
	}
}
