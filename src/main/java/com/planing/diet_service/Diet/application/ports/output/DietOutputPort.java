package com.planing.diet_service.Diet.application.ports.output;


import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.domain.utils.MealType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DietOutputPort {

    // ── Diet ──────────────────────────────
    List<Diet> findAllDiets();
    Optional<Diet> findDietById(Long id);
    Diet saveDiet(Diet diet);
    void deleteDietById(Long id);
    boolean dietExistsById(Long id);
    List<Diet> findDietsByDateRange(LocalDate from, LocalDate to);
    List<Diet> findDietsByDateRangeForShoppingList(LocalDate from, LocalDate to);
    Optional<Diet> findDietByIdForShoppingList(Long dietId);

    // ── DietDay ───────────────────────────
    List<DietDay> findDaysByDietId(Long dietId);
    Optional<DietDay> findDietDayById(Long dayId);
    DietDay saveDietDay(DietDay dietDay);
    void deleteDietDayById(Long dayId);
    boolean dietDayExistsById(Long dayId);

}

