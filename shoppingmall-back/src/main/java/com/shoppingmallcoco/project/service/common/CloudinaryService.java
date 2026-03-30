package com.shoppingmallcoco.project.service.common;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
	
	private final Cloudinary cloudinary;
	
	// 프론트엔드에서 파일을 받아 클라우드로 올리고, 완료된 URL을 문자열로 반환
	public String uploadImage(MultipartFile file) throws IOException {
		
		// 클라우디너리에 파일 업로드 실행
		Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
		
		// 업로드 완료 후 생성된 https로 시작하는 절대 주소 추출
		return uploadResult.get("secure_url").toString();
	}
}
