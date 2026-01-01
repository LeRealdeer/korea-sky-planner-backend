package com.springboot.board.domain.repository;

import com.springboot.board.domain.entity.TravelingVisitEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TravelingVisitRepository extends JpaRepository<TravelingVisitEntity, Long> {
    
    /**
     * 특정 영혼의 모든 유랑 이력 조회 (visitNumber 오름차순)
     */
    List<TravelingVisitEntity> findBySoulIdOrderByVisitNumberAsc(Integer soulId);
    /**
 * visitNumber > 0인 모든 유랑 이력 조회 (Soul과 Image 함께)
 */
@Query("SELECT DISTINCT v FROM TravelingVisitEntity v " +
       "LEFT JOIN FETCH v.soul s " +
       "LEFT JOIN FETCH s.images " +
       "LEFT JOIN FETCH s.season " +
       "WHERE v.visitNumber IS NOT NULL " +
       "ORDER BY v.endDate DESC")
List<TravelingVisitEntity> findAllValidVisitsWithSoul();
    /**
     * 특정 영혼의 특정 visitNumber 존재 여부 확인
     */
    boolean existsBySoulIdAndVisitNumber(Integer soulId, Integer visitNumber);
    
    /**
     * 모든 유랑 이력 조회 (visitNumber > 0만, Soul과 Image JOIN, 페이징)
     */
    @Query("SELECT DISTINCT v FROM TravelingVisitEntity v " +
           "LEFT JOIN FETCH v.soul s " +
           "LEFT JOIN FETCH s.images " +
           "WHERE v.visitNumber IS NOT NULL")
    Page<TravelingVisitEntity> findAllWithSoulAndImages(Pageable pageable);
    
    // 특정 영혼의 특정 방문 번호 조회 (중복 체크용)
    Optional<TravelingVisitEntity> findBySoulIdAndVisitNumber(Integer soulId, Integer visitNumber);

    // 현재 진행 중인 방문 기록 조회 (오늘 날짜가 startDate와 endDate 사이)
    @Query("SELECT v FROM TravelingVisitEntity v WHERE :today BETWEEN v.startDate AND v.endDate")
    List<TravelingVisitEntity> findCurrentVisits(@Param("today") LocalDate today);

    /**
     * 키워드로 유랑 이력 검색 (visitNumber > 0만, 페이징)
     */
    @Query("SELECT DISTINCT v FROM TravelingVisitEntity v " +
           "LEFT JOIN FETCH v.soul s " +
           "LEFT JOIN FETCH s.images " +
           "LEFT JOIN s.keywords k " +
           "WHERE v.visitNumber IS NOT NULL AND (" +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.seasonName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(k) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<TravelingVisitEntity> searchWithSoulAndImages(@Param("query") String query, Pageable pageable);
}