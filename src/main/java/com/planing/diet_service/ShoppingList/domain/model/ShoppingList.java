package com.planing.diet_service.ShoppingList.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingList {
    private Long id;
    private LocalDate weekStart;
    private ShoppingListStatus status;

    @Builder.Default
    private List<ShoppingListItem> items = new ArrayList<>();

    // ─────────────────────────────────────────────────────────
    // Lógica de dominio: recalcula el estado de la lista
    // basándose en el estado de compra de sus items.
    // Encapsulamos aquí para que ninguna capa externa
    // pueda calcular el estado de forma incorrecta.
    // ─────────────────────────────────────────────────────────
    public void recalculateStatus() {
        if (items == null || items.isEmpty()) {
            this.status = ShoppingListStatus.PENDING;
            return;
        }

        long purchased = items.stream().filter(ShoppingListItem::isPurchased).count();

        if (purchased == 0) {
            this.status = ShoppingListStatus.PENDING;
        } else if (purchased == items.size()) {
            this.status = ShoppingListStatus.COMPLETED;
        } else {
            this.status = ShoppingListStatus.PARTIALLY_COMPLETED;
        }
    }
}
