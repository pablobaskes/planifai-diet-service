package com.planing.diet_service.Recipe.application.usecase;

import com.planing.diet_service.Food.application.ports.input.FoodInputPort;
import com.planing.diet_service.Food.domain.exception.FoodNotFoundException;
import com.planing.diet_service.Food.domain.model.Food;
import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.Recipe.application.ports.input.RecipeInputPort;
import com.planing.diet_service.Recipe.application.ports.output.RecipeOutputPort;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.NutritionSummaryEmbedded;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
@Slf4j
public class RecipeUseCase implements RecipeInputPort {

    private final RecipeOutputPort recipeOutputPort;
    private final FoodInputPort foodInputPort;

    @Override
    public List<Recipe> getAllRecipes() {
        log.info("Getting all recipes");
        return recipeOutputPort.findAll();
    }

    @Override
    public Recipe getRecipeById(Long id) {
        log.info("Getting recipe by id: {}", id);
        return recipeOutputPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found with id: " + id));
    }

    @Override
    public Recipe createRecipe(Recipe recipe) {
        log.info("Creating recipe: {}", recipe.getName());
        validateIngredients(recipe);
        recipe.setNutritionSummary(calculateNutrition(recipe));
        return recipeOutputPort.save(recipe);
    }

    @Override
    public Recipe updateRecipe(Long id, Recipe recipe) {
        log.info("Updating recipe with id: {}", id);
        if (!recipeOutputPort.existsById(id)) {
            throw new NoSuchElementException("Recipe not found with id: " + id);
        }
        validateIngredients(recipe);
        recipe.setId(id);
        recipe.setNutritionSummary(calculateNutrition(recipe));
        return recipeOutputPort.save(recipe);
    }

    @Override
    public void deleteRecipe(Long id) {
        log.info("Deleting recipe with id: {}", id);
        if (!recipeOutputPort.existsById(id)) {
            throw new NoSuchElementException("Recipe not found with id: " + id);
        }
        recipeOutputPort.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────
    // Valida que todos los foodId de los ingredientes existen.
    // Lanza FoodNotFoundException con el primer foodId no encontrado.
    // El frontend intercepta el 422 y redirige al formulario de Food.
    // ─────────────────────────────────────────────────────────
    private void validateIngredients(Recipe recipe) {
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Recipe must have at least one ingredient.");
        }

        for (FoodPortion portion : recipe.getIngredients()) {
            if (portion.getFoodId() == null) {
                throw new IllegalArgumentException("All ingredients must have a foodId.");
            }
            try {
                foodInputPort.getFoodById(portion.getFoodId());
            } catch (NoSuchElementException e) {
                log.warn("Ingredient foodId {} not found. Recipe creation aborted.", portion.getFoodId());
                throw new FoodNotFoundException(portion.getFoodId());
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Calcula el NutritionSummary a partir de los ingredientes.
    // En este punto todos los foodId ya están validados.
    // ─────────────────────────────────────────────────────────
    private NutritionSummaryEmbedded calculateNutrition(Recipe recipe) {
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return emptyNutrition();
        }

        double totalCalories = 0;
        double totalProtein  = 0;
        double totalCarbs    = 0;
        double totalFat      = 0;

        for (FoodPortion portion : recipe.getIngredients()) {
            Food food = foodInputPort.getFoodById(portion.getFoodId());
            double grams = toGrams(portion);

            totalCalories += calculateMacro(food.getCaloriesPer100g(), grams);
            totalProtein  += calculateMacro(food.getProteinPer100g(),  grams);
            totalCarbs    += calculateMacro(food.getCarbsPer100g(),    grams);
            totalFat      += calculateMacro(food.getFatPer100g(),      grams);
        }

        int servings = recipe.getServings() != null && recipe.getServings() > 0
                ? recipe.getServings()
                : 1;

        NutritionSummaryEmbedded nutrition = new NutritionSummaryEmbedded();
        nutrition.setTotalCalories(round(totalCalories / servings));
        nutrition.setTotalProtein(round(totalProtein / servings));
        nutrition.setTotalCarbs(round(totalCarbs / servings));
        nutrition.setTotalFat(round(totalFat / servings));
        return nutrition;
    }

    private double toGrams(FoodPortion portion) {
        if (portion.getQuantity() == null || portion.getUnit() == null) return 0;
        double quantity = portion.getQuantity();
        return switch (portion.getUnit()) {
            case G    -> quantity;
            case KG   -> quantity * 1000;
            case ML   -> quantity;
            case L    -> quantity * 1000;
            case TBSP -> quantity * 15;
            case TSP  -> quantity * 5;
            case CUP  -> quantity * 240;
            case UNIT -> {
                double gramsPerUnit = (portion.getWeightPerUnit() != null && portion.getWeightPerUnit() > 0)
                        ? portion.getWeightPerUnit()
                        : 100.0;
                yield quantity * gramsPerUnit;
            }
        };
    }

    private double calculateMacro(Double macroPer100g, double grams) {
        if (macroPer100g == null) return 0;
        return (macroPer100g * grams) / 100.0;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private NutritionSummaryEmbedded emptyNutrition() {
        NutritionSummaryEmbedded n = new NutritionSummaryEmbedded();
        n.setTotalCalories(0.0);
        n.setTotalProtein(0.0);
        n.setTotalCarbs(0.0);
        n.setTotalFat(0.0);
        return n;
    }
}
