package com.planing.diet_service.Recipe.application.usecase;


import com.planing.diet_service.Food.application.ports.input.FoodInputPort;
import com.planing.diet_service.Food.domain.exception.FoodNotFoundException;
import com.planing.diet_service.Food.domain.model.Food;
import com.planing.diet_service.Food.domain.model.FoodCategory;
import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.FoodPortion.domain.model.Unit;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.application.ports.output.RecipeOutputPort;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeUseCaseTest {

    @Mock
    private RecipeOutputPort recipeOutputPort;

    @Mock
    private FoodInputPort foodInputPort;

    @InjectMocks
    private RecipeUseCase recipeUseCase;

    // ── Fixtures ──────────────────────────────────────────────

    private Food chicken;
    private Food rice;
    private Food oliveOil;

    @BeforeEach
    void setUp() {
        chicken = Food.builder()
                .id(1L).name("Pechuga de pollo").category(FoodCategory.MEAT)
                .caloriesPer100g(165.0).proteinPer100g(31.0)
                .carbsPer100g(0.0).fatPer100g(3.6)
                .build();

        rice = Food.builder()
                .id(2L).name("Arroz blanco").category(FoodCategory.GRAIN)
                .caloriesPer100g(365.0).proteinPer100g(7.0)
                .carbsPer100g(80.0).fatPer100g(0.7)
                .build();

        oliveOil = Food.builder()
                .id(3L).name("Aceite de oliva").category(FoodCategory.OIL)
                .caloriesPer100g(884.0).proteinPer100g(0.0)
                .carbsPer100g(0.0).fatPer100g(100.0)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────

    private Recipe buildRecipe(String name, int servings, FoodPortion... portions) {
        Recipe recipe = new Recipe();
        recipe.setName(name);
        recipe.setServings(servings);
        recipe.setIngredients(new ArrayList<>(List.of(portions)));
        recipe.setTags(List.of("test"));
        return recipe;
    }

    private FoodPortion portion(Long foodId, double quantity, Unit unit) {
        return FoodPortion.builder()
                .foodId(foodId).quantity(quantity).unit(unit)
                .build();
    }

    private FoodPortion portionWithWeight(Long foodId, double quantity, double weightPerUnit) {
        return FoodPortion.builder()
                .foodId(foodId).quantity(quantity).unit(Unit.UNIT)
                .weightPerUnit(weightPerUnit)
                .build();
    }

    // ══════════════════════════════════════════════════════════
    // getAllRecipes
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getAllRecipes")
    class GetAllRecipes {

        @Test
        @DisplayName("devuelve lista de recetas del repositorio")
        void returnsAllRecipes() {
            Recipe r1 = new Recipe(); r1.setId(1L); r1.setName("Pollo");
            Recipe r2 = new Recipe(); r2.setId(2L); r2.setName("Arroz");
            when(recipeOutputPort.findAll()).thenReturn(List.of(r1, r2));

            List<Recipe> result = recipeUseCase.getAllRecipes();

            assertThat(result).hasSize(2);
            verify(recipeOutputPort).findAll();
        }

        @Test
        @DisplayName("devuelve lista vacía si no hay recetas")
        void returnsEmptyList() {
            when(recipeOutputPort.findAll()).thenReturn(List.of());
            assertThat(recipeUseCase.getAllRecipes()).isEmpty();
        }
    }

    // ══════════════════════════════════════════════════════════
    // getRecipeById
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getRecipeById")
    class GetRecipeById {

        @Test
        @DisplayName("devuelve receta si existe")
        void returnsRecipeWhenFound() {
            Recipe recipe = new Recipe(); recipe.setId(1L); recipe.setName("Pollo");
            when(recipeOutputPort.findById(1L)).thenReturn(Optional.of(recipe));

            Recipe result = recipeUseCase.getRecipeById(1L);

            assertThat(result.getName()).isEqualTo("Pollo");
        }

        @Test
        @DisplayName("lanza NoSuchElementException si no existe")
        void throwsWhenNotFound() {
            when(recipeOutputPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> recipeUseCase.getRecipeById(99L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("99");
        }
    }

    // ══════════════════════════════════════════════════════════
    // createRecipe
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("createRecipe")
    class CreateRecipe {

        @Test
        @DisplayName("crea receta y calcula nutritionSummary correctamente (G)")
        void createsRecipeAndCalculatesNutrition() {
            // pechuga 200g → 165*2=330 kcal, 31*2=62 prot, 0 carbs, 3.6*2=7.2 fat
            // arroz 100g   → 365*1=365 kcal, 7*1=7 prot, 80*1=80 carbs, 0.7 fat
            // total: 695 kcal, 69 prot, 80 carbs, 7.9 fat (servings=1)
            Recipe recipe = buildRecipe("Pollo con arroz", 1,
                    portion(1L, 200, Unit.G),
                    portion(2L, 100, Unit.G));

            when(foodInputPort.getFoodById(1L)).thenReturn(chicken);
            when(foodInputPort.getFoodById(2L)).thenReturn(rice);
            when(recipeOutputPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Recipe result = recipeUseCase.createRecipe(recipe);

            assertThat(result.getNutritionSummary()).isNotNull();
            assertThat(result.getNutritionSummary().getTotalCalories()).isEqualTo(695.0);
            assertThat(result.getNutritionSummary().getTotalProtein()).isEqualTo(69.0);
            assertThat(result.getNutritionSummary().getTotalCarbs()).isEqualTo(80.0);
            assertThat(result.getNutritionSummary().getTotalFat()).isEqualTo(7.9);
        }

        @Test
        @DisplayName("preserva mealType para que la receta creada por API sea candidata de generacion")
        void preservesMealTypeForGeneratedMealSlotEligibility() {
            Recipe recipe = buildRecipe("Desayuno API", 1,
                    portion(1L, 100, Unit.G));
            recipe.setMealType(MealType.BREAKFAST);

            when(foodInputPort.getFoodById(1L)).thenReturn(chicken);
            when(recipeOutputPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Recipe result = recipeUseCase.createRecipe(recipe);

            assertThat(result.getMealType()).isEqualTo(MealType.BREAKFAST);
            verify(recipeOutputPort).save(argThat(saved -> saved.getMealType() == MealType.BREAKFAST));
        }

        @Test
        @DisplayName("divide nutrition por servings correctamente")
        void dividesByServings() {
            // pechuga 400g → 660 kcal, 4 servings → 165 kcal/ración
            Recipe recipe = buildRecipe("Pollo", 4,
                    portion(1L, 400, Unit.G));

            when(foodInputPort.getFoodById(1L)).thenReturn(chicken);
            when(recipeOutputPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Recipe result = recipeUseCase.createRecipe(recipe);

            assertThat(result.getNutritionSummary().getTotalCalories()).isEqualTo(165.0);
            assertThat(result.getNutritionSummary().getTotalProtein()).isEqualTo(31.0);
        }

        @Test
        @DisplayName("convierte TBSP a gramos correctamente (×15)")
        void convertsTbspToGrams() {
            // aceite 2 TBSP = 30g → 884*0.3=265.2 kcal
            Recipe recipe = buildRecipe("Con aceite", 1,
                    portion(3L, 2, Unit.TBSP));

            when(foodInputPort.getFoodById(3L)).thenReturn(oliveOil);
            when(recipeOutputPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Recipe result = recipeUseCase.createRecipe(recipe);

            assertThat(result.getNutritionSummary().getTotalCalories()).isEqualTo(265.2);
        }

        @Test
        @DisplayName("convierte TSP a gramos correctamente (×5)")
        void convertsTspToGrams() {
            // aceite 1 TSP = 5g → 884*0.05=44.2 kcal
            Recipe recipe = buildRecipe("Con aceite tsp", 1,
                    portion(3L, 1, Unit.TSP));

            when(foodInputPort.getFoodById(3L)).thenReturn(oliveOil);
            when(recipeOutputPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Recipe result = recipeUseCase.createRecipe(recipe);

            assertThat(result.getNutritionSummary().getTotalCalories()).isEqualTo(44.2);
        }

        @Test
        @DisplayName("usa weightPerUnit cuando unit es UNIT")
        void usesWeightPerUnitWhenUnitIsUnit() {
            // huevo: 2 UNIT × 60g/unit = 120g → 165*1.2=198 kcal
            Food egg = Food.builder().id(4L).name("Huevo").category(FoodCategory.DAIRY)
                    .caloriesPer100g(155.0).proteinPer100g(13.0)
                    .carbsPer100g(1.1).fatPer100g(11.0).build();

            Recipe recipe = buildRecipe("Huevos", 1,
                    portionWithWeight(4L, 2, 60.0));

            when(foodInputPort.getFoodById(4L)).thenReturn(egg);
            when(recipeOutputPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Recipe result = recipeUseCase.createRecipe(recipe);

            // 155 * 1.2 = 186 kcal
            assertThat(result.getNutritionSummary().getTotalCalories()).isEqualTo(186.0);
        }

        @Test
        @DisplayName("usa 100g como fallback cuando weightPerUnit es null y unit es UNIT")
        void usesFallback100gWhenWeightPerUnitIsNull() {
            // cebolla 1 UNIT, weightPerUnit null → fallback 100g → 40*1=40 kcal
            Food onion = Food.builder().id(5L).name("Cebolla").category(FoodCategory.VEGETABLE)
                    .caloriesPer100g(40.0).proteinPer100g(1.1)
                    .carbsPer100g(9.3).fatPer100g(0.1).build();

            Recipe recipe = buildRecipe("Con cebolla", 1,
                    portion(5L, 1, Unit.UNIT));

            when(foodInputPort.getFoodById(5L)).thenReturn(onion);
            when(recipeOutputPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Recipe result = recipeUseCase.createRecipe(recipe);

            assertThat(result.getNutritionSummary().getTotalCalories()).isEqualTo(40.0);
        }

        @Test
        @DisplayName("lanza FoodNotFoundException si un foodId no existe")
        void throwsFoodNotFoundExceptionWhenFoodMissing() {
            Recipe recipe = buildRecipe("Receta inválida", 1,
                    portion(99L, 100, Unit.G));

            when(foodInputPort.getFoodById(99L))
                    .thenThrow(new NoSuchElementException("Food not found with id: 99"));

            assertThatThrownBy(() -> recipeUseCase.createRecipe(recipe))
                    .isInstanceOf(FoodNotFoundException.class)
                    .hasMessageContaining("99");

            verify(recipeOutputPort, never()).save(any());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException si la lista de ingredientes está vacía")
        void throwsWhenIngredientsEmpty() {
            Recipe recipe = buildRecipe("Sin ingredientes", 1);
            recipe.setIngredients(new ArrayList<>());

            assertThatThrownBy(() -> recipeUseCase.createRecipe(recipe))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one ingredient");

            verify(recipeOutputPort, never()).save(any());
        }

        @Test
        @DisplayName("lanza IllegalArgumentException si un ingrediente tiene foodId null")
        void throwsWhenFoodIdIsNull() {
            FoodPortion portionWithNullId = FoodPortion.builder()
                    .foodId(null).quantity(100.0).unit(Unit.G).build();
            Recipe recipe = buildRecipe("Ingrediente sin id", 1, portionWithNullId);

            assertThatThrownBy(() -> recipeUseCase.createRecipe(recipe))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("foodId");

            verify(recipeOutputPort, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════
    // updateRecipe
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateRecipe")
    class UpdateRecipe {

        @Test
        @DisplayName("actualiza receta existente y recalcula nutrition")
        void updatesRecipeAndRecalculates() {
            Recipe recipe = buildRecipe("Pollo actualizado", 2,
                    portion(1L, 200, Unit.G));

            when(recipeOutputPort.existsById(1L)).thenReturn(true);
            when(foodInputPort.getFoodById(1L)).thenReturn(chicken);
            when(recipeOutputPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Recipe result = recipeUseCase.updateRecipe(1L, recipe);

            assertThat(result.getId()).isEqualTo(1L);
            // 165*2=330 kcal / 2 servings = 165 kcal/ración
            assertThat(result.getNutritionSummary().getTotalCalories()).isEqualTo(165.0);
        }

        @Test
        @DisplayName("lanza NoSuchElementException si la receta no existe")
        void throwsWhenRecipeNotFound() {
            when(recipeOutputPort.existsById(99L)).thenReturn(false);

            Recipe recipe = buildRecipe("Pollo", 1, portion(1L, 100, Unit.G));

            assertThatThrownBy(() -> recipeUseCase.updateRecipe(99L, recipe))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("99");

            verify(recipeOutputPort, never()).save(any());
        }

        @Test
        @DisplayName("lanza FoodNotFoundException al actualizar si un foodId no existe")
        void throwsFoodNotFoundOnUpdate() {
            when(recipeOutputPort.existsById(1L)).thenReturn(true);
            when(foodInputPort.getFoodById(99L))
                    .thenThrow(new NoSuchElementException("not found"));

            Recipe recipe = buildRecipe("Receta", 1, portion(99L, 100, Unit.G));

            assertThatThrownBy(() -> recipeUseCase.updateRecipe(1L, recipe))
                    .isInstanceOf(FoodNotFoundException.class);

            verify(recipeOutputPort, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════
    // deleteRecipe
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("deleteRecipe")
    class DeleteRecipe {

        @Test
        @DisplayName("elimina receta existente")
        void deletesExistingRecipe() {
            when(recipeOutputPort.existsById(1L)).thenReturn(true);

            recipeUseCase.deleteRecipe(1L);

            verify(recipeOutputPort).deleteById(1L);
        }

        @Test
        @DisplayName("lanza NoSuchElementException si la receta no existe")
        void throwsWhenNotFound() {
            when(recipeOutputPort.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> recipeUseCase.deleteRecipe(99L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("99");

            verify(recipeOutputPort, never()).deleteById(any());
        }
    }

    // ══════════════════════════════════════════════════════════
    // toGrams — conversiones de unidades
    // ══════════════════════════════════════════════════════════
    @Nested
    @DisplayName("conversiones de unidades (toGrams via createRecipe)")
    class UnitConversions {

        private void assertCalories(Unit unit, double quantity, double expectedGrams) {
            // aceite: 884 kcal/100g → expectedGrams * 8.84
            Recipe recipe = buildRecipe("Test " + unit, 1,
                    FoodPortion.builder().foodId(3L).quantity(quantity).unit(unit).build());

            when(foodInputPort.getFoodById(3L)).thenReturn(oliveOil);
            when(recipeOutputPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Recipe result = recipeUseCase.createRecipe(recipe);
            double expected = Math.round((884.0 * expectedGrams / 100.0) * 10.0) / 10.0;
            assertThat(result.getNutritionSummary().getTotalCalories()).isEqualTo(expected);
        }

        @Test @DisplayName("G: sin conversión")
        void gramsConversion()      { assertCalories(Unit.G,   100, 100);  }

        @Test @DisplayName("KG: ×1000")
        void kilogramsConversion()  { assertCalories(Unit.KG,  0.1, 100);  }

        @Test @DisplayName("ML: densidad ≈1")
        void millilitersConversion(){ assertCalories(Unit.ML,  100, 100);  }

        @Test @DisplayName("L: ×1000")
        void litersConversion()     { assertCalories(Unit.L,   0.1, 100);  }

        @Test @DisplayName("TBSP: ×15")
        void tablespoonConversion()  { assertCalories(Unit.TBSP, 2,  30);   }

        @Test @DisplayName("TSP: ×5")
        void teaspoonConversion()    { assertCalories(Unit.TSP,  2,  10);   }

        @Test @DisplayName("CUP: ×240")
        void cupConversion()         { assertCalories(Unit.CUP,  1, 240);   }
    }
}
