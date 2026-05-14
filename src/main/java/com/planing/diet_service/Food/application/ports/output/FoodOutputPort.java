package com.planing.diet_service.Food.application.ports.output;

import com.planing.diet_service.Food.domain.model.Food;
import com.planing.diet_service.Food.domain.model.FoodCategory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FoodOutputPort {

    List<Food> findAll(FoodCategory category, String name);

    List<Food> findAllByIds(Collection<Long> ids);

    Optional<Food> findById(Long id);

    Food save(Food food);

    void deleteById(Long id);

    boolean existsById(Long id);
}
