package com.planing.diet_service.InventoryItem.application.usecase;

import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.InventoryItem.application.ports.input.InventoryInputPort;
import com.planing.diet_service.InventoryItem.application.ports.output.InventoryItemOutputPort;
import com.planing.diet_service.InventoryItem.domain.model.InventoryItem;
import com.planing.diet_service.InventoryItem.domain.model.StorageLocation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
@Slf4j
public class InventoryUseCase implements InventoryInputPort {

    private final InventoryItemOutputPort inventoryItemOutputPort;

    @Override
    public List<InventoryItem> getAllInventoryItems(StorageLocation location) {
        log.info("Getting inventory items - location: {}", location);
        List<InventoryItem> inventoryItems = inventoryItemOutputPort.findAll();

        if (location == null) {
            return inventoryItems;
        }

        return inventoryItems.stream()
                .filter(item -> location.equals(item.getLocation()))
                .toList();
    }

    @Override
    public InventoryItem getInventoryItemById(Long id) {
        log.info("Getting inventory item by id: {}", id);
        validateId(id);
        return inventoryItemOutputPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Inventory item not found with id: " + id));
    }

    @Override
    public InventoryItem createInventoryItem(InventoryItem inventoryItem) {
        validateInventoryItem(inventoryItem);
        inventoryItem.setId(null);
        log.info("Creating inventory item for food id: {}", inventoryItem.getPortion().getFoodId());
        return inventoryItemOutputPort.save(inventoryItem);
    }

    @Override
    public InventoryItem updateInventoryItem(Long id, InventoryItem inventoryItem) {
        log.info("Updating inventory item with id: {}", id);
        validateId(id);
        if (!inventoryItemOutputPort.existsById(id)) {
            throw new NoSuchElementException("Inventory item not found with id: " + id);
        }

        validateInventoryItem(inventoryItem);
        inventoryItem.setId(id);
        return inventoryItemOutputPort.save(inventoryItem);
    }

    @Override
    public void deleteInventoryItem(Long id) {
        log.info("Deleting inventory item with id: {}", id);
        validateId(id);
        if (!inventoryItemOutputPort.existsById(id)) {
            throw new NoSuchElementException("Inventory item not found with id: " + id);
        }

        inventoryItemOutputPort.deleteById(id);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Inventory item id must be positive.");
        }
    }

    private void validateInventoryItem(InventoryItem inventoryItem) {
        if (inventoryItem == null) {
            throw new IllegalArgumentException("Inventory item is required.");
        }
        if (inventoryItem.getLocation() == null) {
            throw new IllegalArgumentException("Inventory item location is required.");
        }

        FoodPortion portion = inventoryItem.getPortion();
        if (portion == null) {
            throw new IllegalArgumentException("Inventory item portion is required.");
        }
        if (portion.getFoodId() == null) {
            throw new IllegalArgumentException("Inventory item foodId is required.");
        }
        if (portion.getQuantity() == null || portion.getQuantity() <= 0) {
            throw new IllegalArgumentException("Inventory item quantity must be positive.");
        }
        if (portion.getUnit() == null) {
            throw new IllegalArgumentException("Inventory item unit is required.");
        }
    }
}
