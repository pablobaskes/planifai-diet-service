package com.planing.diet_service.Diet.infrastructure.input.rest.mapper;


import com.planing.diet.dto.DietDayRequest;
import com.planing.diet.dto.DietDayResponse;
import com.planing.diet.dto.DietRequest;
import com.planing.diet.dto.DietResponse;
import com.planing.diet.dto.MealSlotResponse;
import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DietRestMapper {

    // ── Diet ──────────────────────────────

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "days", ignore = true)
    Diet toDomain(DietRequest dietRequest);

    @Mapping(target = "totalDays", expression = "java(diet.getDays() != null ? diet.getDays().size() : 0)")
    DietResponse toResponse(Diet diet);

    List<DietResponse> toResponseList(List<Diet> diets);

    // ── DietDay ───────────────────────────

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "diet", ignore = true)
    @Mapping(target = "mealSlots", ignore = true)
    DietDay toDomain(DietDayRequest dietDayRequest);

    @Mapping(target = "dietId", source = "diet.id")
    DietDayResponse toResponse(DietDay dietDay);

    List<DietDayResponse> toDayResponseList(List<DietDay> days);


}

