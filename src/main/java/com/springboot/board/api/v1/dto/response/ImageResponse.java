package com.springboot.board.api.v1.dto.response;

import com.springboot.board.domain.entity.ImageEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ImageResponse {

    private Long id;
    private Integer soulId;
    private String imageType;
    private String url;
    private String fileName;
    private Long fileSize;
    private LocalDateTime uploadedAt;

    public static ImageResponse fromEntity(ImageEntity img) {
        if (img == null) {
            return null;
        }

        String rawUrl = img.getUrl();
        String fullUrl;

        if (rawUrl == null || rawUrl.isBlank()) {
            fullUrl = null;
        } else if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            // 이미 전체 URL인 경우 그대로 사용
            fullUrl = rawUrl;
        } else {
            // Railway 백엔드 URL 사용 (korea-sky-planner.com이 아님!)
            fullUrl = "https://korea-sky-planner-backend-production.up.railway.app" + rawUrl;
        }

        return ImageResponse.builder()
                .id(img.getId())
                .soulId(img.getSoul() != null ? img.getSoul().getId() : null)
                .imageType(img.getImageType())
                .url(fullUrl)
                .fileName(img.getFileName())
                .fileSize(img.getFileSize())
                .uploadedAt(img.getUploadedAt())
                .build();
    }

    public static java.util.List<ImageResponse> fromEntities(java.util.List<ImageEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(ImageResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}