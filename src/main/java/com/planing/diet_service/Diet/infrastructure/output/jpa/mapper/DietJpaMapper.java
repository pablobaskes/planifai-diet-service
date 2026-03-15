package com.planing.diet_service.Diet.infrastructure.output.jpa.mapper;


import com.planing.diet_service.Diet.domain.model.Diet;

import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietEntity;
import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietDayEntity;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.entity.MealSlotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DietJpaMapper {

    // ── Diet ──────────────────────────────
    Diet toDomain(DietEntity entity);
    DietEntity toEntity(Diet diet);

    // ── DietDay ───────────────────────────
    // Ignoramos diet en toDomain para evitar recursión infinita Diet→DietDay→Diet
    @Mapping(target = "diet.days", ignore = true)
    DietDay toDomain(DietDayEntity entity);

    @Mapping(target = "diet", ignore = true)
    DietDayEntity toEntity(DietDay dietDay);

    // ── MealSlot ──────────────────────────
    // Ignoramos dietDay en toDomain para evitar recursión infinita
    @Mapping(target = "dietDay.mealSlots", ignore = true)
    @Mapping(target = "dietDay.diet", ignore = true)
    MealSlot toDomain(MealSlotEntity entity);

    @Mapping(target = "dietDay", ignore = true)
    MealSlotEntity toEntity(MealSlot mealSlot);
}
