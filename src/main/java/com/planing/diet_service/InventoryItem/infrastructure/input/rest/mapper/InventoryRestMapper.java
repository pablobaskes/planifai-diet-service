package com.planing.diet_service.InventoryItem.infrastructure.input.rest.mapper;

import com.planing.diet.dto.FoodPortionDto;
import com.planing.diet.dto.InventoryItemRequest;
import com.planing.diet.dto.InventoryItemResponse;
import com.planing.diet.dto.MeasureUnit;
import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.FoodPortion.domain.model.Unit;
import com.planing.diet_service.InventoryItem.domain.model.InventoryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryRestMapper {

    @Mapping(target = "id", ignore = true)
    InventoryItem toDomain(InventoryItemRequest inventoryItemRequest);

    InventoryItemResponse toResponse(InventoryItem inventoryItem);

    List<InventoryItemResponse> toResponseList(List<InventoryItem> inventoryItems);

    @Mapping(target = "unit", expression = "java(mapUnit(dto.getUnit()))")
    FoodPortion toDomain(FoodPortionDto dto);

    @Mapping(target = "unit", expression = "java(mapMeasureUnit(portion.getUnit()))")
    FoodPortionDto toDto(FoodPortion portion);

    default com.planing.diet_service.InventoryItem.domain.model.StorageLocation toDomain(
            com.planing.diet.dto.StorageLocation location) {
        return location == null
                ? null
                : com.planing.diet_service.InventoryItem.domain.model.StorageLocation.valueOf(location.name());
    }

    default com.planing.diet.dto.StorageLocation toDto(
            com.planing.diet_service.InventoryItem.domain.model.StorageLocation location) {
        return location == null
                ? null
                : com.planing.diet.dto.StorageLocation.valueOf(location.name());
    }

    default Unit mapUnit(MeasureUnit measureUnit) {
        return measureUnit == null
                ? null
                : Unit.valueOf(measureUnit.name());
    }

    default MeasureUnit mapMeasureUnit(Unit unit) {
        return unit == null
                ? null
                : MeasureUnit.valueOf(unit.name());
    }
}
