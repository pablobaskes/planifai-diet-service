package com.planing.diet_service.Diet.infrastructure.output.jpa.repository;


import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DietJpaRepository extends JpaRepository<DietEntity, Long> {

    @Query("""
            SELECT DISTINCT d FROM DietEntity d
            WHERE d.initDate <= :to
              AND d.endDate >= :from
            ORDER BY d.initDate ASC
            """)
    List<DietEntity> findDietsBetween(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}

