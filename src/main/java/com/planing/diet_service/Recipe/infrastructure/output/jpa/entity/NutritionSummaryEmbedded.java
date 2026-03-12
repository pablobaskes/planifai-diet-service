package com.planing.diet_service.Recipe.infrastructure.output.jpa.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class NutritionSummaryEmbedded {

    private Double totalCalories;

    private Double totalProtein;

    private Double totalCarbs;

    private Double totalFat;
}