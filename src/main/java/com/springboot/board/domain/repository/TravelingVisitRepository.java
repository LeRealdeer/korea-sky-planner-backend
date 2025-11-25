package com.springboot.board.domain.repository;

import com.springboot.board.domain.entity.TravelingVisitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TravelingVisitRepository extends JpaRepository<TravelingVisitEntity, Long> {

    List<TravelingVisitEntity> findBySoulIdOrderByVisitNumberAsc(Integer soulId);

    Optional<TravelingVisitEntity> findBySoulIdAndVisitNumber(Integer soulId, Integer visitNumber);

    @Query("SELECT tv FROM TravelingVisitEntity tv WHERE tv.endDate >= :date ORDER BY tv.startDate ASC")
    List<TravelingVisitEntity> findUpcomingAndCurrentVisits(@Param("date") LocalDate date);

    @Query("SELECT tv FROM TravelingVisitEntity tv WHERE tv.startDate <= :date AND tv.endDate >= :date")
    List<TravelingVisitEntity> findCurrentVisits(@Param("date") LocalDate date);

    @Query("SELECT MAX(tv.visitNumber) FROM TravelingVisitEntity tv WHERE tv.soul.id = :soulId")
    Integer findMaxVisitNumberBySoulId(@Param("soulId") Integer soulId);
}