package com.planing.diet_service.MealSlot.domain.model;

import com.planing.diet_service.DietDay.domain.model.DietDay;
import com.planing.diet_service.MealSlot.domain.utils.MealType;

public class MealSlot {
    private Long id;
    private MealType type;
    private Long recipeId;
    private DietDay dietDay;
}
