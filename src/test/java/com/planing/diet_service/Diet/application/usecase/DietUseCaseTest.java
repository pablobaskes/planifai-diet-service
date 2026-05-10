package com.planing.diet_service.Diet.application.usecase;

import com.planing.diet_service.Diet.application.ports.output.DietOutputPort;
import com.planing.diet_service.MealSlot.application.ports.output.MealSlotJpaOutputPort;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.application.ports.output.RecipeOutputPort;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private DietUseCase dietUseCase;

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

    private MealSlot mealSlot(Long id, MealType type, Recipe recipe) {
        MealSlot mealSlot = new MealSlot();
        mealSlot.setId(id);
        mealSlot.setType(type);
        mealSlot.setRecipe(recipe);
        return mealSlot;
    }

    private Recipe recipe(Long id, MealType mealType) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setName("Recipe " + id);
        recipe.setMealType(mealType);
        return recipe;
    }
}
