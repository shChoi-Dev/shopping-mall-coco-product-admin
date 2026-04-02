package com.shoppingmallcoco.project.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shoppingmallcoco.project.dto.auth.MemberResponseDto;
import com.shoppingmallcoco.project.dto.product.ProductSaveDTO;
import com.shoppingmallcoco.project.dto.product.ProductDetailResponseDTO;
import com.shoppingmallcoco.project.entity.product.ProductEntity;
import com.shoppingmallcoco.project.service.auth.MemberService;
import com.shoppingmallcoco.project.service.common.CloudinaryService;
import com.shoppingmallcoco.project.service.product.AdminProductService;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 전용 상품 관리 REST API 컨트롤러
 * 경로: /api/admin/**
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminProductApiController {
	
	private final AdminProductService prdService;
    private final MemberService memberService;
    
    // Cloudinary 서비스 추가
    private final CloudinaryService cloudinaryService;
	
	// 관리자 권한 검증 헬퍼 메서드
	private void checkAdminRole(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			throw new SecurityException("인증이 필요합니다.");
		}
		MemberResponseDto currentMember = memberService.getMemberByMemId(authentication.getName());
		if (currentMember.getRole() == null || 
				(!currentMember.getRole().equals("ADMIN") && !currentMember.getRole().equals("admin"))) {
			throw new SecurityException("관리자 권한이 필요합니다.");
		}
	}
	
	/**
     * API: 관리자 상품 등록
     * [POST] /api/admin/products
     */
	@PostMapping(value = "/products", consumes = { "multipart/form-data" })
	public ResponseEntity<ProductDetailResponseDTO> createProduct(
            @RequestPart(value = "dto") ProductSaveDTO requestDTO,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files,
            Authentication authentication
    ) throws IOException {
		checkAdminRole(authentication); 
		
		ProductEntity createdProduct = prdService.createProduct(requestDTO, files);
        
		int reviewCount = prdService.getReviewCount(createdProduct);
        double averageRating = prdService.getAverageRating(createdProduct);
        
        ProductDetailResponseDTO responseDTO = new ProductDetailResponseDTO(createdProduct, reviewCount, averageRating);
        
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }
	
	/**
     * API: 관리자 상품 수정
     * [PUT] /api/admin/products/{prdNo}
     * - 이미지 파일이 포함될 수 있으므로 multipart/form-data로 전송받음
     */
	@PutMapping(value = "/products/{prdNo}", consumes = { "multipart/form-data" })
    public ResponseEntity<ProductDetailResponseDTO> updateProduct(
    		@PathVariable(value = "prdNo") Long prdNo,
            @RequestPart(value = "dto") ProductSaveDTO requestDTO,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files,
            Authentication authentication
    ) throws IOException {
		checkAdminRole(authentication);
		
		if (prdNo == null || prdNo <= 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
        
        ProductEntity updatedProduct = prdService.updateProduct(prdNo, requestDTO, files);
        
        int reviewCount = prdService.getReviewCount(updatedProduct);
        double averageRating = prdService.getAverageRating(updatedProduct);
        
        return new ResponseEntity<>(
            new ProductDetailResponseDTO(updatedProduct, reviewCount, averageRating), 
            HttpStatus.OK
        );
    }

    /**
     * API: 관리자 상품 삭제 (논리적 삭제)
     * [DELETE] /api/admin/products/{prdNo}
     */
    @DeleteMapping("/products/{prdNo}")
    public ResponseEntity<String> deleteProduct(
    		@PathVariable(value = "prdNo") Long prdNo,
    		Authentication authentication
    		) {
		checkAdminRole(authentication);
		
		if (prdNo == null || prdNo <= 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
        prdService.deleteProduct(prdNo);
        return new ResponseEntity<>("상품 삭제 성공", HttpStatus.OK);
    }
    
    /**
     * API: 대시보드 통계 데이터 조회
     * [GET] /api/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getDashboardStats(Authentication authentication) {
		checkAdminRole(authentication);
		
        Map<String, Long> stats = prdService.getDashboardStats();
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
    
    // 에디터 이미지 업로드 전용 API (Cloudinary)
    @PostMapping("/products/editor/image")
    public ResponseEntity<Map<String, String>> uploadEditorImage(@RequestParam("image") MultipartFile image) {
        try {
        	// 클라우디너리를 사용하고 URL 받아오기
            String secureUrl = cloudinaryService.uploadImage(image);

            // 프론트엔드가 기다리고 있는 JSON 형태로 응답
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", secureUrl); 
            
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
