package com.planing.diet_service.Recipe.infrastructure.output.jpa.repository;

import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeJpaRepository extends JpaRepository<RecipeEntity, Long> {

    List<RecipeEntity> findByMealType(MealType mealType);

}
