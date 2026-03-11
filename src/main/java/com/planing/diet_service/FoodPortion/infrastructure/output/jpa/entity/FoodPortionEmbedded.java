package com.planing.diet_service.FoodPortion.infrastructure.output.jpa.entity;

import com.planing.diet_service.FoodPortion.domain.model.Unit;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class FoodPortionEmbedded {

    private Long foodId;

    private Double quantity;

    @Enumerated(EnumType.STRING)
    private Unit unit;
}
