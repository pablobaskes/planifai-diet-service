package com.planing.diet_service.InventoryItem.infrastructure.input.rest;

import com.planing.diet.api.InventoryApi;
import com.planing.diet.dto.InventoryItemRequest;
import com.planing.diet.dto.InventoryItemResponse;
import com.planing.diet.dto.StorageLocation;
import com.planing.diet_service.InventoryItem.application.ports.input.InventoryInputPort;
import com.planing.diet_service.InventoryItem.domain.model.InventoryItem;
import com.planing.diet_service.InventoryItem.infrastructure.input.rest.mapper.InventoryRestMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class InventoryRestAdapter implements InventoryApi {

    private final InventoryInputPort inventoryInputPort;
    private final InventoryRestMapper inventoryRestMapper;

    @Override
    public ResponseEntity<InventoryItemResponse> createInventoryItem(InventoryItemRequest inventoryItemRequest) {
        InventoryItem inventoryItem = inventoryInputPort.createInventoryItem(
                inventoryRestMapper.toDomain(inventoryItemRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryRestMapper.toResponse(inventoryItem));
    }

    @Override
    public ResponseEntity<Void> deleteInventoryItem(Long itemId) {
        inventoryInputPort.deleteInventoryItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<InventoryItemResponse>> getAllInventoryItems(StorageLocation location) {
        List<InventoryItem> inventoryItems = inventoryInputPort.getAllInventoryItems(
                inventoryRestMapper.toDomain(location));
        return ResponseEntity.ok(inventoryRestMapper.toResponseList(inventoryItems));
    }

    @Override
    public ResponseEntity<InventoryItemResponse> getInventoryItemById(Long itemId) {
        InventoryItem inventoryItem = inventoryInputPort.getInventoryItemById(itemId);
        return ResponseEntity.ok(inventoryRestMapper.toResponse(inventoryItem));
    }

    @Override
    public ResponseEntity<InventoryItemResponse> updateInventoryItem(Long itemId, InventoryItemRequest inventoryItemRequest) {
        InventoryItem inventoryItem = inventoryInputPort.updateInventoryItem(
                itemId,
                inventoryRestMapper.toDomain(inventoryItemRequest));
        return ResponseEntity.ok(inventoryRestMapper.toResponse(inventoryItem));
    }
}
