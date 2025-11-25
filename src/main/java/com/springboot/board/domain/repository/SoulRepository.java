package com.springboot.board.domain.repository;

import com.springboot.board.domain.entity.SoulEntity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SoulRepository extends JpaRepository<SoulEntity, Integer> {

    @Query("SELECT DISTINCT s FROM SoulEntity s LEFT JOIN s.keywords k " +
            "WHERE s.name LIKE %:query% " +
            "OR s.seasonName LIKE %:query% " +
            "OR k LIKE %:query% " +
            "ORDER BY s.startDate DESC, s.name DESC")
    List<SoulEntity> searchSouls(@Param("query") String query);

    @EntityGraph(attributePaths = { "images" })
    @Query("SELECT s FROM SoulEntity s WHERE s.id = :id")
    Optional<SoulEntity> findWithImagesById(@Param("id") Integer id);
}