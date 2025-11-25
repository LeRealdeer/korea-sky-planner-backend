// src/main/java/com/springboot/board/domain/entity/ImageEntity.java
package com.springboot.board.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "soul_image")
public class ImageEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "soul_id")
private SoulEntity soul;

    /** REPRESENTATIVE, LOCATION, WEARING_SHOT, GESTURE 등 */
    @Column(length = 30, nullable = false)
    private String imageType;

    /** 서버에 저장된 파일명(UUID.ext) */
    @Column(nullable = false, unique = true)
    private String fileName;

    /** 클라이언트가 보는 URL */
    @Column(nullable = false, length = 512)
    private String url;

    private long fileSize;            // 바이트
    private LocalDateTime uploadedAt; // 업로드 시각
}
