package com.planing.diet_service.InventoryItem.infrastructure.output.jpa.mapper;

import com.planing.diet_service.InventoryItem.domain.model.InventoryItem;
import com.planing.diet_service.InventoryItem.infrastructure.output.jpa.entity.InventoryItemEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryItemJpaMapper {

    InventoryItem toDomain(InventoryItemEntity entity);

    InventoryItemEntity toEntity(InventoryItem domain);

    List<InventoryItem> toDomain(List<InventoryItemEntity> inventoryItemEntities);
}
