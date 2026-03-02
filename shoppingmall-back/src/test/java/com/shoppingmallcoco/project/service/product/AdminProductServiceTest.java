package com.shoppingmallcoco.project.service.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shoppingmallcoco.project.entity.product.ProductEntity;
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
	
	@Test
	@DisplayName("상품 삭제 시 논리적 삭제 및 상태 변경 테스트")
	void deleteProduct_Success() {
		// 준비
		Long prdNo = 1L;
		
		// 삭제 테스트를 진행할 가짜 상품 엔티티 생성
		ProductEntity product = new ProductEntity();
		product.setPrdNo(prdNo);
		product.setStatus("SALE"); // 현재 판매중
		product.setIsDeleted("N"); // 삭제되지 않음
		
		// Repository에서 상품 번호를 찾으면 Mock 엔티티를 반환하도록 설정
		when(prdRepo.findById(prdNo)).thenReturn(java.util.Optional.of(product));
		
		// 실행 | 서비스의 삭제 메서드 호출
		adminProductService.deleteProduct(prdNo);
		
		// 검증
		assertEquals("Y", product.getIsDeleted()); // isDeleted 값이 "Y"로 변경되었는지?
		assertEquals("STOP", product.getStatus()); // 판매상태가 "STOP"으로 변경되었는지?
		verify(prdRepo, times(1)).findById(prdNo); // findById가 1번 정상적으로 호출 되었는지?
	}
	
	@Test
	@DisplayName("존재하지 않는 상품 삭제 시도 시 예외 발생 테스트")
	void deleteProduct_Fail_NotFound() {
		// 준비
		Long invalidPrdNo = 999L;
		
		// 없는 번호를 조회 시 빈 값을 반환
		when(prdRepo.findById(invalidPrdNo)).thenReturn(java.util.Optional.empty());
		
		// 실행 및 검증 | 해당 로직 실행 시 RuntimeException 오류가 생겨야 테스트 성공
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			adminProductService.deleteProduct(invalidPrdNo);
		});
		
		// 예외 메시지가 일치하는지 확인
		assertEquals("상품을 찾을 수 없습니다.", exception.getMessage());
	}
}
