package com.springboot.board.api.v1.dto.response;

import com.springboot.board.domain.entity.ImageEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 이미지 응답 DTO
 */
@Getter
@Builder
public class ImageResponse {
    
    /** 이미지 ID */
    private Long id;
    
    /** 연결된 영혼 ID (nullable) */
    private Integer soulId;
    
    /** 연결된 유랑 방문 ID (nullable) */
    private Long travelingVisitId;
    
    /** 이미지 타입 (REPRESENTATIVE, LOCATION, WEARING_SHOT, NODE_TABLE) */
    private String imageType;
    
    /** 이미지 URL (절대경로) */
    private String url;
    
    /** 파일명 (UUID.ext) */
    private String fileName;
    
    /** 파일 크기 (bytes) */
    private Long fileSize;
    
    /** 업로드 일시 */
    private LocalDateTime uploadedAt;

    /**
     * Entity를 DTO로 변환하는 static factory method
     * 
     * @param img ImageEntity
     * @return ImageResponse
     */
    public static ImageResponse fromEntity(ImageEntity img) {
        if (img == null) {
            return null;
        }

        // URL 절대경로 보정
        String rawUrl = img.getUrl();
        String fullUrl;
        
        if (rawUrl == null || rawUrl.isBlank()) {
            fullUrl = null;
        } else if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            // 이미 절대경로면 그대로
            fullUrl = rawUrl;
        } else {
            // 상대경로면 base URL 추가
            fullUrl = "https://korea-sky-planner.com" + rawUrl;
        }

        return ImageResponse.builder()
                .id(img.getId())
                .soulId(img.getSoul() != null ? img.getSoul().getId() : null)
                .travelingVisitId(img.getTravelingVisit() != null ? img.getTravelingVisit().getId() : null)
                .imageType(img.getImageType())
                .url(fullUrl)
                .fileName(img.getFileName())
                .fileSize(img.getFileSize())
                .uploadedAt(img.getUploadedAt())
                .build();
    }

    /**
     * 여러 Entity를 List<DTO>로 변환
     */
    public static java.util.List<ImageResponse> fromEntities(java.util.List<ImageEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(ImageResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}