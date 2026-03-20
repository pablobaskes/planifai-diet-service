package com.planing.diet_service.MealSlot.application.ports.output;

import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.domain.utils.MealType;

import java.util.List;

public interface MealSlotJpaOutputPort {

    List<MealSlot> findMealSlotsByType(MealType type);

}
