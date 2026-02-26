package com.shoppingmallcoco.project.service.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shoppingmallcoco.project.entity.product.CategoryEntity;
import com.shoppingmallcoco.project.repository.product.CategoryRepository;

@ExtendWith(MockitoExtension.class) // Mockito 프레임워크 사용 선언
class CategoryServiceTest {

	@Mock // Mock객체 Repository
	private CategoryRepository categoryRepository;
	
	@InjectMocks // Mock들을 주입받아 실제로 테스트할 Service 객체
	private CategoryService categoryService;
	
	@Test
	@DisplayName("최상위 카테고리 생성 테스트")
	void createCateogory_WithoutParent() {
		// 준비
		String categoryName = "스킨케어";
		
		CategoryEntity savedEntity = new CategoryEntity();
		savedEntity.setCategoryNo(1L);
		savedEntity.setCategoryName(categoryName);
		
		// Mock Repository가 save 메서드 호출 시, 만든 savedEntity를 반환하도록 설정
		when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(savedEntity);
		
		// 실행 |  부모 카테고리가 없으므로 null을 전달
		CategoryEntity result = categoryService.createCategory(categoryName, null);
		
		// 검증
		assertNotNull(result);
		assertEquals(categoryName, result.getCategoryName());
		assertNull(result.getParentCategory());
		
		// categoryRepository.save()가 1번 호출 되었는지 검증
		verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
	}
	
	@Test
	@DisplayName("하위 카테고리 생성 테스트")
	void createCategory_WithParent() {
		// 준비
		String childCategoryName = "토너/스킨";
		Long parentNo = 1L;
		
		// 부모 엔티티 셋팅
		CategoryEntity parentEntity = new CategoryEntity();
		parentEntity.setCategoryNo(parentNo);
		parentEntity.setCategoryName("스킨케어");
		
		// 저장될 자식 엔티티 셋팅
		CategoryEntity savedChildEntity = new CategoryEntity();
		savedChildEntity.setCategoryNo(2L);
		savedChildEntity.setCategoryName(childCategoryName);
		savedChildEntity.setParentCategory(parentEntity);
		
		// 부모 카테고리 번호 조회시 parentEntity를 반환하도록 Mock 동작 설정
		when(categoryRepository.findById(parentNo)).thenReturn(Optional.of(parentEntity));
		// save 호출 시 자식 엔티티 반환 설정
		when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(savedChildEntity);
		
		// 실행
		CategoryEntity result = categoryService.createCategory(childCategoryName, parentNo);
		
		// 검증
		assertNotNull(result);
		assertEquals(childCategoryName, result.getCategoryName());
		assertNotNull(result.getParentCategory());
		assertEquals(parentNo, result.getParentCategory().getCategoryNo());
	}
	
	@Test
	@DisplayName("존재하지 않는 부모 카테고리 번호로 생성 시 예외 발생 테스트")
	void createCategory_Fail_ParentNotFound() {
		// 준비
		Long invalidParentNo = 999L;
		
		// 없는 번호를 조회하면 빈 Optional.empty()를 반환하도록 설정
		when(categoryRepository.findById(invalidParentNo)).thenReturn(Optional.empty());
		
		// 실행 및 예외 검증 | 해당 로직 실행 시 RuntimeException 에러가 출력되야 성공 
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			categoryService.createCategory("오류테스트", invalidParentNo);
		});
		
		// 예외 메시지가 설정한 메시지와 동일한지 확인
		assertEquals("부모 카테고리를 찾을 수 없습니다.", exception.getMessage());
	}

}
