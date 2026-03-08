package com.planing.diet_service.InventoryItem.infrastructure.output.jpa.entity;

import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.InventoryItem.domain.model.StorageLocation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
public class InventoryItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private FoodPortion portion;

    @Enumerated(EnumType.STRING)
    private StorageLocation location;
}