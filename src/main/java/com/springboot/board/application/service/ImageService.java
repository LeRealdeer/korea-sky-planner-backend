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

    // ✅ Soul 없이 이미지 업로드 (영혼 생성 시)
    @Transactional
    public ImageEntity uploadWithoutSoul(String imageType, MultipartFile file) throws IOException {
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

        log.info("Starting upload without soul - type: {}, file: {}", imageType, file.getOriginalFilename());

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

        // DB 엔티티 생성 (soul 없이)
        ImageEntity entity = ImageEntity.builder()
                .soul(null) // ← soul 없이 저장
                .imageType(imageType.trim().toUpperCase())
                .fileName(uniqueName)
                .url("/" + uploadDir + "/" + uniqueName)
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        ImageEntity saved = imageRepository.save(entity);
        log.info("Image saved successfully without soul - id: {}", saved.getId());

        return saved;
    }

    // ✅ 기존 메소드 (Soul과 함께 업로드)
    @Transactional
    public ImageEntity upload(Integer soulId, String imageType, MultipartFile file) throws IOException {
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

        log.info("Starting upload - soulId: {}, type: {}, file: {}",
                soulId, imageType, file.getOriginalFilename());

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

        // Soul 조회
        SoulEntity soul = soulRepository.findById(soulId)
                .orElseThrow(() -> new DataNotFoundException("영혼을 찾을 수 없습니다. id=" + soulId));

        // DB 엔티티 생성
        ImageEntity entity = ImageEntity.builder()
                .soul(soul)
                .imageType(imageType.trim().toUpperCase())
                .fileName(uniqueName)
                .url("/" + uploadDir + "/" + uniqueName)
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

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

    // ✅ URL 기반 삭제 메소드 추가
    @Transactional
    public void deleteByUrl(String url) throws IOException {
        // URL에서 파일명 추출
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        
        ImageEntity img = imageRepository.findByFileName(fileName)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. url=" + url));

        // 파일 삭제
        try {
            Path fileToDelete = uploadPath.resolve(img.getFileName());
            Files.deleteIfExists(fileToDelete);
            log.info("File deleted: {}", fileToDelete.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage());
        }

        // DB 삭제
        imageRepository.delete(img);
        log.info("Image deleted from DB: {}", img.getId());
    }

    private String getExtension(String original) {
        if (original == null || original.trim().isEmpty()) {
            return "";
        }
        int dot = original.lastIndexOf('.');
        return (dot == -1) ? "" : original.substring(dot).toLowerCase();
    }
}