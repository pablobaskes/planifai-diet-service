package com.planing.diet_service.Food.infrastructure.output.jpa.entity;

import com.planing.diet_service.Food.domain.model.FoodCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "foods")
@Getter
@Setter
@NoArgsConstructor
public class FoodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private FoodCategory category;

    private Double caloriesPer100g;

    private Double proteinPer100g;

    private Double carbsPer100g;

    private Double fatPer100g;
}
