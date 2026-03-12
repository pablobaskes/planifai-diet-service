package com.planing.diet_service.Recipe.infrastructure.input.rest.mapper;

import com.planing.diet.dto.FoodPortionDto;
import com.planing.diet.dto.MeasureUnit;
import com.planing.diet.dto.NutritionSummaryDto;
import com.planing.diet.dto.RecipeRequest;
import com.planing.diet.dto.RecipeResponse;
import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.FoodPortion.domain.model.Unit;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.NutritionSummaryEmbedded;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecipeRestMapper {

    // RecipeRequest → Recipe
    @Mapping(target = "id", ignore = true)
    Recipe toDomain(RecipeRequest recipeRequest);

    // Recipe → RecipeResponse
    RecipeResponse toResponse(Recipe recipe);

    List<RecipeResponse> toResponseList(List<Recipe> recipes);

    // FoodPortionDto → FoodPortion
    @Mapping(target = "unit", expression = "java(mapUnit(dto.getUnit()))")
    FoodPortion toDomain(FoodPortionDto dto);

    // FoodPortion → FoodPortionDto
    @Mapping(target = "unit", expression = "java(mapMeasureUnit(portion.getUnit()))")
    FoodPortionDto toDto(FoodPortion portion);


    NutritionSummaryDto toDto(NutritionSummaryEmbedded nutritionSummary);

    // Unit (dominio) ↔ MeasureUnit (DTO)
    default Unit mapUnit(MeasureUnit measureUnit) {
        if (measureUnit == null) return null;
        return Unit.valueOf(measureUnit.name());
    }

    default MeasureUnit mapMeasureUnit(Unit unit) {
        if (unit == null) return null;
        return MeasureUnit.valueOf(unit.name());
    }
}
