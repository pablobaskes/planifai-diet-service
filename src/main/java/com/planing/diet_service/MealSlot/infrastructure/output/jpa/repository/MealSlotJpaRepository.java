package com.planing.diet_service.MealSlot.infrastructure.output.jpa.repository;


import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.entity.MealSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealSlotJpaRepository extends JpaRepository<MealSlotEntity, Long> {

    // Devuelve los MealSlots del catálogo (sin dietDay asignado) filtrados por tipo
    List<MealSlotEntity> findByTypeAndDietDayIsNull(MealType type);

    // Devuelve todos los MealSlots del catálogo (sin dietDay asignado)
    List<MealSlotEntity> findByDietDayIsNull();
}

