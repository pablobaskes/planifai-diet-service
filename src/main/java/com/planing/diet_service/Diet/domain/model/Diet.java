package com.planing.diet_service.Diet.domain.model;

import com.planing.diet_service.DietDay.domain.model.DietDay;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Diet {
    private Long id;
    private String name;
    private String description;
    private Integer caloriesTarget;
    private LocalDate initDate;
    private LocalDate endDate;
    private List<DietDay> days = new ArrayList<>();
}