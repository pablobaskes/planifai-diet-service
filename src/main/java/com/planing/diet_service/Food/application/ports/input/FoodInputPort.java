package com.planing.diet_service.Food.application.ports.input;

import com.planing.diet_service.Food.domain.model.Food;
import com.planing.diet_service.Food.domain.model.FoodCategory;

import java.util.List;

public interface FoodInputPort {

    List<Food> getAllFoods(FoodCategory category, String name);

    Food getFoodById(Long id);

    Food createFood(Food food);

    Food updateFood(Long id, Food food);

    void deleteFood(Long id);
}

