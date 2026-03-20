package com.planing.diet_service.Diet.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Diet {
    private Long id;
    private String name;
    private String description;
    private Integer caloriesTarget;
    private LocalDate initDate;
    private LocalDate endDate;
    private List<DietDay> days = new ArrayList<>();

    public void validate() {
        if (initDate == null || endDate == null) {
            throw new IllegalArgumentException("initDate and endDate are required.");
        }
        if (endDate.isBefore(initDate)) {
            throw new IllegalArgumentException("endDate cannot be before initDate.");
        }
        if (initDate.plusDays(365).isBefore(endDate)) {
            throw new IllegalArgumentException("Diet duration cannot exceed 365 days.");
        }
    }
}