package com.springboot.board.application.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.springboot.board.common.exception.DataNotFoundException;
import com.springboot.board.domain.entity.ImageEntity;
import com.springboot.board.domain.entity.SoulEntity;
import com.springboot.board.domain.repository.ImageRepository;
import com.springboot.board.domain.repository.SoulRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;
    private final SoulRepository soulRepository;
    private final Cloudinary cloudinary; // ✅ Cloudinary 주입

    /**
     * Soul 없이 이미지 업로드 (영혼 생성 시)
     */
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

        log.info("Starting Cloudinary upload without soul - type: {}, file: {}", imageType, file.getOriginalFilename());

        try {
            // ✅ Cloudinary 업로드
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap(
                    "folder", "sky-planner", // Cloudinary 폴더명
                    "resource_type", "image",
                    "transformation", ObjectUtils.asMap(
                        "quality", "auto:good", // 자동 품질 최적화
                        "fetch_format", "auto"  // 자동 포맷 변환 (WebP 등)
                    )
                )
            );

            String cloudinaryUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            
            log.info("Cloudinary upload successful - URL: {}", cloudinaryUrl);

            // DB 엔티티 생성
            ImageEntity entity = ImageEntity.builder()
                    .soul(null)
                    .imageType(imageType.trim().toUpperCase())
                    .fileName(publicId) // Cloudinary public_id 저장
                    .url(cloudinaryUrl) // Cloudinary URL 저장
                    .fileSize(file.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            ImageEntity saved = imageRepository.save(entity);
            log.info("Image saved successfully without soul - id: {}", saved.getId());

            return saved;
            
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new IOException("Cloudinary 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 기존 메소드 (Soul과 함께 업로드)
     */
    @Transactional
    public ImageEntity upload(Integer soulId, String imageType, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 파일 크기 검증
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 10MB까지 허용됩니다.");
        }

        // 이미지 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        log.info("Starting Cloudinary upload - soulId: {}, type: {}, file: {}",
                soulId, imageType, file.getOriginalFilename());

        // Soul 조회
        SoulEntity soul = soulRepository.findById(soulId)
                .orElseThrow(() -> new DataNotFoundException("영혼을 찾을 수 없습니다. id=" + soulId));

        try {
            // ✅ Cloudinary 업로드
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap(
                    "folder", "sky-planner",
                    "resource_type", "image",
                    "transformation", ObjectUtils.asMap(
                        "quality", "auto:good",
                        "fetch_format", "auto"
                    )
                )
            );

            String cloudinaryUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            
            log.info("Cloudinary upload successful - URL: {}", cloudinaryUrl);

            // DB 엔티티 생성
            ImageEntity entity = ImageEntity.builder()
                    .soul(soul)
                    .imageType(imageType.trim().toUpperCase())
                    .fileName(publicId)
                    .url(cloudinaryUrl)
                    .fileSize(file.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            ImageEntity saved = imageRepository.save(entity);
            log.info("Image saved successfully - id: {}", saved.getId());

            return saved;
            
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new IOException("Cloudinary 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 이미지 교체
     */
    @Transactional
    public ImageEntity replace(Long id, MultipartFile newFile) throws IOException {
        if (newFile.isEmpty()) {
            throw new IllegalArgumentException("교체할 파일이 없습니다.");
        }

        ImageEntity existing = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. id=" + id));

        // ✅ 기존 Cloudinary 이미지 삭제
        try {
            cloudinary.uploader().destroy(existing.getFileName(), ObjectUtils.emptyMap());
            log.info("Old Cloudinary image deleted: {}", existing.getFileName());
        } catch (IOException e) {
            log.warn("Failed to delete old Cloudinary image: {}", e.getMessage());
        }

        // ✅ 새 이미지 업로드
        try {
            Map uploadResult = cloudinary.uploader().upload(newFile.getBytes(), 
                ObjectUtils.asMap(
                    "folder", "sky-planner",
                    "resource_type", "image",
                    "transformation", ObjectUtils.asMap(
                        "quality", "auto:good",
                        "fetch_format", "auto"
                    )
                )
            );

            String cloudinaryUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // 엔티티 업데이트
            existing.setFileName(publicId);
            existing.setUrl(cloudinaryUrl);
            existing.setFileSize(newFile.getSize());
            existing.setUploadedAt(LocalDateTime.now());

            log.info("Image replaced successfully - new URL: {}", cloudinaryUrl);
            return existing;
            
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new IOException("Cloudinary 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 이미지 삭제 (ID 기반)
     */
    @Transactional
    public void delete(Long id) throws IOException {
        ImageEntity img = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. id=" + id));

        // ✅ Cloudinary에서 삭제
        try {
            cloudinary.uploader().destroy(img.getFileName(), ObjectUtils.emptyMap());
            log.info("Cloudinary image deleted: {}", img.getFileName());
        } catch (IOException e) {
            log.error("Failed to delete from Cloudinary: {}", e.getMessage());
        }

        // DB 삭제
        imageRepository.delete(img);
        log.info("Image deleted from DB: {}", img.getId());
    }

    /**
     * URL 기반 삭제
     */
    @Transactional
    public void deleteByUrl(String url) throws IOException {
        // URL에서 public_id 추출
        String publicId = extractPublicIdFromUrl(url);
        
        ImageEntity img = imageRepository.findByFileName(publicId)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. url=" + url));

        // ✅ Cloudinary에서 삭제
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Cloudinary image deleted: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete from Cloudinary: {}", e.getMessage());
        }

        // DB 삭제
        imageRepository.delete(img);
        log.info("Image deleted from DB: {}", img.getId());
    }

    /**
     * Cloudinary URL에서 public_id 추출
     * 예: https://res.cloudinary.com/demo/image/upload/v1234567890/sky-planner/abc123.jpg
     * -> sky-planner/abc123
     */
    private String extractPublicIdFromUrl(String url) {
        // "upload/" 이후 부분 추출
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex == -1) {
            return url.substring(url.lastIndexOf('/') + 1);
        }
        
        String afterUpload = url.substring(uploadIndex + 8); // "/upload/" 길이만큼 건너뜀
        
        // 버전 정보 제거 (v1234567890/)
        int versionEnd = afterUpload.indexOf('/', afterUpload.indexOf('/') + 1);
        if (versionEnd == -1) {
            versionEnd = afterUpload.indexOf('/', 1);
        }
        
        String pathWithExt = afterUpload.substring(versionEnd + 1);
        
        // 확장자 제거
        int dotIndex = pathWithExt.lastIndexOf('.');
        return dotIndex == -1 ? pathWithExt : pathWithExt.substring(0, dotIndex);
    }
}