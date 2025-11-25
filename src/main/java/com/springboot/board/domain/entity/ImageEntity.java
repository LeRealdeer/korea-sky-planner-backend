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

    // 시즌 영혼의 기본 이미지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soul_id")
    private SoulEntity soul;

    // 특정 유랑 방문의 이미지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traveling_visit_id")
    private TravelingVisitEntity travelingVisit;

    /**
     * REPRESENTATIVE - 대표 이미지
     * LOCATION - 위치 이미지
     * WEARING_SHOT - 착용샷
     * NODE_TABLE - 노드표
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