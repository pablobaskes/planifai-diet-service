package com.planing.diet_service.MealSlot.domain.model;

import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

import static com.planing.diet_service.MealSlot.domain.utils.DietConstants.TOLERANCE_PCT;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class MealSlot {

    private Long id;
    private MealType type;
    private Recipe recipe;
    private DietDay dietDay;

    // ─────────────────────────────────────────────────────────
    // Selecciona la Recipe más adecuada según el objetivo calórico,
    // excluyendo las recetas ya usadas en la semana para ese MealType.
    // Si tras excluir no quedan candidatos, permite repetición (fallback).
    // ─────────────────────────────────────────────────────────
    public static MealSlot selectBest(List<Recipe> candidates,
                                      MealType type,
                                      double calorieTarget,
                                      DietDay dietDay,
                                      Set<Long> excludedIds) {
        if (candidates == null || candidates.isEmpty()) {
            log.warn("No Recipe candidates found for type: {}", type);
            return null;
        }

        // Excluir recetas ya usadas esta semana para este MealType
        List<Recipe> available = candidates.stream()
                .filter(r -> !excludedIds.contains(r.getId()))
                .toList();

        // Si no quedan disponibles, permitir repetición
        if (available.isEmpty()) {
            log.warn("All recipes already used for type {}. Allowing repetition as fallback.", type);
            available = candidates;
        }

        List<Recipe> withNutrition = available.stream()
                .filter(MealSlot::hasNutritionData)
                .toList();

        Recipe selected;

        if (withNutrition.isEmpty()) {
            log.warn("No recipes with nutrition data for type {}. Using random fallback.", type);
            selected = available.get((int) (Math.random() * available.size()));
        } else {
            double lowerBound = calorieTarget * (1 - TOLERANCE_PCT);
            double upperBound = calorieTarget * (1 + TOLERANCE_PCT);

            List<Recipe> inRange = withNutrition.stream()
                    .filter(r -> {
                        double cal = r.getNutritionSummary().getTotalCalories();
                        return cal >= lowerBound && cal <= upperBound;
                    })
                    .toList();

            List<Recipe> pool = inRange.isEmpty() ? withNutrition : inRange;

            if (inRange.isEmpty()) {
                log.debug("No recipe in range [{}-{}] for type {}. Using closest fallback.",
                        lowerBound, upperBound, type);
            }

            selected = pool.stream()
                    .min((a, b) -> Double.compare(
                            Math.abs(a.getNutritionSummary().getTotalCalories() - calorieTarget),
                            Math.abs(b.getNutritionSummary().getTotalCalories() - calorieTarget)))
                    .orElse(pool.get(0));

            log.debug("Selected recipe='{}' cal={} target={} excludedCount={}",
                    selected.getName(),
                    selected.getNutritionSummary().getTotalCalories(),
                    calorieTarget,
                    excludedIds.size());
        }

        MealSlot mealSlot = new MealSlot();
        mealSlot.setType(type);
        mealSlot.setRecipe(selected);
        mealSlot.setDietDay(dietDay);
        return mealSlot;
    }

    // ─────────────────────────────────────────────────────────
    // Devuelve las calorías reales de la receta asignada.
    // Usado por el useCase para el balance calórico diario.
    // ─────────────────────────────────────────────────────────
    public double getCalories() {
        if (!hasNutritionData(recipe)) return 0.0;
        return recipe.getNutritionSummary().getTotalCalories();
    }

    private static boolean hasNutritionData(Recipe recipe) {
        return recipe != null
                && recipe.getNutritionSummary() != null
                && recipe.getNutritionSummary().getTotalCalories() != null;
    }
}
