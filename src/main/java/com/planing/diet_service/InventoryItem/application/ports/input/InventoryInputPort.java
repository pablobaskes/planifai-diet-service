package com.planing.diet_service.InventoryItem.application.ports.input;

import com.planing.diet_service.InventoryItem.domain.model.InventoryItem;
import com.planing.diet_service.InventoryItem.domain.model.StorageLocation;

import java.util.List;

public interface InventoryInputPort {

    List<InventoryItem> getAllInventoryItems(StorageLocation location);

    InventoryItem getInventoryItemById(Long id);

    InventoryItem createInventoryItem(InventoryItem inventoryItem);

    InventoryItem updateInventoryItem(Long id, InventoryItem inventoryItem);

    void deleteInventoryItem(Long id);
}
