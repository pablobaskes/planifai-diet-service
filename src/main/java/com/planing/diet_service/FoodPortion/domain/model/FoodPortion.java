package com.planing.diet_service.FoodPortion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FoodPortion {
    private Long foodId;
    private Double quantity;
    private Unit unit;
    private Double weightPerUnit;
}