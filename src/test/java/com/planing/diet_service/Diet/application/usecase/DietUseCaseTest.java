package com.planing.diet_service.Diet.application.usecase;

import com.planing.diet_service.Diet.application.ports.output.DietOutputPort;
import com.planing.diet_service.Diet.domain.exception.OverlappingDietException;
import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.MealSlot.application.ports.output.MealSlotJpaOutputPort;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.application.ports.output.RecipeOutputPort;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.NutritionSummaryEmbedded;
import com.planing.diet_service.ShoppingList.application.ports.output.ShoppingListOutputPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DietUseCaseTest {

    @Mock
    private DietOutputPort dietOutputPort;

    @Mock
    private MealSlotJpaOutputPort mealSlotJpaOutputPort;

    @Mock
    private RecipeOutputPort recipeOutputPort;

    @Mock
    private ShoppingListOutputPort shoppingListOutputPort;

    @InjectMocks
    private DietUseCase dietUseCase;

    @Test
    void createDietGeneratesOneDayPerDateWithBreakfastLunchAndDinnerSlots() {
        LocalDate initDate = LocalDate.of(2026, 5, 11);
        Diet requested = new Diet(null, "Wave 1 Diet", "Generated", 2000, initDate, initDate.plusDays(1), List.of());
        Diet saved = new Diet(1L, "Wave 1 Diet", "Generated", 2000, initDate, initDate.plusDays(1), List.of());

        when(dietOutputPort.findDietsByDateRange(initDate, initDate.plusDays(1))).thenReturn(List.of());
        when(dietOutputPort.saveDiet(requested)).thenReturn(saved);
        when(dietOutputPort.saveDietDay(any(DietDay.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mealSlotJpaOutputPort.saveMealSlot(any(MealSlot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mealSlotJpaOutputPort.findRecipesByMealType(MealType.BREAKFAST))
                .thenReturn(List.of(recipe(1L, MealType.BREAKFAST, 500.0)));
        when(mealSlotJpaOutputPort.findRecipesByMealType(MealType.LUNCH))
                .thenReturn(List.of(recipe(2L, MealType.LUNCH, 800.0)));
        when(mealSlotJpaOutputPort.findRecipesByMealType(MealType.DINNER))
                .thenReturn(List.of(recipe(3L, MealType.DINNER, 700.0)));

        Diet result = dietUseCase.createDiet(requested);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDays()).hasSize(2);
        assertThat(result.getDays()).extracting(DietDay::getDate)
                .containsExactly(initDate, initDate.plusDays(1));
        assertThat(result.getDays())
                .allSatisfy(day -> {
                    assertThat(day.getDiet()).isSameAs(saved);
                    assertThat(day.getMealSlots()).hasSize(3);
                    assertThat(day.getMealSlots()).extracting(MealSlot::getType)
                            .containsExactly(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER);
                    assertThat(day.getMealSlots()).allSatisfy(slot -> assertThat(slot.getDietDay()).isSameAs(day));
                });

        verify(dietOutputPort).saveDiet(requested);
        verify(dietOutputPort, org.mockito.Mockito.times(2)).saveDietDay(any(DietDay.class));
        verify(mealSlotJpaOutputPort, org.mockito.Mockito.times(6)).saveMealSlot(any(MealSlot.class));
    }

    @Test
    void createDietRejectsOverlappingExistingDiet() {
        LocalDate initDate = LocalDate.of(2026, 5, 11);
        Diet requested = new Diet(null, "New Diet", "Generated", 2000, initDate, initDate.plusDays(6), List.of());
        Diet existing = new Diet(1L, "Existing Diet", "Generated", 2000, initDate.minusDays(1), initDate.plusDays(2), List.of());

        when(dietOutputPort.findDietsByDateRange(initDate, initDate.plusDays(6))).thenReturn(List.of(existing));

        assertThatThrownBy(() -> dietUseCase.createDiet(requested))
                .isInstanceOf(OverlappingDietException.class)
                .hasMessageContaining("already exists overlapping");

        verify(dietOutputPort, never()).saveDiet(any());
        verify(mealSlotJpaOutputPort, never()).saveMealSlot(any());
    }

    @Test
    void createDietAllowsNonOverlappingDiet() {
        LocalDate initDate = LocalDate.of(2026, 5, 18);
        Diet requested = new Diet(null, "Wave 1 Diet", "Generated", 2000, initDate, initDate, List.of());
        Diet saved = new Diet(2L, "Wave 1 Diet", "Generated", 2000, initDate, initDate, List.of());

        when(dietOutputPort.findDietsByDateRange(initDate, initDate)).thenReturn(List.of());
        when(dietOutputPort.saveDiet(requested)).thenReturn(saved);
        when(dietOutputPort.saveDietDay(any(DietDay.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mealSlotJpaOutputPort.findRecipesByMealType(MealType.BREAKFAST)).thenReturn(List.of());
        when(mealSlotJpaOutputPort.findRecipesByMealType(MealType.LUNCH)).thenReturn(List.of());
        when(mealSlotJpaOutputPort.findRecipesByMealType(MealType.DINNER)).thenReturn(List.of());

        Diet result = dietUseCase.createDiet(requested);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getDays()).hasSize(1);
        verify(dietOutputPort).saveDiet(requested);
    }

    @Test
    void createDietRejectsMissingDateRange() {
        Diet requested = new Diet(null, "Invalid", null, 2000, null, LocalDate.of(2026, 5, 12), List.of());

        assertThatThrownBy(() -> dietUseCase.createDiet(requested))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("initDate and endDate are required");

        verify(dietOutputPort, never()).saveDiet(any());
        verify(mealSlotJpaOutputPort, never()).saveMealSlot(any());
    }

    @Test
    void createDietRejectsEndDateBeforeInitDate() {
        Diet requested = new Diet(null, "Invalid", null, 2000,
                LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 11), List.of());

        assertThatThrownBy(() -> dietUseCase.createDiet(requested))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endDate cannot be before initDate");

        verify(dietOutputPort, never()).saveDiet(any());
        verify(mealSlotJpaOutputPort, never()).saveMealSlot(any());
    }

    @Test
    void createDietRejectsDurationLongerThanOneYear() {
        LocalDate initDate = LocalDate.of(2026, 5, 11);
        Diet requested = new Diet(null, "Invalid", null, 2000, initDate, initDate.plusDays(366), List.of());

        assertThatThrownBy(() -> dietUseCase.createDiet(requested))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Diet duration cannot exceed 365 days");

        verify(dietOutputPort, never()).saveDiet(any());
        verify(mealSlotJpaOutputPort, never()).saveMealSlot(any());
    }

    @Test
    void deleteDietDeletesShoppingListsForDietRangeBeforeDeletingDietGraph() {
        LocalDate initDate = LocalDate.of(2026, 5, 11);
        Diet existing = new Diet(1L, "Wave 1 Diet", "Generated", 2000, initDate, initDate.plusDays(6), List.of());

        when(dietOutputPort.findDietById(1L)).thenReturn(Optional.of(existing));

        dietUseCase.deleteDiet(1L);

        verify(shoppingListOutputPort).deleteByWeekStartBetween(initDate, initDate.plusDays(6));
        verify(dietOutputPort).deleteDietById(1L);
    }

    @Test
    void deleteDietThrowsWhenDietDoesNotExist() {
        when(dietOutputPort.findDietById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dietUseCase.deleteDiet(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Diet not found");

        verify(shoppingListOutputPort, never()).deleteByWeekStartBetween(any(), any());
        verify(dietOutputPort, never()).deleteDietById(any());
    }

    @Test
    void overrideMealSlotRecipeUpdatesExistingSlot() {
        MealSlot slot = mealSlot(10L, MealType.LUNCH, recipe(1L, MealType.LUNCH));
        Recipe newRecipe = recipe(2L, MealType.LUNCH);

        when(mealSlotJpaOutputPort.findMealSlotById(10L)).thenReturn(Optional.of(slot));
        when(recipeOutputPort.findById(2L)).thenReturn(Optional.of(newRecipe));
        when(mealSlotJpaOutputPort.saveMealSlot(any(MealSlot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MealSlot result = dietUseCase.overrideMealSlotRecipe(10L, 2L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getRecipe()).isEqualTo(newRecipe);
        verify(mealSlotJpaOutputPort).saveMealSlot(slot);
    }

    @Test
    void overrideMealSlotRecipeThrowsWhenSlotDoesNotExist() {
        when(mealSlotJpaOutputPort.findMealSlotById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dietUseCase.overrideMealSlotRecipe(99L, 2L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("MealSlot not found");

        verify(recipeOutputPort, never()).findById(any());
        verify(mealSlotJpaOutputPort, never()).saveMealSlot(any());
    }

    @Test
    void overrideMealSlotRecipeThrowsWhenRecipeDoesNotExist() {
        MealSlot slot = mealSlot(10L, MealType.DINNER, recipe(1L, MealType.DINNER));

        when(mealSlotJpaOutputPort.findMealSlotById(10L)).thenReturn(Optional.of(slot));
        when(recipeOutputPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dietUseCase.overrideMealSlotRecipe(10L, 99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Recipe not found");

        verify(mealSlotJpaOutputPort, never()).saveMealSlot(any());
    }

    @Test
    void overrideMealSlotRecipeRejectsIncompatibleMealType() {
        MealSlot slot = mealSlot(10L, MealType.BREAKFAST, recipe(1L, MealType.BREAKFAST));
        Recipe dinnerRecipe = recipe(2L, MealType.DINNER);

        when(mealSlotJpaOutputPort.findMealSlotById(10L)).thenReturn(Optional.of(slot));
        when(recipeOutputPort.findById(2L)).thenReturn(Optional.of(dinnerRecipe));

        assertThatThrownBy(() -> dietUseCase.overrideMealSlotRecipe(10L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not compatible");

        verify(mealSlotJpaOutputPort, never()).saveMealSlot(any());
    }

    @Test
    void overrideMealSlotRecipeAllowsRecipeWithoutMealType() {
        MealSlot slot = mealSlot(10L, MealType.BREAKFAST, recipe(1L, MealType.BREAKFAST));
        Recipe untypedRecipe = recipe(2L, null);

        when(mealSlotJpaOutputPort.findMealSlotById(10L)).thenReturn(Optional.of(slot));
        when(recipeOutputPort.findById(2L)).thenReturn(Optional.of(untypedRecipe));
        when(mealSlotJpaOutputPort.saveMealSlot(any(MealSlot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MealSlot result = dietUseCase.overrideMealSlotRecipe(10L, 2L);

        assertThat(result.getRecipe()).isEqualTo(untypedRecipe);
    }

    @Test
    void overrideMealSlotRecipeThrowsWhenSlotIdIsInvalid() {
        assertThatThrownBy(() -> dietUseCase.overrideMealSlotRecipe(0L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Meal slot id must be positive");

        verify(mealSlotJpaOutputPort, never()).findMealSlotById(any());
        verify(recipeOutputPort, never()).findById(any());
        verify(mealSlotJpaOutputPort, never()).saveMealSlot(any());
    }

    @Test
    void overrideMealSlotRecipeThrowsWhenRecipeIdIsInvalid() {
        assertThatThrownBy(() -> dietUseCase.overrideMealSlotRecipe(10L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Recipe id must be positive");

        verify(mealSlotJpaOutputPort, never()).findMealSlotById(any());
        verify(recipeOutputPort, never()).findById(any());
        verify(mealSlotJpaOutputPort, never()).saveMealSlot(any());
    }

    @Test
    void dietsByDateRangeReflectsOverriddenRecipeWhenUsingPersistedSlotReference() {
        LocalDate date = LocalDate.of(2026, 5, 10);
        Recipe originalRecipe = recipe(1L, MealType.LUNCH);
        Recipe newRecipe = recipe(2L, MealType.LUNCH);
        MealSlot slot = mealSlot(10L, MealType.LUNCH, originalRecipe);
        Diet diet = dietWithSlot(date, slot);

        when(mealSlotJpaOutputPort.findMealSlotById(10L)).thenReturn(Optional.of(slot));
        when(recipeOutputPort.findById(2L)).thenReturn(Optional.of(newRecipe));
        when(mealSlotJpaOutputPort.saveMealSlot(any(MealSlot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(dietOutputPort.findDietsByDateRange(date, date)).thenReturn(List.of(diet));

        dietUseCase.overrideMealSlotRecipe(10L, 2L);
        List<Diet> result = dietUseCase.getDietsByDateRange(date, date);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDays().get(0).getMealSlots().get(0).getRecipe())
                .isEqualTo(newRecipe);
    }

    private MealSlot mealSlot(Long id, MealType type, Recipe recipe) {
        MealSlot mealSlot = new MealSlot();
        mealSlot.setId(id);
        mealSlot.setType(type);
        mealSlot.setRecipe(recipe);
        return mealSlot;
    }

    private Diet dietWithSlot(LocalDate date, MealSlot slot) {
        Diet diet = new Diet();
        diet.setId(1L);
        diet.setName("Wave 1 Diet");
        diet.setInitDate(date);
        diet.setEndDate(date);

        DietDay day = new DietDay();
        day.setId(100L);
        day.setDate(date);
        day.setDiet(diet);
        day.setMealSlots(List.of(slot));

        slot.setDietDay(day);
        diet.setDays(List.of(day));
        return diet;
    }

    private Recipe recipe(Long id, MealType mealType) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setName("Recipe " + id);
        recipe.setMealType(mealType);
        return recipe;
    }

    private Recipe recipe(Long id, MealType mealType, Double calories) {
        Recipe recipe = recipe(id, mealType);
        NutritionSummaryEmbedded nutrition = new NutritionSummaryEmbedded();
        nutrition.setTotalCalories(calories);
        recipe.setNutritionSummary(nutrition);
        return recipe;
    }
}
