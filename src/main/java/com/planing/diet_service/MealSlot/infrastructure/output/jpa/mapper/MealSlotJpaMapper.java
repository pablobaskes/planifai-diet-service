package com.planing.diet_service.MealSlot.infrastructure.output.jpa.mapper;

import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.entity.MealSlotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MealSlotJpaMapper {


    @Mapping(target = "dietDay.mealSlots", ignore = true)
    @Mapping(target = "dietDay.diet", ignore = true)
    MealSlot toDomain(MealSlotEntity entity);


    @Mapping(target = "dietDay", ignore = true)
    MealSlotEntity toEntity(MealSlot mealSlot);
}
