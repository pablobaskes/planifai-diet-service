package com.planing.diet_service.ShoppingList.application.ports.input;

import com.planing.diet_service.ShoppingList.domain.model.ShoppingList;

public interface ShoppingListInputPort {

    ShoppingList generateWeeklyShoppingList(Long dietId);

    ShoppingList getCurrentShoppingList();

    ShoppingList markItemAsPurchased(Long itemId);

    ShoppingList markAllItemsAsPurchased();
}
