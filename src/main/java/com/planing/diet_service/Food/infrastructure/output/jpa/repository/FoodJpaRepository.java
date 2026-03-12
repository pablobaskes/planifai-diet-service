package com.planing.diet_service.Food.infrastructure.output.jpa.repository;


import com.planing.diet_service.Food.domain.model.FoodCategory;
import com.planing.diet_service.Food.infrastructure.output.jpa.entity.FoodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodJpaRepository extends JpaRepository<FoodEntity, Long> {

    List<FoodEntity> findByCategory(FoodCategory category);

    List<FoodEntity> findByNameContainingIgnoreCase(String name);

    List<FoodEntity> findByCategoryAndNameContainingIgnoreCase(FoodCategory category, String name);
}
