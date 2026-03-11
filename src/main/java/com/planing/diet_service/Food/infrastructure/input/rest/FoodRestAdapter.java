package com.planing.diet_service.Food.infrastructure.input.rest;

import com.planing.diet.api.FoodsApi;
import com.planing.diet.dto.FoodCategory;
import com.planing.diet.dto.FoodRequest;
import com.planing.diet.dto.FoodResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FoodRestAdapter implements FoodsApi {
    @Override
    public ResponseEntity<FoodResponse> createFood(FoodRequest foodRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteFood(Long foodId) {
        return null;
    }

    @Override
    public ResponseEntity<List<FoodResponse>> getAllFoods(FoodCategory category, String name) {
        return null;
    }

    @Override
    public ResponseEntity<FoodResponse> getFoodById(Long foodId) {
        return null;
    }

    @Override
    public ResponseEntity<FoodResponse> updateFood(Long foodId, FoodRequest foodRequest) {
        return null;
    }
}
