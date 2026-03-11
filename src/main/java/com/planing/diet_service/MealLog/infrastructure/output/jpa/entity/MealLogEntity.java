package com.planing.diet_service.MealLog.infrastructure.output.jpa.entity;

import com.planing.diet_service.FoodPortion.infrastructure.output.jpa.entity.FoodPortionEmbedded;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "meal_logs")
@Getter
@Setter
@NoArgsConstructor
public class MealLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long recipeId;

    private LocalDateTime consumedAt;

    @Embedded
    private FoodPortionEmbedded portion;

    private String notes;
}
