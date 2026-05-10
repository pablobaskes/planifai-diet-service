package com.planing.diet_service.InventoryItem.infrastructure.output.jpa.repository;

import com.planing.diet_service.InventoryItem.infrastructure.output.jpa.entity.InventoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryItemJpaRepository extends JpaRepository<InventoryItemEntity, Long> {

    @Query("SELECT i FROM InventoryItemEntity i WHERE i.portion.foodId = :foodId")
    Optional<InventoryItemEntity> findByFoodId(@Param("foodId") Long foodId);
}
