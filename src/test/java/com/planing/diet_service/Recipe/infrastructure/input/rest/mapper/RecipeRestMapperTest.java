package com.planing.diet_service.Recipe.infrastructure.input.rest.mapper;

import com.planing.diet.dto.FoodPortionDto;
import com.planing.diet.dto.MeasureUnit;
import com.planing.diet.dto.RecipeRequest;
import com.planing.diet.dto.RecipeResponse;
import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.FoodPortion.domain.model.Unit;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeRestMapperTest {

    private final RecipeRestMapper mapper = Mappers.getMapper(RecipeRestMapper.class);

    @Test
    void mapsMealTypeFromApiRequestToDomainRecipe() {
        RecipeRequest request = new RecipeRequest()
                .name("API lunch")
                .mealType(com.planing.diet.dto.MealType.LUNCH)
                .ingredients(List.of(new FoodPortionDto()
                        .foodId(10L)
                        .quantity(150.0)
                        .unit(MeasureUnit.G)))
                .servings(BigDecimal.ONE);

        Recipe recipe = mapper.toDomain(request);

        assertThat(recipe.getMealType()).isEqualTo(MealType.LUNCH);
        assertThat(recipe.getIngredients()).hasSize(1);
        assertThat(recipe.getIngredients().get(0).getUnit()).isEqualTo(Unit.G);
    }

    @Test
    void mapsMealTypeFromDomainRecipeToApiResponse() {
        Recipe recipe = Recipe.builder()
                .id(1L)
                .name("API breakfast")
                .mealType(MealType.BREAKFAST)
                .ingredients(List.of(FoodPortion.builder()
                        .foodId(10L)
                        .quantity(150.0)
                        .unit(Unit.G)
                        .build()))
                .servings(1)
                .build();

        RecipeResponse response = mapper.toResponse(recipe);

        assertThat(response.getMealType()).isEqualTo(com.planing.diet.dto.MealType.BREAKFAST);
    }
}
