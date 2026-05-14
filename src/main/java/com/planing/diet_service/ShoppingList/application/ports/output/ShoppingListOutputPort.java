package com.planing.diet_service.ShoppingList.application.ports.output;

import com.planing.diet_service.ShoppingList.domain.model.ShoppingList;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingListItem;

import java.time.LocalDate;
import java.util.Optional;

public interface ShoppingListOutputPort {

    Optional<ShoppingList> findCurrentByWeekStart(LocalDate weekStart);

    ShoppingList save(ShoppingList shoppingList);

    Optional<ShoppingList> findById(Long id);

    Optional<ShoppingListItem> findItemById(Long itemId);

    void deleteById(Long id);

    void deleteByWeekStartBetween(LocalDate from, LocalDate to);
}
