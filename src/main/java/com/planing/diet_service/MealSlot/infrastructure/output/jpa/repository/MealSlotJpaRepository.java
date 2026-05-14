package com.planing.diet_service.MealSlot.infrastructure.output.jpa.repository;

import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.entity.MealSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealSlotJpaRepository extends JpaRepository<MealSlotEntity, Long> {

    // Devuelve los MealSlots del catalogo (sin dietDay asignado) filtrados por tipo
    List<MealSlotEntity> findByTypeAndDietDayIsNull(MealType type);

    // Devuelve todos los MealSlots del catalogo (sin dietDay asignado)
    List<MealSlotEntity> findByDietDayIsNull();

    @Query("""
            SELECT ms FROM MealSlotEntity ms
            LEFT JOIN FETCH ms.recipe
            LEFT JOIN FETCH ms.dietDay
            WHERE ms.id = :id
            """)
    Optional<MealSlotEntity> findByIdWithRecipeAndDietDay(@Param("id") Long id);
}
