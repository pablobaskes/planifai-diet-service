package com.planing.diet_service.InventoryItem.domain.model;

import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryItem {
    private Long id;
    private FoodPortion portion;
    private StorageLocation location;
}