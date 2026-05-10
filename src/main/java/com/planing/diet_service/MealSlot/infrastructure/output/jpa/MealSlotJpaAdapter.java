package com.planing.diet_service.MealSlot.infrastructure.output.jpa;

import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietDayEntity;
import com.planing.diet_service.Diet.infrastructure.output.jpa.repository.DietDayJpaRepository;
import com.planing.diet_service.MealSlot.application.ports.output.MealSlotJpaOutputPort;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.entity.MealSlotEntity;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.mapper.MealSlotJpaMapper;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.repository.MealSlotJpaRepository;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.RecipeEntity;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.mapper.RecipeJpaMapper;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.repository.RecipeJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class MealSlotJpaAdapter implements MealSlotJpaOutputPort {

    private final MealSlotJpaRepository mealSlotJpaRepository;
    private final MealSlotJpaMapper mealSlotJpaMapper;
    private final RecipeJpaRepository recipeJpaRepository;
    private final RecipeJpaMapper recipeJpaMapper;
    private final DietDayJpaRepository dietDayJpaRepository;

    @Override
    public List<Recipe> findRecipesByMealType(MealType mealType) {
        return recipeJpaRepository.findByMealType(mealType)
                .stream()
                .map(recipeJpaMapper::toDomain)
                .toList();
    }

    @Override
    public MealSlot saveMealSlot(MealSlot mealSlot) {
        MealSlotEntity entity = mealSlotJpaMapper.toEntity(mealSlot);

        // Asignar DietDay gestionada por JPA
        if (mealSlot.getDietDay() != null && mealSlot.getDietDay().getId() != null) {
            DietDayEntity dietDayEntity = dietDayJpaRepository
                    .findById(mealSlot.getDietDay().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "DietDay not found with id: " + mealSlot.getDietDay().getId()));
            entity.setDietDay(dietDayEntity);
        }

        // Asignar Recipe gestionada por JPA
        if (mealSlot.getRecipe() != null && mealSlot.getRecipe().getId() != null) {
            RecipeEntity recipeEntity = recipeJpaRepository
                    .findById(mealSlot.getRecipe().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Recipe not found with id: " + mealSlot.getRecipe().getId()));
            entity.setRecipe(recipeEntity);
        }

        return mealSlotJpaMapper.toDomain(mealSlotJpaRepository.save(entity));
    }

    @Override
    public Optional<MealSlot> findMealSlotById(Long id) {
        return mealSlotJpaRepository.findById(id)
                .map(mealSlotJpaMapper::toDomain);
    }
}
