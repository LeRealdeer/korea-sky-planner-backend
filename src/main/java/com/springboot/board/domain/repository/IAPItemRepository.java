package com.springboot.board.domain.repository;

import com.springboot.board.domain.entity.IAPItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAPItemRepository extends JpaRepository<IAPItemEntity, Long> {

    List<IAPItemEntity> findBySeasonId(Integer seasonId);

    List<IAPItemEntity> findByPurchaseType(String purchaseType);

    @Query("SELECT DISTINCT i FROM IAPItemEntity i " +
           "LEFT JOIN i.keywords k " +
           "WHERE i.name LIKE %:query% " +
           "OR k LIKE %:query%")
    List<IAPItemEntity> searchItems(@Param("query") String query);
}