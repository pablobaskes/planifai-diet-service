package com.planing.diet_service.MealSlot.infrastructure.output.jpa.entity;

import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietDayEntity;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.RecipeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meal_slots")
@Getter
@Setter
@NoArgsConstructor
public class MealSlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MealType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private RecipeEntity recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_day_id")
    private DietDayEntity dietDay;
}
