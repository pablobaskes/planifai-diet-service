package com.planing.diet_service.Recipe.infrastructure.output.jpa.entity;

import com.planing.diet_service.FoodPortion.infrastructure.output.jpa.entity.FoodPortionEmbedded;
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

    @Column(name = "name")
    private String name;

    @ElementCollection
    @CollectionTable(
            name = "recipe_ingredients",
            joinColumns = @JoinColumn(name = "recipe_id")
    )
    private List<FoodPortionEmbedded> ingredients = new ArrayList<>();

    @Embedded
    private NutritionSummaryEmbedded nutritionSummary;

    @ElementCollection
    @CollectionTable(
            name = "recipe_tags",
            joinColumns = @JoinColumn(name = "recipe_id")
    )
    @Column(name = "tag")
    private List<String> tags;

    @Column(name = "servings")
    private Integer servings;
}