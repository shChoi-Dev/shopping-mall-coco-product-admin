package com.shoppingmallcoco.project.repository.product;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.shoppingmallcoco.project.entity.product.CategoryEntity;
import com.shoppingmallcoco.project.entity.product.ProductEntity;
import com.shoppingmallcoco.project.entity.product.ProductOptionEntity;

@DataJpaTest
@ActiveProfiles("test") // application-test.properties 로드
class ProductOptionRepositoryTest {

	// Repository 주입
	@Autowired 
    private ProductOptionRepository optionRepo;

    @Autowired
    private ProductRepository prdRepo;

    @Autowired
    private CategoryRepository catRepo;

    @Test
    @DisplayName("모든 상품 옵션의 재고 총합(sumTotalStock) 쿼리가 정상 작동하는지 테스트")
    void sumTotalStock_Success() {
        // 준비 - 실제 테스트 DB에 데이터 입력
        // 제약 조건을 맞추기 위해 카테고리 생성 및 저장
        CategoryEntity category = new CategoryEntity();
        category.setCategoryName("테스트 카테고리");
        catRepo.save(category);

        // 상품 생성 및 저장
        ProductEntity product = new ProductEntity();
        product.setPrdName("테스트 상품");
        product.setPrdPrice(10000);
        product.setCategory(category);
        prdRepo.save(product);

        // 재고가 각각 10, 20, 30인 옵션 3개 생성 및 저장 (총합 60이 되어야 함)
        ProductOptionEntity option1 = ProductOptionEntity.create(product, "색상", "레드", 0, 10);
        ProductOptionEntity option2 = ProductOptionEntity.create(product, "색상", "블루", 0, 20);
        ProductOptionEntity option3 = ProductOptionEntity.create(product, "색상", "그린", 0, 30);
        
        optionRepo.save(option1);
        optionRepo.save(option2);
        optionRepo.save(option3);

        // 실행 - 커스텀 쿼리 호출
        Long totalStock = optionRepo.sumTotalStock();

        // 검증 - 10 + 20 + 30 = 60 확인
        assertEquals(60L, totalStock);
    }
}