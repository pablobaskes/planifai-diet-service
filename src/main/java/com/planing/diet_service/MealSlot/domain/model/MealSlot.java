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
    // Selecciona el MealSlot más adecuado de una lista de candidatos
    // según el objetivo calórico de la franja.
    // Devuelve una copia nueva con el dietDay asignado.
    // ─────────────────────────────────────────────────────────
    public static MealSlot selectBest(List<MealSlot> candidates, double calorieTarget, DietDay dietDay) {
        if (candidates == null || candidates.isEmpty()) {
            log.warn("No MealSlot candidates found for type: {}", dietDay);
            return null;
        }

        List<MealSlot> withNutrition = candidates.stream()
                .filter(MealSlot::hasNutritionData)
                .toList();

        if (withNutrition.isEmpty()) {
            log.warn("No MealSlots with nutrition data. Using random fallback.");
            MealSlot fallback = candidates.get((int) (Math.random() * candidates.size()));
            return fallback.copyFor(dietDay);
        }

        double lowerBound = calorieTarget * (1 - TOLERANCE_PCT);
        double upperBound = calorieTarget * (1 + TOLERANCE_PCT);

        List<MealSlot> inRange = withNutrition.stream()
                .filter(ms -> {
                    double cal = ms.getRecipe().getNutritionSummary().getTotalCalories();
                    return cal >= lowerBound && cal <= upperBound;
                })
                .toList();

        List<MealSlot> pool = inRange.isEmpty() ? withNutrition : inRange;

        if (inRange.isEmpty()) {
            log.debug("No MealSlot in calorie range [{}-{}]. Using closest fallback.", lowerBound, upperBound);
        }

        MealSlot selected = pool.stream()
                .min((a, b) -> Double.compare(
                        Math.abs(a.getRecipe().getNutritionSummary().getTotalCalories() - calorieTarget),
                        Math.abs(b.getRecipe().getNutritionSummary().getTotalCalories() - calorieTarget)))
                .orElse(pool.get(0));

        log.debug("Selected MealSlot type={} recipe='{}' cal={} target={}",
                selected.getType(),
                selected.getRecipe().getName(),
                selected.getRecipe().getNutritionSummary().getTotalCalories(),
                calorieTarget);

        return selected.copyFor(dietDay);
    }

    // ─────────────────────────────────────────────────────────
    // Crea una copia de este MealSlot asignándole un DietDay.
    // El original (catálogo) no se modifica.
    // ─────────────────────────────────────────────────────────
    public MealSlot copyFor(DietDay dietDay) {
        MealSlot copy = new MealSlot();
        copy.setType(this.type);
        copy.setRecipe(this.recipe);
        copy.setDietDay(dietDay);
        return copy;
    }

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────
    public boolean hasNutritionData() {
        return recipe != null
                && recipe.getNutritionSummary() != null
                && recipe.getNutritionSummary().getTotalCalories() != null;
    }
}

