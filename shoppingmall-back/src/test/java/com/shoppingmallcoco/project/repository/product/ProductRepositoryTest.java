package com.shoppingmallcoco.project.repository.product;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.shoppingmallcoco.project.entity.product.CategoryEntity;
import com.shoppingmallcoco.project.entity.product.ProductEntity;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	private CategoryEntity testCategory;
	
	@BeforeEach // @BeforeEach - 각 테스트 메서드가 실행되기 전에 항상 먼저 실행되는 준비 작업
	void setUp() {
		// 상품 저장시 카테고리가 필수이므로 미리 생성
		testCategory = new CategoryEntity();
		testCategory.setCategoryName("테스트 카테고리");
		categoryRepository.save(testCategory);
	}
	
	@Test
	@DisplayName("상태별 상품 개수 조회 테스트")
	void countByStatus_Success() {
		// 준비 - 판매중 2개, 품절 1개 저장
		ProductEntity p1 = createProduct("상품1", "SALE", LocalDate.now(), "N");
		ProductEntity p2 = createProduct("상품2", "SALE", LocalDate.now(), "N");
		ProductEntity p3 = createProduct("상품3", "SOLD_OUT", LocalDate.now(), "N");
		
		productRepository.saveAll(List.of(p1, p2, p3));
		
		// 실행
		long saleCount = productRepository.countByStatus("SALE");
		long soldOutCount = productRepository.countByStatus("SOLD_OUT");
		
		// 검증
		assertEquals(2L, saleCount, "SALE 상태인 상품은 2개여야 합니다.");
		assertEquals(1L, soldOutCount, "SOLD_OUT 상태인 상품은 1개여야 합니다.");
	}
	
	@Test
	@DisplayName("최근 등록 상품 조회 쿼리 테스트 - 삭제된 상품 제외 및 정렬 확인")
	void findRecentProducts_Success() {
		// 준비
		// p1 - 3일 전 등록, 정상
		ProductEntity p1 = createProduct("오래된 상품", "SALE", LocalDate.now().minusDays(3), "N");
		// p2 - 1일 전 등록, 정상
		ProductEntity p2 = createProduct("최근 상품", "SALE", LocalDate.now().minusDays(1), "N");
		// p3 - 오늘 등록되었으나 삭제됨
		ProductEntity p3 = createProduct("삭제된 상품", "SALE", LocalDate.now(), "Y");
		
		productRepository.saveAll(List.of(p1, p2, p3));
		
		// 실행
		// 최대 5개까지만 가져오도록 페이징 정보 세팅
		Pageable pageable = PageRequest.of(0, 5); // 0번 페이지, 5개 사이즈
		List<ProductEntity> recentProducts = productRepository.findRecentProducts(pageable);
		
		// 검증
		// 삭제된 상품이 제외되어 총 2개만 조회되어야 함
		assertEquals(2, recentProducts.size(), "삭제된 상품은 조회되지 않아야 합니다.");
		
		// 내림차순 정렬이므로 더 최근에 등록된 p2(최근 상품)가 먼저 나와야 함
		assertEquals("최근 상품", recentProducts.get(0).getPrdName());
        assertEquals("오래된 상품", recentProducts.get(1).getPrdName());
	}

	// 테스트용 상품 엔티티를 쉽게 만들기 위한 헬퍼 메서드
	private ProductEntity createProduct(String name, String status, LocalDate regDate, String isDeleted) {
		
		ProductEntity product = new ProductEntity();
		product.setPrdName(name);
		product.setPrdPrice(10000);
		product.setCategory(testCategory);
		product.setStatus(status);
		product.setRegDate(regDate);
		product.setIsDeleted(isDeleted);
		
		return product;
	}
}
