// src/main/java/com/springboot/board/application/service/ImageService.java
package com.springboot.board.application.service;

import com.springboot.board.common.exception.DataNotFoundException;
import com.springboot.board.domain.entity.ImageEntity;
import com.springboot.board.domain.entity.SoulEntity;
import com.springboot.board.domain.repository.ImageRepository;
import com.springboot.board.domain.repository.SoulRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;
    private final SoulRepository soulRepository;

    /** application.yml 에서 주입 **/
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:https://korea-sky-planner.com}")
    private String baseUrl;

    private Path uploadPath;

    /** uploadDir 프로퍼티 기반으로 실제 Path 객체 생성 */
    @PostConstruct
    public void init() throws IOException {
        uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Upload directory created: {}", uploadPath.toAbsolutePath());
        }
        log.info("Image service initialized - upload path: {}, base URL: {}", 
                 uploadPath.toAbsolutePath(), baseUrl);
    }

    /** 업로드 */
    @Transactional
    public ImageEntity upload(Integer soulId, String imageType, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 파일 크기 검증 (예: 10MB 제한)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 10MB까지 허용됩니다.");
        }

        // 이미지 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        log.info("Starting image upload - soulId: {}, imageType: {}, fileName: {}, size: {}",
                 soulId, imageType, file.getOriginalFilename(), file.getSize());

        // 파일명 생성
        String ext = getExtension(file.getOriginalFilename());
        String uniqueName = UUID.randomUUID() + ext;
        Path target = uploadPath.resolve(uniqueName);
        
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved successfully: {}", target.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save file: {}", e.getMessage());
            throw new IOException("파일 저장에 실패했습니다: " + e.getMessage());
        }

        // DB에 저장할 엔티티 준비
        ImageEntity entity = ImageEntity.builder()
                .imageType(imageType.trim().toUpperCase())
                .fileName(uniqueName)
                .url("/" + uploadDir + "/" + uniqueName) // 상대 경로로 저장
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        // soulId 연관관계 처리
        if (soulId != null) {
            SoulEntity soul = soulRepository.findById(soulId)
                    .orElseThrow(() -> new DataNotFoundException("영혼이 존재하지 않습니다. id=" + soulId));
            entity.setSoul(soul);
            log.info("Soul association added - soulId: {}", soulId);
        }

        ImageEntity savedEntity = imageRepository.save(entity);
        log.info("Image entity saved successfully - id: {}", savedEntity.getId());
        
        return savedEntity;
    }

    /** 교체(수정) */
    @Transactional
    public ImageEntity replace(Long id, MultipartFile newFile) throws IOException {
        if (newFile.isEmpty()) {
            throw new IllegalArgumentException("교체할 파일이 없습니다.");
        }

        ImageEntity existing = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. id=" + id));

        log.info("Starting image replacement - id: {}, oldFileName: {}, newFileName: {}",
                 id, existing.getFileName(), newFile.getOriginalFilename());

        // 이전 파일 삭제
        try {
            Path oldFile = uploadPath.resolve(existing.getFileName());
            Files.deleteIfExists(oldFile);
            log.info("Old file deleted: {}", oldFile.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Failed to delete old file: {}", e.getMessage());
        }

        // 새 파일 저장
        String ext = getExtension(newFile.getOriginalFilename());
        String uniqueName = UUID.randomUUID() + ext;
        Path target = uploadPath.resolve(uniqueName);
        
        try {
            Files.copy(newFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("New file saved successfully: {}", target.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save new file: {}", e.getMessage());
            throw new IOException("새 파일 저장에 실패했습니다: " + e.getMessage());
        }

        // 엔티티 업데이트
        existing.setFileName(uniqueName);
        existing.setUrl("/" + uploadDir + "/" + uniqueName);
        existing.setFileSize(newFile.getSize());
        existing.setUploadedAt(LocalDateTime.now());
        
        log.info("Image replacement completed - id: {}", id);
        return existing;
    }

    /** 삭제 */
    @Transactional
    public void delete(Long id) throws IOException {
        ImageEntity img = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. id=" + id));

        log.info("Starting image deletion - id: {}, fileName: {}", id, img.getFileName());

        // 파일 삭제
        try {
            Path fileToDelete = uploadPath.resolve(img.getFileName());
            boolean deleted = Files.deleteIfExists(fileToDelete);
            if (deleted) {
                log.info("File deleted successfully: {}", fileToDelete.toAbsolutePath());
            } else {
                log.warn("File not found or already deleted: {}", fileToDelete.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage());
            // 파일 삭제 실패해도 DB는 삭제하도록 진행
        }

        // DB 삭제
        imageRepository.delete(img);
        log.info("Image entity deleted successfully - id: {}", id);
    }

    /** 파일 확장자 추출 */
    private String getExtension(String original) {
        if (original == null || original.trim().isEmpty()) {
            return "";
        }
        int dot = original.lastIndexOf('.');
        return (dot == -1) ? "" : original.substring(dot).toLowerCase();
    }

    /** 이미지 존재 여부 확인 */
    public boolean imageExists(Long id) {
        return imageRepository.existsById(id);
    }

    /** 특정 영혼의 특정 타입 이미지 존재 여부 */
    public boolean imageExists(Integer soulId, String imageType) {
        return imageRepository.existsBySoulIdAndImageType(soulId, imageType);
    }
}