package com.planing.diet_service.Recipe.domain.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class NutritionSummary {

    private Double calories;

    private Double protein;

    private Double carbs;

    private Double fat;
}