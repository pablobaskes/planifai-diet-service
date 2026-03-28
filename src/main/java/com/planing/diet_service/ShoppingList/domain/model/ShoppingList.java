package com.planing.diet_service.ShoppingList.domain.model;

import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShoppingList {
    private Long id;
    private LocalDate weekStart;
    private ShoppingListStatus status;
    private List<ShoppingListItem> items;
}