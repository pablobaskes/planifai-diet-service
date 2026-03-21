package com.planing.diet_service.Diet.application.usecase;

import com.planing.diet_service.Diet.application.ports.input.DietInputPort;
import com.planing.diet_service.Diet.application.ports.output.DietOutputPort;
import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.MealSlot.application.ports.output.MealSlotJpaOutputPort;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.planing.diet_service.MealSlot.domain.utils.DietConstants.*;

@Service
@AllArgsConstructor
@Slf4j
public class DietUseCase implements DietInputPort {

    private final DietOutputPort dietOutputPort;
    private final MealSlotJpaOutputPort mealSlotJpaOutputPort;


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
        log.info("Creating diet: {} from {} to {}", diet.getName(), diet.getInitDate(), diet.getEndDate());
        diet.validate();
        Diet savedDiet = dietOutputPort.saveDiet(diet);
        savedDiet.setDays(generateDietDays(savedDiet));
        return savedDiet;
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

    @Override
    public List<Diet> getDietsByDateRange(LocalDate from, LocalDate to) {
        log.info("Getting diets between {} and {}", from, to);
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both 'from' and 'to' dates are required.");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("'to' date cannot be before 'from' date.");
        }
        return dietOutputPort.findDietsByDateRange(from, to);
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
        log.info("Getting diet day {} for diet {}", dayId, dietId);
        if (!dietOutputPort.dietExistsById(dietId)) {
            throw new NoSuchElementException("Diet not found with id: " + dietId);
        }
        return dietOutputPort.findDietDayById(dayId)
                .orElseThrow(() -> new NoSuchElementException("DietDay not found with id: " + dayId));
    }

    @Override
    public DietDay createDietDay(Long dietId, DietDay dietDay) {
        log.info("Creating diet day for diet id: {}", dietId);
        dietDay.setDiet(getDietById(dietId));
        return dietOutputPort.saveDietDay(dietDay);
    }

    @Override
    public DietDay updateDietDay(Long dietId, Long dayId, DietDay dietDay) {
        log.info("Updating diet day {} for diet {}", dayId, dietId);
        if (!dietOutputPort.dietExistsById(dietId)) {
            throw new NoSuchElementException("Diet not found with id: " + dietId);
        }
        if (!dietOutputPort.dietDayExistsById(dayId)) {
            throw new NoSuchElementException("DietDay not found with id: " + dayId);
        }
        dietDay.setId(dayId);
        dietDay.setDiet(getDietById(dietId));
        return dietOutputPort.saveDietDay(dietDay);
    }

    @Override
    public void deleteDietDay(Long dietId, Long dayId) {
        log.info("Deleting diet day {} for diet {}", dayId, dietId);
        if (!dietOutputPort.dietExistsById(dietId)) {
            throw new NoSuchElementException("Diet not found with id: " + dietId);
        }
        if (!dietOutputPort.dietDayExistsById(dayId)) {
            throw new NoSuchElementException("DietDay not found with id: " + dayId);
        }
        dietOutputPort.deleteDietDayById(dayId);
    }

    // ─────────────────────────────────────────────────────────
    // Generación de días y asignación de MealSlots
    // ─────────────────────────────────────────────────────────

    private List<DietDay> generateDietDays(Diet diet) {
        List<DietDay> days = new ArrayList<>();
        LocalDate current = diet.getInitDate();

        while (!current.isAfter(diet.getEndDate())) {
            days.add(buildAndPersistDietDay(current, diet));
            current = current.plusDays(1);
        }

        log.info("Generated {} diet days for diet '{}'", days.size(), diet.getName());
        return days;
    }

    private DietDay buildAndPersistDietDay(LocalDate date, Diet diet) {
        // 1. Persistir DietDay vacío → obtener ID de BD
        DietDay dietDay = new DietDay();
        dietDay.setDate(date);
        dietDay.setDiet(diet);
        dietDay.setMealSlots(new ArrayList<>());
        DietDay savedDietDay = dietOutputPort.saveDietDay(dietDay);

        // 2. Seleccionar receta por tipo y objetivo calórico → persistir MealSlot
        int caloriesTarget = diet.getCaloriesTarget() != null
                ? diet.getCaloriesTarget()
                : DEFAULT_CALORIES_TARGET;

        List<MealSlot> savedSlots = new ArrayList<>();

        MealSlot breakfast = MealSlot.selectBest(
                mealSlotJpaOutputPort.findRecipesByMealType(MealType.BREAKFAST),
                MealType.BREAKFAST, caloriesTarget * BREAKFAST_PCT, savedDietDay);
        if (breakfast != null) savedSlots.add(mealSlotJpaOutputPort.saveMealSlot(breakfast));

        MealSlot lunch = MealSlot.selectBest(
                mealSlotJpaOutputPort.findRecipesByMealType(MealType.LUNCH),
                MealType.LUNCH, caloriesTarget * LUNCH_PCT, savedDietDay);
        if (lunch != null) savedSlots.add(mealSlotJpaOutputPort.saveMealSlot(lunch));

        MealSlot dinner = MealSlot.selectBest(
                mealSlotJpaOutputPort.findRecipesByMealType(MealType.DINNER),
                MealType.DINNER, caloriesTarget * DINNER_PCT, savedDietDay);
        if (dinner != null) savedSlots.add(mealSlotJpaOutputPort.saveMealSlot(dinner));

        savedDietDay.setMealSlots(savedSlots);
        return savedDietDay;
    }
}
