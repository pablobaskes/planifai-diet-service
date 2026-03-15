package com.planing.diet_service.Diet.infrastructure.output.jpa.repository;



import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DietDayJpaRepository extends JpaRepository<DietDayEntity, Long> {

    List<DietDayEntity> findByDietId(Long dietId);
}
