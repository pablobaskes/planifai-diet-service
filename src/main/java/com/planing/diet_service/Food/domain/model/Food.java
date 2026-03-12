package com.planing.diet_service.Food.domain.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Food {
    private Long id;
    private String name;
    private FoodCategory category;
    private Double caloriesPer100g;
    private Double proteinPer100g;
    private Double carbsPer100g;
    private Double fatPer100g;
}