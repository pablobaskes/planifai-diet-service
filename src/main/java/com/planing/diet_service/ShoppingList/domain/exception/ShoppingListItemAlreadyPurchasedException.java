package com.planing.diet_service.ShoppingList.domain.exception;

public class ShoppingListItemAlreadyPurchasedException extends RuntimeException {
    public ShoppingListItemAlreadyPurchasedException(Long itemId) {
        super("ShoppingListItem with id " + itemId + " is already purchased.");
    }
}
