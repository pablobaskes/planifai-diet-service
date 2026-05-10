package com.planing.diet_service.Diet.infrastructure.input.rest.mapper;

import com.planing.diet.dto.DietDayDetailResponse;
import com.planing.diet.dto.DietDayRequest;
import com.planing.diet.dto.DietDayResponse;
import com.planing.diet.dto.DietDetailResponse;
import com.planing.diet.dto.DietRequest;
import com.planing.diet.dto.DietResponse;
import com.planing.diet.dto.MealSlotDetailResponse;
import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.infrastructure.input.rest.mapper.MealSlotRestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MealSlotRestMapper.class})
public interface DietRestMapper {

    // ── DietRequest → Diet ────────────────────────────────────
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "days", ignore = true)
    Diet toDomain(DietRequest dietRequest);

    // ── Diet → DietResponse (lista simple, sin días anidados) ─
    @Mapping(target = "totalDays", expression = "java(diet.getDays() != null ? diet.getDays().size() : 0)")
    DietResponse toResponse(Diet diet);

    List<DietResponse> toResponseList(List<Diet> diets);

    // ── Diet → DietDetailResponse (días + mealSlots + recipe) ─
    DietDetailResponse toDetailResponse(Diet diet);

    List<DietDetailResponse> toDetailResponseList(List<Diet> diets);

    // ── DietDayRequest → DietDay ──────────────────────────────
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "diet", ignore = true)
    @Mapping(target = "mealSlots", ignore = true)
    DietDay toDomain(DietDayRequest dietDayRequest);

    // ── DietDay → DietDayResponse (con dietId) ────────────────
    @Mapping(target = "dietId", source = "diet.id")
    DietDayResponse toResponse(DietDay dietDay);

    List<DietDayResponse> toDayResponseList(List<DietDay> days);

    // ── DietDay → DietDayDetailResponse ──────────────────────
    // mealSlots → delegado a MealSlotRestMapper vía uses
    DietDayDetailResponse toDetailResponse(DietDay dietDay);

    MealSlotDetailResponse toDetailResponse(MealSlot mealSlot);
}
