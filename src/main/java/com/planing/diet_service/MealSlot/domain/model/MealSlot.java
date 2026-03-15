package com.planing.diet_service.MealSlot.domain.model;

import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.domain.model.Recipe;

public class MealSlot {
    private Long id;
    private MealType type;
    private Recipe recipe;
    private DietDay dietDay;
}
