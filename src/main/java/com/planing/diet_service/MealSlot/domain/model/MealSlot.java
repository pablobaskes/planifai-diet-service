package com.planing.diet_service.MealSlot.domain.model;

import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
    // Selecciona la Recipe más adecuada según el objetivo calórico
    // y construye un MealSlot nuevo (sin id) listo para persistir.
    // ─────────────────────────────────────────────────────────
    public static MealSlot selectBest(List<Recipe> candidates, MealType type,
                                      double calorieTarget, DietDay dietDay) {
        if (candidates == null || candidates.isEmpty()) {
            log.warn("No Recipe candidates found for type: {}", type);
            return null;
        }

        List<Recipe> withNutrition = candidates.stream()
                .filter(MealSlot::hasNutritionData)
                .toList();

        Recipe selected;

        if (withNutrition.isEmpty()) {
            log.warn("No recipes with nutrition data for type {}. Using random fallback.", type);
            selected = candidates.get((int) (Math.random() * candidates.size()));
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

            log.debug("Selected recipe='{}' cal={} target={}",
                    selected.getName(),
                    selected.getNutritionSummary().getTotalCalories(),
                    calorieTarget);
        }

        MealSlot mealSlot = new MealSlot();
        mealSlot.setType(type);
        mealSlot.setRecipe(selected);
        mealSlot.setDietDay(dietDay);
        return mealSlot;
    }

    private static boolean hasNutritionData(Recipe recipe) {
        return recipe != null
                && recipe.getNutritionSummary() != null
                && recipe.getNutritionSummary().getTotalCalories() != null;
    }
}

