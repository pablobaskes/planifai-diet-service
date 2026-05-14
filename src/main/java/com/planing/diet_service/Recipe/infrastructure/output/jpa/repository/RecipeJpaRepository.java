package com.planing.diet_service.Recipe.infrastructure.output.jpa.repository;

import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeJpaRepository extends JpaRepository<RecipeEntity, Long> {

    List<RecipeEntity> findByMealType(MealType mealType);

    @Query("""
            SELECT DISTINCT r FROM RecipeEntity r
            LEFT JOIN FETCH r.ingredients
            """)
    List<RecipeEntity> findAllWithIngredients();

    @Query("""
            SELECT DISTINCT r FROM RecipeEntity r
            LEFT JOIN FETCH r.ingredients
            WHERE r.id = :recipeId
            """)
    Optional<RecipeEntity> findByIdWithIngredients(@Param("recipeId") Long recipeId);

    @Query("""
            SELECT DISTINCT r FROM RecipeEntity r
            LEFT JOIN FETCH r.ingredients
            WHERE r.mealType = :mealType
            """)
    List<RecipeEntity> findByMealTypeWithIngredients(@Param("mealType") MealType mealType);

    @Query("""
            SELECT DISTINCT r FROM RecipeEntity r
            LEFT JOIN FETCH r.ingredients
            WHERE r.id IN :recipeIds
            """)
    List<RecipeEntity> findByIdInWithIngredients(@Param("recipeIds") List<Long> recipeIds);

    @Query("""
            SELECT DISTINCT r FROM RecipeEntity r
            LEFT JOIN FETCH r.tags
            WHERE r.id IN :recipeIds
            """)
    List<RecipeEntity> findByIdInWithTags(@Param("recipeIds") List<Long> recipeIds);

}
