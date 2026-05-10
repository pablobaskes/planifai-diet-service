package com.planing.diet_service.InventoryItem.infrastructure.output.jpa;

import com.planing.diet_service.InventoryItem.application.ports.output.InventoryItemOutputPort;
import com.planing.diet_service.InventoryItem.domain.model.InventoryItem;
import com.planing.diet_service.InventoryItem.infrastructure.output.jpa.entity.InventoryItemEntity;
import com.planing.diet_service.InventoryItem.infrastructure.output.jpa.mapper.InventoryItemJpaMapper;
import com.planing.diet_service.InventoryItem.infrastructure.output.jpa.repository.InventoryItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InventoryItemJpaAdapter implements InventoryItemOutputPort {

    private final InventoryItemJpaMapper inventoryItemJpaMapper;
    private final InventoryItemJpaRepository inventoryItemJpaRepository;

    @Override
    public List<InventoryItem> findAll() {
        return inventoryItemJpaMapper.toDomain(inventoryItemJpaRepository.findAll());
    }

    @Override
    public Optional<InventoryItem> findByFoodId(Long foodId) {
        return inventoryItemJpaRepository.findByFoodId(foodId)
                .map(inventoryItemJpaMapper::toDomain);
    }

    @Override
    public InventoryItem save(InventoryItem inventoryItem) {
        InventoryItemEntity entity = inventoryItemJpaMapper.toEntity(inventoryItem);
        InventoryItemEntity savedEntity = inventoryItemJpaRepository.save(entity);
        return inventoryItemJpaMapper.toDomain(savedEntity);
    }
}
