package com.planing.diet_service.Recipe.infrastructure.output.jpa.repository;

import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeJpaRepository extends JpaRepository<RecipeEntity, Long> {
}
