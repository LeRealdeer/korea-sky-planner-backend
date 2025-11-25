// src/main/java/com/springboot/board/api/v1/controller/ImageController.java
package com.springboot.board.api.v1.controller;

import com.springboot.board.api.v1.dto.response.ImageResponse;
import com.springboot.board.application.service.ImageService;
import com.springboot.board.common.response.ApiResponse;
import com.springboot.board.domain.entity.ImageEntity;
import com.springboot.board.domain.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ImageRepository imageRepository;

    /** 1) 업로드 **/
    @PostMapping
    public ApiResponse<ImageResponse> upload(
            @RequestParam(required = false) Integer soulId,
            @RequestParam String imageType,
            @RequestParam MultipartFile file) throws Exception {
        
        log.info("Image upload request - soulId: {}, imageType: {}, fileName: {}", 
                 soulId, imageType, file.getOriginalFilename());

        ImageEntity img = imageService.upload(soulId, imageType, file);
        log.info("Image uploaded successfully - id: {}", img.getId());
        
        return ApiResponse.success(ImageResponse.fromEntity(img));
    }

    /** 2) 교체 **/
    @PutMapping("/{id}")
    public ApiResponse<ImageResponse> replace(
            @PathVariable Long id,
            @RequestParam MultipartFile file) throws Exception {
        
        log.info("Image replace request - id: {}, fileName: {}", id, file.getOriginalFilename());

        ImageEntity img = imageService.replace(id, file);
        log.info("Image replaced successfully - id: {}", img.getId());
        
        return ApiResponse.success(ImageResponse.fromEntity(img));
    }

    /** 3) 삭제 **/
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) throws Exception {
        log.info("Image delete request - id: {}", id);
        
        imageService.delete(id);
        log.info("Image deleted successfully - id: {}", id);
        
        return ApiResponse.success(null);
    }

    /** 4) 한 개만 조회 **/
    @GetMapping("/{id}")
    public ApiResponse<ImageResponse> getOne(@PathVariable Long id) {
        log.info("Get single image request - id: {}", id);
        
        ImageEntity img = imageRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Image not found. id=" + id));
            
        return ApiResponse.success(ImageResponse.fromEntity(img));
    }

    /** 5) 리스트 조회 (개선된 필터링 및 정렬) **/
    @GetMapping
    public ApiResponse<Page<ImageResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Integer soulId,
            @RequestParam(required = false) String imageType,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Image list request - page: {}, size: {}, soulId: {}, imageType: {}", 
                 page, size, soulId, imageType);

        // 정렬 방향 설정
        Sort sort = "asc".equalsIgnoreCase(sortDirection) 
            ? Sort.by("id").ascending() 
            : Sort.by("id").descending();
            
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ImageEntity> entities;

        // 필터링 로직
        if (soulId != null && imageType != null && !imageType.trim().isEmpty()) {
            // 둘 다 있는 경우
            entities = imageRepository.findAllBySoulIdAndImageType(soulId, imageType.trim(), pageable);
        } else if (soulId != null) {
            // soulId만 있는 경우
            entities = imageRepository.findAllBySoulId(soulId, pageable);
        } else if (imageType != null && !imageType.trim().isEmpty()) {
            // imageType만 있는 경우
            entities = imageRepository.findAllByImageType(imageType.trim(), pageable);
        } else {
            // 필터 없음 - 전체 조회
            entities = imageRepository.findAll(pageable);
        }

        Page<ImageResponse> responsePage = entities.map(ImageResponse::fromEntity);
        
        log.info("Image list response - totalElements: {}, totalPages: {}", 
                 responsePage.getTotalElements(), responsePage.getTotalPages());
                 
        return ApiResponse.success(responsePage);
    }

    /** 6) 특정 Soul의 모든 이미지 조회 **/
    @GetMapping("/soul/{soulId}")
    public ApiResponse<Page<ImageResponse>> getBySoulId(
            @PathVariable Integer soulId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("Get images by soulId request - soulId: {}", soulId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ImageEntity> entities = imageRepository.findAllBySoulId(soulId, pageable);
        Page<ImageResponse> responsePage = entities.map(ImageResponse::fromEntity);
        
        return ApiResponse.success(responsePage);
    }

    /** 7) 이미지 타입별 조회 **/
    @GetMapping("/type/{imageType}")
    public ApiResponse<Page<ImageResponse>> getByImageType(
            @PathVariable String imageType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("Get images by imageType request - imageType: {}", imageType);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ImageEntity> entities = imageRepository.findAllByImageType(imageType, pageable);
        Page<ImageResponse> responsePage = entities.map(ImageResponse::fromEntity);
        
        return ApiResponse.success(responsePage);
    }
}