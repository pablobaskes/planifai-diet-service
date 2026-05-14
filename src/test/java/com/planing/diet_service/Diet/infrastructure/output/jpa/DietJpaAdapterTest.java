package com.planing.diet_service.Diet.infrastructure.output.jpa;

import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietDayEntity;
import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietEntity;
import com.planing.diet_service.Diet.infrastructure.output.jpa.mapper.DietJpaMapper;
import com.planing.diet_service.Diet.infrastructure.output.jpa.repository.DietDayJpaRepository;
import com.planing.diet_service.Diet.infrastructure.output.jpa.repository.DietJpaRepository;
import com.planing.diet_service.FoodPortion.domain.model.Unit;
import com.planing.diet_service.FoodPortion.infrastructure.output.jpa.entity.FoodPortionEmbedded;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.entity.MealSlotEntity;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.RecipeEntity;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.repository.RecipeJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DietJpaAdapterTest {

    @Mock
    private DietJpaRepository dietJpaRepository;

    @Mock
    private DietDayJpaRepository dietDayJpaRepository;

    @Mock
    private RecipeJpaRepository recipeJpaRepository;

    @Mock
    private DietJpaMapper dietJpaMapper;

    @Test
    void shoppingLookupDoesNotLoadDietGraphWhenActiveDietIsAmbiguous() {
        LocalDate from = LocalDate.of(2026, 5, 11);
        LocalDate to = from.plusDays(6);
        DietJpaAdapter adapter = adapter();

        when(dietJpaRepository.findDietIdsBetween(from, to)).thenReturn(List.of(1L, 2L));

        List<Diet> diets = adapter.findDietsByDateRangeForShoppingList(from, to);

        assertThat(diets).extracting(Diet::getId).containsExactly(1L, 2L);
        verify(dietDayJpaRepository, never()).findByDietIdWithMealSlotsAndRecipe(1L);
        verify(recipeJpaRepository, never()).findByIdInWithIngredients(List.of(10L));
    }

    @Test
    void shoppingLookupLoadsSingleDietSlotsRecipesAndIngredientsWithoutTags() {
        LocalDate from = LocalDate.of(2026, 5, 11);
        LocalDate to = from.plusDays(6);
        DietJpaAdapter adapter = adapter();
        DietEntity diet = dietEntity(1L, from, to);
        RecipeEntity recipe = recipeEntity(10L);
        DietDayEntity day = dietDayEntity(100L, from);
        MealSlotEntity slot = mealSlotEntity(1000L, recipe);
        day.setMealSlots(List.of(slot));

        when(dietJpaRepository.findDietIdsBetween(from, to)).thenReturn(List.of(1L));
        when(dietJpaRepository.findById(1L)).thenReturn(Optional.of(diet));
        when(dietDayJpaRepository.findByDietIdWithMealSlotsAndRecipe(1L)).thenReturn(List.of(day));

        List<Diet> diets = adapter.findDietsByDateRangeForShoppingList(from, to);

        assertThat(diets).hasSize(1);
        assertThat(diets.get(0).getDays()).hasSize(1);
        assertThat(diets.get(0).getDays().get(0).getMealSlots()).hasSize(1);
        assertThat(diets.get(0).getDays().get(0).getMealSlots().get(0).getRecipe().getIngredients())
                .hasSize(1);
        assertThat(diets.get(0).getDays().get(0).getMealSlots().get(0).getRecipe().getTags())
                .as("shopping generation does not need recipe tags")
                .isNull();
        verify(recipeJpaRepository).findByIdInWithIngredients(List.of(10L));
    }

    @Test
    void rangeLookupLoadsDietDaysForAllDietsInOneBatch() {
        LocalDate from = LocalDate.of(2026, 5, 11);
        LocalDate to = from.plusDays(6);
        DietJpaAdapter adapter = adapter();
        DietEntity firstDiet = dietEntity(1L, from, to);
        DietEntity secondDiet = dietEntity(2L, from.plusDays(1), to.plusDays(1));
        RecipeEntity firstRecipe = recipeEntity(10L);
        RecipeEntity secondRecipe = recipeEntity(11L);
        DietDayEntity firstDay = dietDayEntity(100L, from);
        DietDayEntity secondDay = dietDayEntity(200L, from.plusDays(1));
        firstDay.setDiet(firstDiet);
        secondDay.setDiet(secondDiet);
        firstDay.setMealSlots(List.of(mealSlotEntity(1000L, firstRecipe)));
        secondDay.setMealSlots(List.of(mealSlotEntity(2000L, secondRecipe)));
        Diet firstDomain = new Diet();
        firstDomain.setId(1L);
        Diet secondDomain = new Diet();
        secondDomain.setId(2L);

        when(dietJpaRepository.findDietsBetween(from, to)).thenReturn(List.of(firstDiet, secondDiet));
        when(dietDayJpaRepository.findByDietIdInWithMealSlotsAndRecipe(List.of(1L, 2L)))
                .thenReturn(List.of(firstDay, secondDay));
        when(dietJpaMapper.toDomain(firstDiet)).thenReturn(firstDomain);
        when(dietJpaMapper.toDomain(secondDiet)).thenReturn(secondDomain);

        List<Diet> diets = adapter.findDietsByDateRange(from, to);

        assertThat(diets).extracting(Diet::getId).containsExactly(1L, 2L);
        assertThat(firstDiet.getDays()).containsExactly(firstDay);
        assertThat(secondDiet.getDays()).containsExactly(secondDay);
        verify(dietDayJpaRepository).findByDietIdInWithMealSlotsAndRecipe(List.of(1L, 2L));
        verify(dietDayJpaRepository, never()).findByDietIdWithMealSlotsAndRecipe(1L);
        verify(dietDayJpaRepository, never()).findByDietIdWithMealSlotsAndRecipe(2L);
        verify(recipeJpaRepository).findByIdInWithIngredients(List.of(10L, 11L));
        verify(recipeJpaRepository).findByIdInWithTags(List.of(10L, 11L));
    }

    private DietJpaAdapter adapter() {
        return new DietJpaAdapter(dietJpaRepository, dietDayJpaRepository, recipeJpaRepository, dietJpaMapper);
    }

    private DietEntity dietEntity(Long id, LocalDate from, LocalDate to) {
        DietEntity diet = new DietEntity();
        diet.setId(id);
        diet.setName("Diet");
        diet.setInitDate(from);
        diet.setEndDate(to);
        diet.setCaloriesTarget(2000);
        return diet;
    }

    private DietDayEntity dietDayEntity(Long id, LocalDate date) {
        DietDayEntity day = new DietDayEntity();
        day.setId(id);
        day.setDate(date);
        return day;
    }

    private MealSlotEntity mealSlotEntity(Long id, RecipeEntity recipe) {
        MealSlotEntity slot = new MealSlotEntity();
        slot.setId(id);
        slot.setType(MealType.LUNCH);
        slot.setRecipe(recipe);
        return slot;
    }

    private RecipeEntity recipeEntity(Long id) {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setId(id);
        recipe.setName("Lunch");
        recipe.setMealType(MealType.LUNCH);
        recipe.setIngredients(List.of(ingredient()));
        recipe.setTags(List.of("unused"));
        return recipe;
    }

    private FoodPortionEmbedded ingredient() {
        FoodPortionEmbedded ingredient = new FoodPortionEmbedded();
        ingredient.setFoodId(20L);
        ingredient.setQuantity(100.0);
        ingredient.setUnit(Unit.G);
        return ingredient;
    }
}
