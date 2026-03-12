package com.planing.diet_service.FoodPortion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FoodPortion {
    private Long foodId;
    private Double quantity;
    private Unit unit;
}