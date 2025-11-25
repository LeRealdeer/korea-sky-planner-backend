// src/main/java/com/springboot/board/domain/repository/ImageRepository.java
package com.springboot.board.domain.repository;

import com.springboot.board.domain.entity.ImageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    
    /** 영혼 ID와 이미지 타입으로 필터링 **/
    Page<ImageEntity> findAllBySoulIdAndImageType(Integer soulId, String imageType, Pageable pageable);
    
    /** 영혼 ID로 필터링 **/
    Page<ImageEntity> findAllBySoulId(Integer soulId, Pageable pageable);
    
    /** 이미지 타입으로 필터링 **/
    Page<ImageEntity> findAllByImageType(String imageType, Pageable pageable);
    
    /** 영혼 ID로 모든 이미지 조회 (리스트) **/
    List<ImageEntity> findAllBySoulIdOrderByIdDesc(Integer soulId);
    
    /** 이미지 타입으로 모든 이미지 조회 (리스트) **/
    List<ImageEntity> findAllByImageTypeOrderByIdDesc(String imageType);
    
    /** 영혼 ID와 이미지 타입으로 조회 (리스트) **/
    List<ImageEntity> findAllBySoulIdAndImageTypeOrderByIdDesc(Integer soulId, String imageType);
    
    /** 특정 영혼의 특정 타입 이미지 개수 **/
    long countBySoulIdAndImageType(Integer soulId, String imageType);
    
    /** 특정 영혼의 모든 이미지 개수 **/
    long countBySoulId(Integer soulId);
    
    /** 특정 타입의 모든 이미지 개수 **/
    long countByImageType(String imageType);
    
    /** 이미지 존재 여부 확인 **/
    boolean existsBySoulIdAndImageType(Integer soulId, String imageType);
    
    /** 커스텀 쿼리 - 파일명으로 검색 **/
    @Query("SELECT i FROM ImageEntity i WHERE i.fileName LIKE %:fileName%")
    Page<ImageEntity> findByFileNameContaining(@Param("fileName") String fileName, Pageable pageable);
    
    /** 커스텀 쿼리 - 업로드 날짜 범위로 검색 **/
    @Query("SELECT i FROM ImageEntity i WHERE DATE(i.uploadedAt) = CURRENT_DATE")
    Page<ImageEntity> findTodaysImages(Pageable pageable);
}