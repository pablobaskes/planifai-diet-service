package com.planing.diet_service.ShoppingList.infrastructure.output.jpa.repository;

import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.entity.ShoppingListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ShoppingListJpaRepository extends JpaRepository<ShoppingListEntity, Long> {

    // Carga la lista con sus items en una sola query — evita N+1
    @Query("""
            SELECT sl FROM ShoppingListEntity sl
            LEFT JOIN FETCH sl.items
            WHERE sl.weekStart = :weekStart
            """)
    Optional<ShoppingListEntity> findByWeekStart(@Param("weekStart") LocalDate weekStart);

}
