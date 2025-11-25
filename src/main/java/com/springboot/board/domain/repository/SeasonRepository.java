package com.springboot.board.domain.repository;

import com.springboot.board.domain.entity.SeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<SeasonEntity, Integer> {

    Optional<SeasonEntity> findByName(String name);

    List<SeasonEntity> findAllByOrderByOrderNumAsc();

    List<SeasonEntity> findAllByIsCollaboration(boolean isCollaboration);

    boolean existsByOrderNum(Integer orderNum);
}