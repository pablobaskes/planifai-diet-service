package com.planing.diet_service.MealSlot.infrastructure.output.jpa;

import com.planing.diet_service.MealSlot.application.ports.output.MealSlotJpaOutputPort;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.mapper.MealSlotJpaMapper;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.repository.MealSlotJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class MealSlotJpaAdapter implements MealSlotJpaOutputPort {

    private final MealSlotJpaRepository mealSlotJpaRepository;
    private final MealSlotJpaMapper mealSlotJpaMapper;

    @Override
    public List<MealSlot> findMealSlotsByType(MealType type) {
        return mealSlotJpaRepository.findByTypeAndDietDayIsNull(type)
                .stream()
                .map(mealSlotJpaMapper::toDomain)
                .toList();
    }
}
