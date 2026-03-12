package com.planing.diet_service.Diet.infrastructure.input.rest;

import com.planing.diet.api.DietsApi;
import com.planing.diet.dto.DietDayRequest;
import com.planing.diet.dto.DietDayResponse;
import com.planing.diet.dto.DietRequest;
import com.planing.diet.dto.DietResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class DietRestController implements DietsApi {

    @Override
    public ResponseEntity<DietResponse> createDiet(DietRequest dietRequest) {
        return null;
    }

    @Override
    public ResponseEntity<DietDayResponse> createDietDay(Long dietId, DietDayRequest dietDayRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteDiet(Long dietId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteDietDay(Long dietId, Long dayId) {
        return null;
    }

    @Override
    public ResponseEntity<List<DietResponse>> getAllDiets() {
        return null;
    }

    @Override
    public ResponseEntity<List<DietDayResponse>> getDaysByDiet(Long dietId) {
        return null;
    }

    @Override
    public ResponseEntity<DietResponse> getDietById(Long dietId) {
        return null;
    }

    @Override
    public ResponseEntity<DietDayResponse> getDietDayById(Long dietId, Long dayId) {
        return null;
    }

    @Override
    public ResponseEntity<DietResponse> updateDiet(Long dietId, DietRequest dietRequest) {
        return null;
    }

    @Override
    public ResponseEntity<DietDayResponse> updateDietDay(Long dietId, Long dayId, DietDayRequest dietDayRequest) {
        return null;
    }
}
