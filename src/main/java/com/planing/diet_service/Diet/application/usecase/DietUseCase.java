package com.planing.diet_service.Diet.application.usecase;


import com.planing.diet_service.Diet.application.ports.input.DietInputPort;
import com.planing.diet_service.Diet.application.ports.output.DietOutputPort;
import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Diet.domain.model.DietDay;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
@Slf4j
public class DietUseCase implements DietInputPort {

    private final DietOutputPort dietOutputPort;

    // ── Diet ──────────────────────────────

    @Override
    public List<Diet> getAllDiets() {
        log.info("Getting all diets");
        return dietOutputPort.findAllDiets();
    }

    @Override
    public Diet getDietById(Long id) {
        log.info("Getting diet by id: {}", id);
        return dietOutputPort.findDietById(id)
                .orElseThrow(() -> new NoSuchElementException("Diet not found with id: " + id));
    }

    @Override
    public Diet createDiet(Diet diet) {
        log.info("Creating diet: {}", diet.getName());
        return dietOutputPort.saveDiet(diet);
    }

    @Override
    public Diet updateDiet(Long id, Diet diet) {
        log.info("Updating diet with id: {}", id);
        if (!dietOutputPort.dietExistsById(id)) {
            throw new NoSuchElementException("Diet not found with id: " + id);
        }
        diet.setId(id);
        return dietOutputPort.saveDiet(diet);
    }

    @Override
    public void deleteDiet(Long id) {
        log.info("Deleting diet with id: {}", id);
        if (!dietOutputPort.dietExistsById(id)) {
            throw new NoSuchElementException("Diet not found with id: " + id);
        }
        dietOutputPort.deleteDietById(id);
    }

    // ── DietDay ───────────────────────────

    @Override
    public List<DietDay> getDaysByDiet(Long dietId) {
        log.info("Getting days for diet id: {}", dietId);
        if (!dietOutputPort.dietExistsById(dietId)) {
            throw new NoSuchElementException("Diet not found with id: " + dietId);
        }
        return dietOutputPort.findDaysByDietId(dietId);
    }

    @Override
    public DietDay getDietDayById(Long dietId, Long dayId) {
        log.info("Getting diet day by id: {} for diet: {}", dayId, dietId);
        if (!dietOutputPort.dietExistsById(dietId)) {
            throw new NoSuchElementException("Diet not found with id: " + dietId);
        }
        return dietOutputPort.findDietDayById(dayId)
                .orElseThrow(() -> new NoSuchElementException("DietDay not found with id: " + dayId));
    }

    @Override
    public DietDay createDietDay(Long dietId, DietDay dietDay) {
        log.info("Creating diet day for diet id: {}", dietId);
        Diet diet = getDietById(dietId);
        dietDay.setDiet(diet);
        return dietOutputPort.saveDietDay(dietDay);
    }

    @Override
    public DietDay updateDietDay(Long dietId, Long dayId, DietDay dietDay) {
        log.info("Updating diet day id: {} for diet: {}", dayId, dietId);
        if (!dietOutputPort.dietExistsById(dietId)) {
            throw new NoSuchElementException("Diet not found with id: " + dietId);
        }
        if (!dietOutputPort.dietDayExistsById(dayId)) {
            throw new NoSuchElementException("DietDay not found with id: " + dayId);
        }
        dietDay.setId(dayId);
        Diet diet = getDietById(dietId);
        dietDay.setDiet(diet);
        return dietOutputPort.saveDietDay(dietDay);
    }

    @Override
    public void deleteDietDay(Long dietId, Long dayId) {
        log.info("Deleting diet day id: {} for diet: {}", dayId, dietId);
        if (!dietOutputPort.dietExistsById(dietId)) {
            throw new NoSuchElementException("Diet not found with id: " + dietId);
        }
        if (!dietOutputPort.dietDayExistsById(dayId)) {
            throw new NoSuchElementException("DietDay not found with id: " + dayId);
        }
        dietOutputPort.deleteDietDayById(dayId);
    }
}
