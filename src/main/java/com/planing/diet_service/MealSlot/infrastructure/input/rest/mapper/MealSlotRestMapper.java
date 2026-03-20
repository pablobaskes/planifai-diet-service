package com.planing.diet_service.MealSlot.infrastructure.input.rest.mapper;


import com.planing.diet.dto.MealSlotDetailResponse;
import com.planing.diet.dto.MealSlotResponse;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.Recipe.infrastructure.input.rest.mapper.RecipeRestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RecipeRestMapper.class})
public interface MealSlotRestMapper {

    // MealSlot → MealSlotResponse (ids simples)
    @Mapping(target = "dietDayId", source = "dietDay.id")
    @Mapping(target = "recipeId", source = "recipe.id")
    MealSlotResponse toResponse(MealSlot mealSlot);

    List<MealSlotResponse> toResponseList(List<MealSlot> mealSlots);

    // MealSlot → MealSlotDetailResponse (recipe completa)
    @Mapping(target = "recipe", source = "recipe")
    MealSlotDetailResponse toDetailResponse(MealSlot mealSlot);

    List<MealSlotDetailResponse> toDetailResponseList(List<MealSlot> mealSlots);
}

