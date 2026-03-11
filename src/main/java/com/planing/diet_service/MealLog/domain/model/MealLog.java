package com.planing.diet_service.MealLog.domain.model;

import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;

import java.time.LocalDateTime;

public class MealLog {
    private Long id;
    private Long recipeId;
    private LocalDateTime consumedAt;
    private FoodPortion portion;
    private String notes;
}