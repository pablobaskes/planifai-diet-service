package com.planing.diet_service.Recipe.infrastructure.output.jpa.entity;

import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.Recipe.domain.model.NutritionSummary;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
public class RecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    @CollectionTable(
            name = "recipe_ingredients",
            joinColumns = @JoinColumn(name = "recipe_id")
    )
    private List<FoodPortion> ingredients = new ArrayList<>();

    @Embedded
    private NutritionSummary nutritionSummary;
}