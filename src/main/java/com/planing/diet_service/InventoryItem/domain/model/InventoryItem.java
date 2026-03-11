package com.planing.diet_service.InventoryItem.domain.model;

import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;

public class InventoryItem {
    private Long id;
    private FoodPortion portion;
    private StorageLocation location;
}