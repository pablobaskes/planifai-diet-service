package com.planing.diet_service.Diet.infrastructure.output.jpa.repository;


import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DietJpaRepository extends JpaRepository<DietEntity, Long> {
}

