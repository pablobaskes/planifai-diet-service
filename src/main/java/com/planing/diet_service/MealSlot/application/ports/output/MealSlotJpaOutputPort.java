package com.planing.diet_service.MealSlot.application.ports.output;

import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.domain.model.Recipe;

import java.util.List;
import java.util.Optional;

public interface MealSlotJpaOutputPort {

    List<Recipe> findRecipesByMealType(MealType mealType);

    // Persiste un MealSlot nuevo (con dietDayId ya asignado)
    MealSlot saveMealSlot(MealSlot mealSlot);

    Optional<MealSlot> findMealSlotById(Long id);

}
