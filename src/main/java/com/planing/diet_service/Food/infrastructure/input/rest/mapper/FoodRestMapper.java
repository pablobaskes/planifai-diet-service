package com.planing.diet_service.Food.infrastructure.input.rest.mapper;

import com.planing.diet.dto.FoodRequest;
import com.planing.diet.dto.FoodResponse;
import com.planing.diet_service.Food.domain.model.Food;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FoodRestMapper {

    Food toDomain(FoodRequest foodRequest);

    FoodResponse toResponse(Food food);

    List<FoodResponse> toResponseList(List<Food> foods);
}
