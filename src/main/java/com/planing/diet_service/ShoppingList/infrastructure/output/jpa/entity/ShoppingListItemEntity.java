package com.planing.diet_service.ShoppingList.infrastructure.output.jpa.entity;

import com.planing.diet_service.FoodPortion.domain.model.Unit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shopping_list_items")
@Getter
@Setter
@NoArgsConstructor
public class ShoppingListItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id")
    private ShoppingListEntity shoppingList;

    private Long foodId;
    private String foodName;
    private Double requiredQuantity;
    private Double availableQuantity;
    private Double missingQuantity;

    @Enumerated(EnumType.STRING)
    private Unit unit;

    private boolean purchased;
}
