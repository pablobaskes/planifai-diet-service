package com.planing.diet_service.Food.infrastructure.output.jpa;

import com.planing.diet_service.Food.application.ports.output.FoodOutputPort;
import com.planing.diet_service.Food.domain.model.Food;
import com.planing.diet_service.Food.domain.model.FoodCategory;
import com.planing.diet_service.Food.infrastructure.output.jpa.entity.FoodEntity;
import com.planing.diet_service.Food.infrastructure.output.jpa.mapper.FoodJpaMapper;
import com.planing.diet_service.Food.infrastructure.output.jpa.repository.FoodJpaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class FoodJpaAdapter implements FoodOutputPort {

    private final FoodJpaRepository foodJpaRepository;
    private final FoodJpaMapper foodJpaMapper;

    @Override
    public List<Food> findAll(FoodCategory category, String name) {
        // Convertimos el enum de dominio al enum de entidad JPA (mismo nombre)
        FoodCategory jpaCategory =
                category != null
                        ? FoodCategory.valueOf(category.name())
                        : null;

        List<FoodEntity> entities;

        if (jpaCategory != null && name != null && !name.isBlank()) {
            entities = foodJpaRepository.findByCategoryAndNameContainingIgnoreCase(jpaCategory, name);
        } else if (jpaCategory != null) {
            entities = foodJpaRepository.findByCategory(jpaCategory);
        } else if (name != null && !name.isBlank()) {
            entities = foodJpaRepository.findByNameContainingIgnoreCase(name);
        } else {
            entities = foodJpaRepository.findAll();
        }

        return entities.stream()
                .map(foodJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Food> findAllByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return foodJpaRepository.findAllById(ids)
                .stream()
                .map(foodJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Food> findById(Long id) {
        return foodJpaRepository.findById(id)
                .map(foodJpaMapper::toDomain);
    }

    @Override
    public Food save(Food food) {
        FoodEntity entity = foodJpaMapper.toEntity(food);
        FoodEntity saved = foodJpaRepository.save(entity);
        return foodJpaMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        foodJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return foodJpaRepository.existsById(id);
    }
}
