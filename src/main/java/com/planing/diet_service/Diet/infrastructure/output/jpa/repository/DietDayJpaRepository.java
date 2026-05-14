package com.planing.diet_service.Diet.infrastructure.output.jpa.repository;



import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DietDayJpaRepository extends JpaRepository<DietDayEntity, Long> {

    @Query("""
            SELECT DISTINCT dd FROM DietDayEntity dd
            LEFT JOIN FETCH dd.mealSlots ms
            LEFT JOIN FETCH ms.recipe
            WHERE dd.diet.id = :dietId
            ORDER BY dd.date ASC
            """)
    List<DietDayEntity> findByDietIdWithMealSlotsAndRecipe(@Param("dietId") Long dietId);

    @Query("""
            SELECT DISTINCT dd FROM DietDayEntity dd
            JOIN FETCH dd.diet d
            LEFT JOIN FETCH dd.mealSlots ms
            LEFT JOIN FETCH ms.recipe
            WHERE d.id IN :dietIds
            ORDER BY dd.date ASC
            """)
    List<DietDayEntity> findByDietIdInWithMealSlotsAndRecipe(@Param("dietIds") List<Long> dietIds);

    List<DietDayEntity> findByDietId(Long dietId);

}
