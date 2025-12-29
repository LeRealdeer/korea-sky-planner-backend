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

        // ✅ Cloudinary URL은 이미 완전한 URL이므로 그대로 사용
        String url = img.getUrl();

        return ImageResponse.builder()
                .id(img.getId())
                .soulId(img.getSoul() != null ? img.getSoul().getId() : null)
                .imageType(img.getImageType())
                .url(url) // Cloudinary URL 그대로 사용
                .fileName(img.getFileName()) // public_id
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