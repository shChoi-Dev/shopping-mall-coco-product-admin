package com.shoppingmallcoco.project.service.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shoppingmallcoco.project.repository.product.CategoryRepository;
import com.shoppingmallcoco.project.repository.product.ProductImageRepository;
import com.shoppingmallcoco.project.repository.product.ProductOptionRepository;
import com.shoppingmallcoco.project.repository.product.ProductRepository;
import com.shoppingmallcoco.project.service.review.IReviewService;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

	// AdminProductService가 사용하는 모든 객체를 Mock으로 제작
	@Mock
	private ProductRepository prdRepo;
	
	@Mock
	private ProductOptionRepository optionRepo;
	
	@Mock
	private CategoryRepository catRepo;
	
	@Mock
	private ProductImageRepository prdImgRepo;
	
	@Mock
	private IReviewService reviewService;
	
	// 위에서 만든 Mock 객체들을 AdminProductService에 주입
	@InjectMocks
	private AdminProductService adminProductService;

	@Test
	@DisplayName("관리자 대시보드 통계 데이터가 올바른 Map 형태로 반환되는지 테스트")
	void getDashboardStats_Success() {
		// 준비 | Mock DB가 반환할 특정 숫자를 미리 설정 (Stubbing)
		long expectedTotalProducts = 150L;
		long expectedInStockProducts = 100L;
		long expectedOutOfStockProducts = 50L;
		long expectedTotalStock = 3000L;
		
		// Repository의 특정 메서드가 호출되면 숫자를 반환
		when(prdRepo.count()).thenReturn(expectedTotalProducts);
		when(prdRepo.countByStatus("SALE")).thenReturn(expectedInStockProducts);
		when(prdRepo.countByStatus("SOLD_OUT")).thenReturn(expectedOutOfStockProducts);
		when(optionRepo.sumTotalStock()).thenReturn(expectedTotalStock);
		
		// 실행
		Map<String, Long> resultStats = adminProductService.getDashboardStats();
		
		// 검증
		assertEquals(expectedTotalProducts, resultStats.get("totalProducts"));
		assertEquals(expectedInStockProducts, resultStats.get("inStockProducts"));
		assertEquals(expectedOutOfStockProducts, resultStats.get("outOfStockProducts"));
		assertEquals(expectedTotalStock, resultStats.get("totalStock"));
	}
}
