package com.planing.diet_service.ShoppingList.infrastructure.output.jpa.entity;

import com.planing.diet_service.ShoppingList.domain.model.ShoppingListStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "shopping_lists",
        uniqueConstraints = @UniqueConstraint(name = "uk_shopping_lists_week_diet", columnNames = {"week_start", "diet_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class ShoppingListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "diet_id")
    private Long dietId;

    @Column(name = "week_start")
    private LocalDate weekStart;

    @Enumerated(EnumType.STRING)
    private ShoppingListStatus status;

    @OneToMany(
            mappedBy = "shoppingList",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ShoppingListItemEntity> items = new ArrayList<>();
}
