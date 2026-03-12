package com.planing.diet_service.Food.infrastructure.input.rest;

import com.planing.diet.api.FoodsApi;
import com.planing.diet.dto.FoodCategory;
import com.planing.diet.dto.FoodRequest;
import com.planing.diet.dto.FoodResponse;
import com.planing.diet_service.Food.application.ports.input.FoodInputPort;
import com.planing.diet_service.Food.domain.model.Food;
import com.planing.diet_service.Food.infrastructure.input.rest.mapper.FoodRestMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class FoodRestAdapter implements FoodsApi {

    private final FoodInputPort foodInputPort;
    private final FoodRestMapper foodRestMapper;

    @Override
    public ResponseEntity<FoodResponse> createFood(FoodRequest foodRequest) {
        Food food = foodInputPort.createFood(foodRestMapper.toDomain(foodRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(foodRestMapper.toResponse(food));
    }

    @Override
    public ResponseEntity<Void> deleteFood(Long foodId) {
        foodInputPort.deleteFood(foodId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<FoodResponse>> getAllFoods(FoodCategory category, String name) {
        List<Food> foods = foodInputPort.getAllFoods(
                category != null ? com.planing.diet_service.Food.domain.model.FoodCategory.valueOf(category.name()) : null,
                name
        );
        return ResponseEntity.ok(foodRestMapper.toResponseList(foods));
    }

    @Override
    public ResponseEntity<FoodResponse> getFoodById(Long foodId) {
        Food food = foodInputPort.getFoodById(foodId);
        return ResponseEntity.ok(foodRestMapper.toResponse(food));
    }

    @Override
    public ResponseEntity<FoodResponse> updateFood(Long foodId, FoodRequest foodRequest) {
        Food food = foodInputPort.updateFood(foodId, foodRestMapper.toDomain(foodRequest));
        return ResponseEntity.ok(foodRestMapper.toResponse(food));
    }
}
