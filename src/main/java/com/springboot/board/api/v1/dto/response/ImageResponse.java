// src/main/java/com/springboot/board/api/v1/dto/response/ImageResponse.java
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

    /** Entity → DTO 변환 시 URL 절대경로 보정 */
    public static ImageResponse fromEntity(ImageEntity img) {
        String raw = img.getUrl();
        String fullUrl;
        if (raw == null || raw.isBlank()) {
            fullUrl = null;
        } else if (raw.startsWith("http")) {
            fullUrl = raw;
        } else {
            fullUrl = "https://korea-sky-planner.com" + raw;
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
}
