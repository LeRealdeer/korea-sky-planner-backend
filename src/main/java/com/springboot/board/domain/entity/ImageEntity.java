package com.springboot.board.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "soul_image")
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ nullable = false 제거 (영혼 생성 전 이미지 업로드 허용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soul_id")
    private SoulEntity soul;

    /**
     * REPRESENTATIVE - 대표 이미지
     * LOCATION - 위치 이미지
     * WEARING - 착용샷
     * NODE_CHART - 노드표
     */
    @Column(length = 30, nullable = false)
    private String imageType;

    @Column(nullable = false, unique = true)
    private String fileName; // UUID.ext

    @Column(nullable = false, length = 512)
    private String url;

    private Long fileSize;
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}