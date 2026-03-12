package com.planing.diet_service.Food.application.usecase;


import com.planing.diet_service.Food.application.ports.input.FoodInputPort;
import com.planing.diet_service.Food.application.ports.output.FoodOutputPort;
import com.planing.diet_service.Food.domain.model.Food;
import com.planing.diet_service.Food.domain.model.FoodCategory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
@Slf4j
public class FoodUseCase implements FoodInputPort {

    private final FoodOutputPort foodOutputPort;

    @Override
    public List<Food> getAllFoods(FoodCategory category, String name) {
        log.info("Getting all foods - category: {}, name: {}", category, name);
        return foodOutputPort.findAll(category, name);
    }

    @Override
    public Food getFoodById(Long id) {
        log.info("Getting food by id: {}", id);
        return foodOutputPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Food not found with id: " + id));
    }

    @Override
    public Food createFood(Food food) {
        log.info("Creating food: {}", food.getName());
        return foodOutputPort.save(food);
    }

    @Override
    public Food updateFood(Long id, Food food) {
        log.info("Updating food with id: {}", id);
        if (!foodOutputPort.existsById(id)) {
            throw new NoSuchElementException("Food not found with id: " + id);
        }
        food.setId(id);
        return foodOutputPort.save(food);
    }

    @Override
    public void deleteFood(Long id) {
        log.info("Deleting food with id: {}", id);
        if (!foodOutputPort.existsById(id)) {
            throw new NoSuchElementException("Food not found with id: " + id);
        }
        foodOutputPort.deleteById(id);
    }
}
