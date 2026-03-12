package com.planing.diet_service.InventoryItem.infrastructure.input.rest;

import com.planing.diet.api.InventoryApi;
import com.planing.diet.dto.InventoryItemRequest;
import com.planing.diet.dto.InventoryItemResponse;
import com.planing.diet.dto.StorageLocation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class InventoryRestAdapter implements InventoryApi {
    @Override
    public ResponseEntity<InventoryItemResponse> createInventoryItem(InventoryItemRequest inventoryItemRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteInventoryItem(Long itemId) {
        return null;
    }

    @Override
    public ResponseEntity<List<InventoryItemResponse>> getAllInventoryItems(StorageLocation location) {
        return null;
    }

    @Override
    public ResponseEntity<InventoryItemResponse> getInventoryItemById(Long itemId) {
        return null;
    }

    @Override
    public ResponseEntity<InventoryItemResponse> updateInventoryItem(Long itemId, InventoryItemRequest inventoryItemRequest) {
        return null;
    }
}
