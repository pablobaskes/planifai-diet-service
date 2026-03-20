package com.planing.diet_service.Diet.application.ports.input;

import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Diet.domain.model.DietDay;


import java.time.LocalDate;
import java.util.List;

public interface DietInputPort {

    // ── Diet ──────────────────────────────
    List<Diet> getAllDiets();
    Diet getDietById(Long id);
    Diet createDiet(Diet diet);
    Diet updateDiet(Long id, Diet diet);
    void deleteDiet(Long id);
    List<Diet> getDietsByDateRange(LocalDate from, LocalDate to);

    // ── DietDay ───────────────────────────
    List<DietDay> getDaysByDiet(Long dietId);
    DietDay getDietDayById(Long dietId, Long dayId);
    DietDay createDietDay(Long dietId, DietDay dietDay);
    DietDay updateDietDay(Long dietId, Long dayId, DietDay dietDay);
    void deleteDietDay(Long dietId, Long dayId);
}
