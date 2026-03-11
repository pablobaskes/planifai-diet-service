package com.planing.diet_service.DietDay.domain.model;

import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DietDay {
    private Long id;
    private LocalDate date;
    private Diet diet;
    private List<MealSlot> mealSlots = new ArrayList<>();
}
