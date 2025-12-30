package com.springboot.board.domain.repository;

import com.springboot.board.domain.entity.SoulEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface SoulRepository extends JpaRepository<SoulEntity, Integer> {

    // ========== 기존 검색 ==========
    @Query("SELECT DISTINCT s FROM SoulEntity s LEFT JOIN s.keywords k " +
            "WHERE s.name LIKE %:query% " +
            "OR s.seasonName LIKE %:query% " +
            "OR k LIKE %:query% " +
            "ORDER BY s.season.startDate DESC, s.orderNum ASC")
    List<SoulEntity> searchSouls(@Param("query") String query);

    // ========== 상세 조회 ==========
    @EntityGraph(attributePaths = { "images" })
    @Query("SELECT s FROM SoulEntity s WHERE s.id = :id")
    Optional<SoulEntity> findWithImagesById(@Param("id") Integer id);

    @EntityGraph(attributePaths = { "images", "travelingVisits", "season" })
    @Query("SELECT s FROM SoulEntity s WHERE s.id = :id")
    Optional<SoulEntity> findByIdWithDetails(@Param("id") Integer id);

    // ========== 시즌별 조회 ==========
    List<SoulEntity> findBySeasonId(Integer seasonId);
    
    // ✅ 시즌 이름으로 필터링 (페이징) - 정렬 수정
    @Query("SELECT s FROM SoulEntity s " +
           "LEFT JOIN FETCH s.season season " +
           "WHERE season.name = :seasonName " +
           "ORDER BY s.orderNum ASC")
    Page<SoulEntity> findBySeasonName(@Param("seasonName") String seasonName, Pageable pageable);

    // ========== 검색 쿼리 (페이징) - 정렬 수정 ==========
    @Query("SELECT DISTINCT s FROM SoulEntity s " +
           "LEFT JOIN FETCH s.season " +
           "LEFT JOIN s.keywords k " +
           "WHERE s.name LIKE %:query% " +
           "OR s.seasonName LIKE %:query% " +
           "OR k LIKE %:query% " +
           "ORDER BY s.season.startDate DESC, s.orderNum ASC")
    Page<SoulEntity> findByNameOrKeywordsContaining(@Param("query") String query, Pageable pageable);

    // ========== 시즌 + 검색 (페이징) - 정렬 수정 ==========
    @Query("SELECT DISTINCT s FROM SoulEntity s " +
           "LEFT JOIN FETCH s.season season " +
           "LEFT JOIN s.keywords k " +
           "WHERE season.name = :seasonName " +
           "AND (s.name LIKE %:query% OR s.seasonName LIKE %:query% OR k LIKE %:query%) " +
           "ORDER BY s.orderNum ASC")
    Page<SoulEntity> findBySeasonNameAndQuery(
        @Param("seasonName") String seasonName, 
        @Param("query") String query, 
        Pageable pageable
    );

    // ========== 시즌별 영혼 개수 ==========
    @Query("SELECT s.season.id as seasonId, COUNT(s) as count " +
           "FROM SoulEntity s GROUP BY s.season.id")
    List<Map<String, Object>> countBySeasonGrouped();

    // ========== 오래된 유랑용 ==========
    @Query("SELECT s FROM SoulEntity s " +
           "LEFT JOIN FETCH s.travelingVisits tv " +
           "LEFT JOIN FETCH s.season " +
           "ORDER BY s.season.orderNum, s.orderNum")
    List<SoulEntity> findAllWithVisits();

    // ========== 유랑 온 적 있는 영혼만 ==========
    @Query("SELECT s FROM SoulEntity s WHERE EXISTS " +
           "(SELECT 1 FROM TravelingVisitEntity tv WHERE tv.soul = s)")
    List<SoulEntity> findAllWithTravelingVisits();

    // ✅ 페이징 처리된 전체 조회 - 정렬 수정 (가장 중요!)
    @Query("SELECT s FROM SoulEntity s " +
           "LEFT JOIN FETCH s.season season " +
           "ORDER BY season.startDate DESC, s.orderNum ASC")
    Page<SoulEntity> findAllWithSeason(Pageable pageable);
}