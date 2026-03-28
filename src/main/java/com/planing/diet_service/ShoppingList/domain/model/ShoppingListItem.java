package com.planing.diet_service.ShoppingList.domain.model;

import com.planing.diet_service.FoodPortion.domain.model.Unit;

public class ShoppingListItem {
    private Long id;
    private Long foodId;
    private String foodName;
    private Double requiredQuantity;
    private Double availableQuantity;
    private Double missingQuantity;
    private Unit unit;
    private boolean purchased;
}
