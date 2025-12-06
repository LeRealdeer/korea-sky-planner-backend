package com.springboot.board.api.v1.controller;

import com.springboot.board.api.v1.dto.response.ImageResponse;
import com.springboot.board.application.service.ImageService;
import com.springboot.board.common.response.ApiResponse;
import com.springboot.board.domain.entity.ImageEntity;
import com.springboot.board.domain.repository.ImageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "Image", description = "이미지 관리 API")
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ImageRepository imageRepository;

    // ✅ 새로운 업로드 엔드포인트 (soulId 선택적)
    @Operation(summary = "이미지 업로드 (영혼 생성/수정용)", 
               description = "영혼의 이미지를 업로드합니다. soulId는 수정 시에만 필요합니다.")
    @PostMapping("/upload")
    public ApiResponse<ImageResponse> uploadForCreation(
            @RequestParam MultipartFile file,
            @RequestParam String imageType) throws Exception {
        
        log.info("Image upload - type: {}", imageType);

        // soulId 없이 임시 업로드 (생성 시)
        ImageEntity img = imageService.uploadWithoutSoul(imageType, file);
        return ApiResponse.success(ImageResponse.fromEntity(img));
    }

    // ✅ 기존 업로드 엔드포인트 (soulId 필수)
    @Operation(summary = "이미지 업로드 (영혼 연결)", 
               description = "특정 영혼에 이미지를 업로드합니다.")
    @PostMapping
    public ApiResponse<ImageResponse> upload(
            @RequestParam Integer soulId,
            @RequestParam String imageType,
            @RequestParam MultipartFile file) throws Exception {
        
        log.info("Image upload - soulId: {}, type: {}", soulId, imageType);

        ImageEntity img = imageService.upload(soulId, imageType, file);
        return ApiResponse.success(ImageResponse.fromEntity(img));
    }

    @Operation(summary = "이미지 교체", description = "기존 이미지를 새 이미지로 교체합니다.")
    @PutMapping("/{id}")
    public ApiResponse<ImageResponse> replace(
            @PathVariable Long id,
            @RequestParam MultipartFile file) throws Exception {
        
        ImageEntity img = imageService.replace(id, file);
        return ApiResponse.success(ImageResponse.fromEntity(img));
    }

    // ✅ URL로 이미지 삭제 (프론트엔드 호환)
    @Operation(summary = "이미지 삭제 (URL 기반)")
    @DeleteMapping
    public ApiResponse<Void> deleteByUrl(@RequestBody DeleteImageRequest request) throws Exception {
        imageService.deleteByUrl(request.getUrl());
        return ApiResponse.success(null);
    }

    @Operation(summary = "이미지 삭제 (ID 기반)")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) throws Exception {
        imageService.delete(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "이미지 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<ImageResponse> getOne(@PathVariable Long id) {
        ImageEntity img = imageRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Image not found. id=" + id));
        return ApiResponse.success(ImageResponse.fromEntity(img));
    }

    @Operation(summary = "이미지 목록 조회", description = "필터링 및 페이징을 지원합니다.")
    @GetMapping
    public ApiResponse<Page<ImageResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Integer soulId,
            @RequestParam(required = false) String imageType,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Sort sort = "asc".equalsIgnoreCase(sortDirection) 
            ? Sort.by("id").ascending() 
            : Sort.by("id").descending();
            
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ImageEntity> entities;

        if (soulId != null && imageType != null && !imageType.trim().isEmpty()) {
            entities = imageRepository.findAllBySoulIdAndImageType(soulId, imageType.trim(), pageable);
        } else if (soulId != null) {
            entities = imageRepository.findAllBySoulId(soulId, pageable);
        } else if (imageType != null && !imageType.trim().isEmpty()) {
            entities = imageRepository.findAllByImageType(imageType.trim(), pageable);
        } else {
            entities = imageRepository.findAll(pageable);
        }

        Page<ImageResponse> responsePage = entities.map(ImageResponse::fromEntity);
        return ApiResponse.success(responsePage);
    }

    @Operation(summary = "특정 영혼의 모든 이미지 조회")
    @GetMapping("/soul/{soulId}")
    public ApiResponse<Page<ImageResponse>> getBySoulId(
            @PathVariable Integer soulId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ImageEntity> entities = imageRepository.findAllBySoulId(soulId, pageable);
        Page<ImageResponse> responsePage = entities.map(ImageResponse::fromEntity);
        
        return ApiResponse.success(responsePage);
    }

    @Operation(summary = "이미지 타입별 조회")
    @GetMapping("/type/{imageType}")
    public ApiResponse<Page<ImageResponse>> getByImageType(
            @PathVariable String imageType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ImageEntity> entities = imageRepository.findAllByImageType(imageType, pageable);
        Page<ImageResponse> responsePage = entities.map(ImageResponse::fromEntity);
        
        return ApiResponse.success(responsePage);
    }

    // ✅ 요청 DTO
    @lombok.Data
    public static class DeleteImageRequest {
        private String url;
    }
}