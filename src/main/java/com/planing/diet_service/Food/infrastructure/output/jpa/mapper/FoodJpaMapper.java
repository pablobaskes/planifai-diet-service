package com.planing.diet_service.Food.infrastructure.output.jpa.mapper;

import com.planing.diet_service.Food.domain.model.Food;
import com.planing.diet_service.Food.infrastructure.output.jpa.entity.FoodEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FoodJpaMapper {

    Food toDomain(FoodEntity entity);

    FoodEntity toEntity(Food food);
}
