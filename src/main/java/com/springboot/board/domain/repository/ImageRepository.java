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

    Page<ImageEntity> findAllBySoulIdAndImageType(Integer soulId, String imageType, Pageable pageable);
    Page<ImageEntity> findAllBySoulId(Integer soulId, Pageable pageable);
    Page<ImageEntity> findAllByImageType(String imageType, Pageable pageable);

    List<ImageEntity> findAllBySoulIdOrderByIdDesc(Integer soulId);
    List<ImageEntity> findAllByImageTypeOrderByIdDesc(String imageType);
    List<ImageEntity> findAllBySoulIdAndImageTypeOrderByIdDesc(Integer soulId, String imageType);

    long countBySoulIdAndImageType(Integer soulId, String imageType);
    long countBySoulId(Integer soulId);
    long countByImageType(String imageType);

    boolean existsBySoulIdAndImageType(Integer soulId, String imageType);

    @Query("SELECT i FROM ImageEntity i WHERE i.fileName LIKE %:fileName%")
    Page<ImageEntity> findByFileNameContaining(@Param("fileName") String fileName, Pageable pageable);

    @Query("SELECT i FROM ImageEntity i WHERE DATE(i.uploadedAt) = CURRENT_DATE")
    Page<ImageEntity> findTodaysImages(Pageable pageable);
}