package com.planing.diet_service.Diet.infrastructure.input.rest;


import com.planing.diet.api.DietsApi;
import com.planing.diet.dto.*;
import com.planing.diet_service.Diet.application.ports.input.DietInputPort;
import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Diet.infrastructure.input.rest.mapper.DietRestMapper;
import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class DietRestAdapter implements DietsApi {

    private final DietInputPort dietInputPort;
    private final DietRestMapper dietRestMapper;

    // ── Diet ──────────────────────────────

    @Override
    public ResponseEntity<List<DietResponse>> getAllDiets() {
        List<Diet> diets = dietInputPort.getAllDiets();
        return ResponseEntity.ok(dietRestMapper.toResponseList(diets));
    }

    @Override
    public ResponseEntity<DietResponse> getDietById(Long dietId) {
        Diet diet = dietInputPort.getDietById(dietId);
        return ResponseEntity.ok(dietRestMapper.toResponse(diet));
    }

    @Override
    public ResponseEntity<DietResponse> createDiet(DietRequest dietRequest) {
        Diet diet = dietInputPort.createDiet(dietRestMapper.toDomain(dietRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(dietRestMapper.toResponse(diet));
    }

    @Override
    public ResponseEntity<DietResponse> updateDiet(Long dietId, DietRequest dietRequest) {
        Diet diet = dietInputPort.updateDiet(dietId, dietRestMapper.toDomain(dietRequest));
        return ResponseEntity.ok(dietRestMapper.toResponse(diet));
    }

    @Override
    public ResponseEntity<Void> deleteDiet(Long dietId) {
        dietInputPort.deleteDiet(dietId);
        return ResponseEntity.noContent().build();
    }

    // ── DietDay ───────────────────────────

    @Override
    public ResponseEntity<List<DietDayResponse>> getDaysByDiet(Long dietId) {
        List<DietDay> days = dietInputPort.getDaysByDiet(dietId);
        return ResponseEntity.ok(dietRestMapper.toDayResponseList(days));
    }

    @Override
    public ResponseEntity<DietDayResponse> getDietDayById(Long dietId, Long dayId) {
        DietDay dietDay = dietInputPort.getDietDayById(dietId, dayId);
        return ResponseEntity.ok(dietRestMapper.toResponse(dietDay));
    }

    @Override
    public ResponseEntity<List<DietDetailResponse>> getDietsByDateRange(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<Diet> diets = dietInputPort.getDietsByDateRange(from, to);
        return ResponseEntity.ok(dietRestMapper.toDetailResponseList(diets));
    }

    @Override
    public ResponseEntity<DietDayResponse> createDietDay(Long dietId, DietDayRequest dietDayRequest) {
        DietDay dietDay = dietInputPort.createDietDay(dietId, dietRestMapper.toDomain(dietDayRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(dietRestMapper.toResponse(dietDay));
    }

    @Override
    public ResponseEntity<DietDayResponse> updateDietDay(Long dietId, Long dayId, DietDayRequest dietDayRequest) {
        DietDay dietDay = dietInputPort.updateDietDay(dietId, dayId, dietRestMapper.toDomain(dietDayRequest));
        return ResponseEntity.ok(dietRestMapper.toResponse(dietDay));
    }

    @Override
    public ResponseEntity<Void> deleteDietDay(Long dietId, Long dayId) {
        dietInputPort.deleteDietDay(dietId, dayId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<MealSlotDetailResponse> overrideMealSlotRecipe(
            Long slotId,
            MealSlotRecipeOverrideRequest mealSlotRecipeOverrideRequest) {

        if (mealSlotRecipeOverrideRequest == null) {
            throw new IllegalArgumentException("Meal slot recipe override request is required.");
        }
        MealSlot mealSlot = dietInputPort.overrideMealSlotRecipe(
                slotId,
                mealSlotRecipeOverrideRequest.getRecipeId());
        return ResponseEntity.ok(dietRestMapper.toDetailResponse(mealSlot));
    }
}

