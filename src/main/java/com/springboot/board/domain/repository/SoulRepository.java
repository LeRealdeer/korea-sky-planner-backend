package com.springboot.board.domain.repository;

import com.springboot.board.domain.entity.SoulEntity;
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

    // 기존 검색
    @Query("SELECT DISTINCT s FROM SoulEntity s LEFT JOIN s.keywords k " +
            "WHERE s.name LIKE %:query% " +
            "OR s.seasonName LIKE %:query% " +
            "OR k LIKE %:query% " +
            "ORDER BY s.startDate DESC, s.name DESC")
    List<SoulEntity> searchSouls(@Param("query") String query);

    // 상세 조회 (기존)
    @EntityGraph(attributePaths = { "images" })
    @Query("SELECT s FROM SoulEntity s WHERE s.id = :id")
    Optional<SoulEntity> findWithImagesById(@Param("id") Integer id);

    // 상세 조회 (새로운 - 유랑 이력 포함)
    @EntityGraph(attributePaths = { "images", "travelingVisits", "season" })
    @Query("SELECT s FROM SoulEntity s WHERE s.id = :id")
    Optional<SoulEntity> findByIdWithDetails(@Param("id") Integer id);

    // 시즌별 조회
    List<SoulEntity> findBySeasonId(Integer seasonId);

    // 시즌별 영혼 개수
    @Query("SELECT s.season.id as seasonId, COUNT(s) as count " +
           "FROM SoulEntity s GROUP BY s.season.id")
    List<Map<String, Object>> countBySeasonGrouped();

    // 오래된 유랑용
    @Query("SELECT s FROM SoulEntity s " +
           "LEFT JOIN FETCH s.travelingVisits tv " +
           "LEFT JOIN FETCH s.season " +
           "ORDER BY s.season.orderNum, s.orderNum")
    List<SoulEntity> findAllWithVisits();

    // 유랑 온 적 있는 영혼만
    @Query("SELECT s FROM SoulEntity s WHERE EXISTS " +
           "(SELECT 1 FROM TravelingVisitEntity tv WHERE tv.soul = s)")
    List<SoulEntity> findAllWithTravelingVisits();
}