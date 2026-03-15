package com.planing.diet_service.Diet.domain.model;

import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DietDay {
    private Long id;
    private LocalDate date;
    private Diet diet;
    private List<MealSlot> mealSlots = new ArrayList<>();
}
