package com.planing.diet_service.Recipe.domain.model;

import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.NutritionSummaryEmbedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Recipe {
    private Long id;
    private String name;
    private List<FoodPortion> ingredients = new ArrayList<>();
    private NutritionSummaryEmbedded nutritionSummary;
}