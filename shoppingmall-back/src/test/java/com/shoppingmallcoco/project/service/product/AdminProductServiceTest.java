package com.shoppingmallcoco.project.service.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.shoppingmallcoco.project.dto.product.ProductSaveDTO;
import com.shoppingmallcoco.project.entity.product.CategoryEntity;
import com.shoppingmallcoco.project.entity.product.ProductEntity;
import com.shoppingmallcoco.project.entity.product.ProductImageEntity;
import com.shoppingmallcoco.project.entity.product.ProductOptionEntity;
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
	
	@Test
	@DisplayName("상품 등록 로직(옵션, 이미지 포함) 성공 테스트")
	void createProduct_Success() throws Exception {
		// 준비 | @Value 필드 수동 주입: ReflectionTestUtils을 사용해 강제로 값을 세팅
		ReflectionTestUtils.setField(adminProductService, "rootDir", "test-dir/");
		
		// 입력 데이터 셋팅
		ProductSaveDTO dto = new ProductSaveDTO();
		dto.setPrdName("테스트 상품1");
		dto.setPrdPrice(15000);
		dto.setCategoryNo(1L);
		dto.setStatus("SALE");
		
		ProductSaveDTO.OptionDTO optionDto = new ProductSaveDTO.OptionDTO();
		optionDto.setOptionName("용량");
		optionDto.setOptionValue("50ml");
		optionDto.setAddPrice(0);
		optionDto.setStock(100);
		dto.setOptions(List.of(optionDto));
		
		// 카테고리 Mock 동작 설정
		CategoryEntity category = new CategoryEntity();
		category.setCategoryNo(1L);
		when(catRepo.findById(1L)).thenReturn(java.util.Optional.of(category));
		
		// 상품 저장 Mock 동작 설정
		ProductEntity savedProduct = new ProductEntity();
		savedProduct.setPrdNo(100L);
		when(prdRepo.save(any(ProductEntity.class))).thenReturn(savedProduct);
		
		// Mock 파일(MockMultipartFile 역할) 만들기
		MultipartFile mockFile = mock(MultipartFile.class);
		when(mockFile.isEmpty()).thenReturn(false); // 빈 파일 아님
		when(mockFile.getContentType()).thenReturn("image/jpeg"); // 이미지 파일 타입
		when(mockFile.getSize()).thenReturn(1024L); // 10MB 이하 사이즈
		when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
		
		// 1x1 픽셀의 투명 GIF 이미지를 Base64로 디코딩하여 이미지 바이트를 생성
		byte[] validImageBytes = Base64.getDecoder().decode("R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==");
		
		// Thumbnailator 라이브러리가 getInputStream()을 호출할 때마다 새로운 InputStream을 반환하도록 설정 (Fallback 로직 대비)
		when(mockFile.getInputStream()).thenAnswer(invocation -> new ByteArrayInputStream(validImageBytes));
		
		List<MultipartFile> files = List.of(mockFile);
		
		// 실행
		ProductEntity result = adminProductService.createProduct(dto, files);
		
		// 검증
		assertNotNull(result);
		assertEquals(100L, result.getPrdNo());
		
		// 부가기능(옵션, 이미지)들이 정상적으로 DB에 저장(save) 요청을 보냈는지 호출 횟수로 검증
		verify(prdRepo, times(1)).save(any(ProductEntity.class));
		verify(optionRepo, times(1)).save(any(ProductOptionEntity.class));
		verify(prdImgRepo, times(1)).save(any(ProductImageEntity.class));
	}
	
	@Test
	@DisplayName("상품 수정 로직(옵션, 이미지, 카테고리 변경 포함) 성공 테스트")
	void updateProduct_Success() throws Exception {
		// 준비
		Long prdNo = 1L;
		ReflectionTestUtils.setField(adminProductService, "rootDir", "test-dir/");
		
		// 기존 카테고리 세팅
		CategoryEntity oldCategory = new CategoryEntity();
		oldCategory.setCategoryNo(1L);
		
		// 기존 상품 엔티티 세팅 (DB 저장 상태 가정)
		ProductEntity existingProduct = new ProductEntity();
		existingProduct.setPrdNo(prdNo);
		existingProduct.setPrdName("기존 상품명");
		existingProduct.setPrdPrice(10000);
		existingProduct.setCategory(oldCategory);
		
		// 기존 옵션 1개
		ProductOptionEntity oldOption = new ProductOptionEntity();
		oldOption.setOptionNo(10L);
		oldOption.setOptionName("기존 용량");
		oldOption.setOptionValue("50ml");
		List<ProductOptionEntity> existingOptions = new ArrayList<>();
		existingOptions.add(oldOption);
		existingProduct.setOptions(existingOptions);
		
		// 기존 이미지 2개 존재
		ProductImageEntity oldImage1 = new ProductImageEntity();
		oldImage1.setImageUrl("http://localhost:8080/images/products/old1.jpg");
		ProductImageEntity oldImage2 = new ProductImageEntity();
		oldImage2.setImageUrl("http://localhost:8080/images/products/old2.jpg");
		List<ProductImageEntity> existingImages = new ArrayList<>(List.of(oldImage1, oldImage2));
		existingProduct.setImages(existingImages);
		
		// 상품 조회 시 기존 상품 반환
		when(prdRepo.findById(prdNo)).thenReturn(java.util.Optional.of(existingProduct));
		
		// 새 카테고리로 변경한다고 가정
		CategoryEntity newCategory = new CategoryEntity();
		newCategory.setCategoryNo(2L);
		when(catRepo.findById(2L)).thenReturn(java.util.Optional.of(newCategory));
		
		// 수정을 요청할 DTO 세팅
		ProductSaveDTO dto = new ProductSaveDTO();
		dto.setPrdName("수정된 상품명");
		dto.setPrdPrice(15000);
		dto.setCategoryNo(2L); // 1번에서 2번 카테고리로 변경 요청
		
		// 기존 옵션(10L)은 이름 수정, 새로운 옵션(옵션번호 X)은 추가
		ProductSaveDTO.OptionDTO updateOptDto = new ProductSaveDTO.OptionDTO();
		updateOptDto.setOptionNo(10L);
		updateOptDto.setOptionName("수정된 용량");
		ProductSaveDTO.OptionDTO newOptDto = new ProductSaveDTO.OptionDTO();
		newOptDto.setOptionName("새로운 색상");
		dto.setOptions(List.of(updateOptDto, newOptDto));
		
		// 이미지 요청: old1.jpg는 유지, old2.jpg는 목록에 없으므로 삭제, 그리고 새 파일 추가
		dto.setKeptImageUrls(List.of("http://localhost:8080/images/products/old1.jpg", "NEW_FILE"));
		
		// Mock 이미지 파일 세팅
		MultipartFile mockFile = mock(MultipartFile.class);
		when(mockFile.isEmpty()).thenReturn(false);
		when(mockFile.getContentType()).thenReturn("image/jpeg");
		when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("new.jpg");
        byte[] validImageBytes = java.util.Base64.getDecoder().decode("R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==");
        when(mockFile.getInputStream()).thenAnswer(i -> new java.io.ByteArrayInputStream(validImageBytes));
        
        List<MultipartFile> files = List.of(mockFile);
        
        // 실행
        ProductEntity result = adminProductService.updateProduct(prdNo, dto, files);
        
        // 검증
        // 기본 정보 및 카테고리가 잘 수정되었는지?
        assertEquals("수정된 상품명", result.getPrdName());
        assertEquals(15000, result.getPrdPrice());
        assertEquals(2L, result.getCategory().getCategoryNo());
        
        // 옵션 처리가 잘 진행되었는지? (기존 1개 수정 + 신규 1개 추가 = 총 2개)
        assertEquals(2, result.getOptions().size());
        assertEquals("수정된 용량", result.getOptions().get(0).getOptionName());
        assertEquals("새로운 색상", result.getOptions().get(1).getOptionName());
        
        // 이미지 처리가 잘 호출되었는지?
        // old2.jpg 가 삭제 대상이 되어 deleteAll이 1번 호출되었는지 검증
        verify(prdImgRepo, times(1)).deleteAll(anyList());
        // NEW_FILE 처리를 위해 saveSingleImage가 작동하여 save가 1번 호출되었는지 검증
        verify(prdImgRepo, times(1)).save(any(ProductImageEntity.class));
	}
}
