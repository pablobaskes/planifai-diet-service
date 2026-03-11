package com.planing.diet_service.Diet.infrastructure.input.rest;

import com.planing.diet.api.DietDaysApi;
import com.planing.diet.dto.DietDayRequest;
import com.planing.diet.dto.DietDayResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class DietRestController implements DietDaysApi {
    @Override
    public ResponseEntity<DietDayResponse> createDietDay(Long dietId, DietDayRequest dietDayRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteDietDay(Long dietId, Long dayId) {
        return null;
    }

    @Override
    public ResponseEntity<List<DietDayResponse>> getDaysByDiet(Long dietId) {
        return null;
    }

    @Override
    public ResponseEntity<DietDayResponse> getDietDayById(Long dietId, Long dayId) {
        return null;
    }

    @Override
    public ResponseEntity<DietDayResponse> updateDietDay(Long dietId, Long dayId, DietDayRequest dietDayRequest) {
        return null;
    }
}
