package com.planing.diet_service.ShoppingList.infrastructure.output.jpa.entity;

import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shopping_lists")
@Getter
@Setter
@NoArgsConstructor
public class ShoppingListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDate weekStart;

    @ElementCollection
    @CollectionTable(
            name = "shopping_list_items",
            joinColumns = @JoinColumn(name = "shopping_list_id")
    )
    private List<FoodPortion> items = new ArrayList<>();
}
