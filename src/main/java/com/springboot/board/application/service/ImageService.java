package com.springboot.board.application.service;

import com.springboot.board.common.exception.DataNotFoundException;
import com.springboot.board.domain.entity.ImageEntity;
import com.springboot.board.domain.entity.SoulEntity;
import com.springboot.board.domain.entity.TravelingVisitEntity;
import com.springboot.board.domain.repository.ImageRepository;
import com.springboot.board.domain.repository.SoulRepository;
import com.springboot.board.domain.repository.TravelingVisitRepository;
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
    private final TravelingVisitRepository travelingVisitRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:https://korea-sky-planner.com}")
    private String baseUrl;

    private Path uploadPath;

    @PostConstruct
    public void init() throws IOException {
        uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Upload directory created: {}", uploadPath.toAbsolutePath());
        }
    }

    /** 기존 메소드 (호환성) */
    @Transactional
    public ImageEntity upload(Integer soulId, String imageType, MultipartFile file) throws IOException {
        return upload(soulId, null, imageType, file);
    }

    /** 새로운 통합 메소드 */
    @Transactional
    public ImageEntity upload(Integer soulId, Long travelingVisitId, String imageType, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 파일 크기 검증 (10MB)
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 10MB까지 허용됩니다.");
        }

        // 이미지 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        log.info("Starting upload - soulId: {}, visitId: {}, type: {}, file: {}", 
                 soulId, travelingVisitId, imageType, file.getOriginalFilename());

        // 파일 저장
        String ext = getExtension(file.getOriginalFilename());
        String uniqueName = UUID.randomUUID() + ext;
        Path target = uploadPath.resolve(uniqueName);
        
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved: {}", target.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save file: {}", e.getMessage());
            throw new IOException("파일 저장에 실패했습니다: " + e.getMessage());
        }

        // DB 엔티티 생성
        ImageEntity entity = ImageEntity.builder()
                .imageType(imageType.trim().toUpperCase())
                .fileName(uniqueName)
                .url("/" + uploadDir + "/" + uniqueName)
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        // 연관관계 설정
        if (travelingVisitId != null) {
            TravelingVisitEntity visit = travelingVisitRepository.findById(travelingVisitId)
                    .orElseThrow(() -> new DataNotFoundException("유랑 방문을 찾을 수 없습니다. id=" + travelingVisitId));
            entity.setTravelingVisit(visit);
        } else if (soulId != null) {
            SoulEntity soul = soulRepository.findById(soulId)
                    .orElseThrow(() -> new DataNotFoundException("영혼을 찾을 수 없습니다. id=" + soulId));
            entity.setSoul(soul);
        }

        ImageEntity saved = imageRepository.save(entity);
        log.info("Image saved successfully - id: {}", saved.getId());
        
        return saved;
    }

    @Transactional
    public ImageEntity replace(Long id, MultipartFile newFile) throws IOException {
        if (newFile.isEmpty()) {
            throw new IllegalArgumentException("교체할 파일이 없습니다.");
        }

        ImageEntity existing = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. id=" + id));

        // 이전 파일 삭제
        try {
            Path oldFile = uploadPath.resolve(existing.getFileName());
            Files.deleteIfExists(oldFile);
        } catch (IOException e) {
            log.warn("Failed to delete old file: {}", e.getMessage());
        }

        // 새 파일 저장
        String ext = getExtension(newFile.getOriginalFilename());
        String uniqueName = UUID.randomUUID() + ext;
        Path target = uploadPath.resolve(uniqueName);
        
        Files.copy(newFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // 엔티티 업데이트
        existing.setFileName(uniqueName);
        existing.setUrl("/" + uploadDir + "/" + uniqueName);
        existing.setFileSize(newFile.getSize());
        existing.setUploadedAt(LocalDateTime.now());
        
        return existing;
    }

    @Transactional
    public void delete(Long id) throws IOException {
        ImageEntity img = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. id=" + id));

        // 파일 삭제
        try {
            Path fileToDelete = uploadPath.resolve(img.getFileName());
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage());
        }

        // DB 삭제
        imageRepository.delete(img);
    }

    private String getExtension(String original) {
        if (original == null || original.trim().isEmpty()) {
            return "";
        }
        int dot = original.lastIndexOf('.');
        return (dot == -1) ? "" : original.substring(dot).toLowerCase();
    }
}