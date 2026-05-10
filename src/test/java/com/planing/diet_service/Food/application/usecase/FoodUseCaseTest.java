package com.planing.diet_service.Food.application.usecase;

import com.planing.diet_service.Food.application.ports.output.FoodOutputPort;
import com.planing.diet_service.Food.domain.model.Food;
import com.planing.diet_service.Food.domain.model.FoodCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodUseCaseTest {

    @Mock
    private FoodOutputPort foodOutputPort;

    @InjectMocks
    private FoodUseCase foodUseCase;

    @Test
    void getAllFoodsPassesCategoryAndNameFiltersToOutputPort() {
        Food rice = food(1L, "Brown rice", FoodCategory.GRAIN);
        when(foodOutputPort.findAll(FoodCategory.GRAIN, "rice")).thenReturn(List.of(rice));

        List<Food> result = foodUseCase.getAllFoods(FoodCategory.GRAIN, "rice");

        assertThat(result).containsExactly(rice);
        verify(foodOutputPort).findAll(FoodCategory.GRAIN, "rice");
    }

    @Test
    void getFoodByIdReturnsFoodWhenFound() {
        Food chicken = food(1L, "Chicken", FoodCategory.MEAT);
        when(foodOutputPort.findById(1L)).thenReturn(Optional.of(chicken));

        Food result = foodUseCase.getFoodById(1L);

        assertThat(result).isSameAs(chicken);
    }

    @Test
    void getFoodByIdThrowsWhenFoodDoesNotExist() {
        when(foodOutputPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> foodUseCase.getFoodById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Food not found with id: 99");
    }

    @Test
    void createFoodSavesFood() {
        Food request = food(null, "Spinach", FoodCategory.VEGETABLE);
        Food created = food(1L, "Spinach", FoodCategory.VEGETABLE);
        when(foodOutputPort.save(request)).thenReturn(created);

        Food result = foodUseCase.createFood(request);

        assertThat(result).isSameAs(created);
        verify(foodOutputPort).save(request);
    }

    @Test
    void updateFoodSetsIdAndSavesExistingFood() {
        Food request = food(null, "Updated rice", FoodCategory.GRAIN);
        when(foodOutputPort.existsById(1L)).thenReturn(true);
        when(foodOutputPort.save(request)).thenReturn(request);

        Food result = foodUseCase.updateFood(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(foodOutputPort).save(request);
    }

    @Test
    void updateFoodThrowsWhenFoodDoesNotExist() {
        Food request = food(null, "Missing", FoodCategory.OTHER);
        when(foodOutputPort.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> foodUseCase.updateFood(99L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Food not found with id: 99");

        verify(foodOutputPort, never()).save(request);
    }

    @Test
    void deleteFoodDeletesExistingFood() {
        when(foodOutputPort.existsById(1L)).thenReturn(true);

        foodUseCase.deleteFood(1L);

        verify(foodOutputPort).deleteById(1L);
    }

    @Test
    void deleteFoodThrowsWhenFoodDoesNotExist() {
        when(foodOutputPort.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> foodUseCase.deleteFood(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Food not found with id: 99");

        verify(foodOutputPort, never()).deleteById(99L);
    }

    private Food food(Long id, String name, FoodCategory category) {
        return Food.builder()
                .id(id)
                .name(name)
                .category(category)
                .caloriesPer100g(100.0)
                .proteinPer100g(10.0)
                .carbsPer100g(12.0)
                .fatPer100g(2.0)
                .build();
    }
}
