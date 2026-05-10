package com.planing.diet_service.InventoryItem.application.ports.output;

import com.planing.diet_service.InventoryItem.domain.model.InventoryItem;

import java.util.List;
import java.util.Optional;

public interface InventoryItemOutputPort {

    List<InventoryItem> findAll();

    Optional<InventoryItem> findById(Long id);

    Optional<InventoryItem> findByFoodId(Long foodId);

    InventoryItem save(InventoryItem inventoryItem);

    void deleteById(Long id);

    boolean existsById(Long id);
}
