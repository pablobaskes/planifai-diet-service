package com.planing.diet_service.Diet.infrastructure.output.jpa;


import com.planing.diet_service.Diet.application.ports.output.DietOutputPort;
import com.planing.diet_service.Diet.domain.model.Diet;

import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietEntity;
import com.planing.diet_service.Diet.infrastructure.output.jpa.mapper.DietJpaMapper;
import com.planing.diet_service.Diet.infrastructure.output.jpa.repository.DietJpaRepository;
import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietDayEntity;
import com.planing.diet_service.Diet.infrastructure.output.jpa.repository.DietDayJpaRepository;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.repository.MealSlotJpaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class DietJpaAdapter implements DietOutputPort {

    private final DietJpaRepository dietJpaRepository;
    private final DietDayJpaRepository dietDayJpaRepository;
    private final DietJpaMapper dietJpaMapper;

    // ── Diet ──────────────────────────────

    @Override
    public List<Diet> findAllDiets() {
        return dietJpaRepository.findAll()
                .stream()
                .map(dietJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Diet> findDietById(Long id) {
        return dietJpaRepository.findById(id)
                .map(dietJpaMapper::toDomain);
    }

    @Override
    public Diet saveDiet(Diet diet) {
        DietEntity entity = dietJpaMapper.toEntity(diet);
        DietEntity saved = dietJpaRepository.save(entity);
        return dietJpaMapper.toDomain(saved);
    }

    @Override
    public void deleteDietById(Long id) {
        dietJpaRepository.deleteById(id);
    }

    @Override
    public boolean dietExistsById(Long id) {
        return dietJpaRepository.existsById(id);
    }

    @Override
    public List<Diet> findDietsByDateRange(LocalDate from, LocalDate to) {
        List<DietEntity> dietEntities = dietJpaRepository.findDietsBetween(from, to);

        return dietEntities.stream()
                .map(dietEntity -> {
                    List<DietDayEntity> daysWithSlots = dietDayJpaRepository
                            .findByDietIdWithMealSlotsAndRecipe(dietEntity.getId());
                    dietEntity.setDays(daysWithSlots);
                    return dietJpaMapper.toDomain(dietEntity);
                })
                .toList();
    }

    // ── DietDay ───────────────────────────

    @Override
    public List<DietDay> findDaysByDietId(Long dietId) {
        return dietDayJpaRepository.findByDietId(dietId)
                .stream()
                .map(dietJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<DietDay> findDietDayById(Long dayId) {
        return dietDayJpaRepository.findById(dayId)
                .map(dietJpaMapper::toDomain);
    }

    @Override
    public DietDay saveDietDay(DietDay dietDay) {
        DietDayEntity entity = dietJpaMapper.toEntity(dietDay);
        // Recuperamos la entidad Diet para mantener la FK correctamente
        if (dietDay.getDiet() != null && dietDay.getDiet().getId() != null) {
            dietJpaRepository.findById(dietDay.getDiet().getId())
                    .ifPresent(entity::setDiet);
        }
        DietDayEntity saved = dietDayJpaRepository.save(entity);
        return dietJpaMapper.toDomain(saved);
    }

    @Override
    public void deleteDietDayById(Long dayId) {
        dietDayJpaRepository.deleteById(dayId);
    }

    @Override
    public boolean dietDayExistsById(Long dayId) {
        return dietDayJpaRepository.existsById(dayId);
    }


}

