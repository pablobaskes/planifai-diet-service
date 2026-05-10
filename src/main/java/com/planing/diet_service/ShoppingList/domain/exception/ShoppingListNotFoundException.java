package com.planing.diet_service.ShoppingList.domain.exception;

public class ShoppingListNotFoundException extends RuntimeException {
    public ShoppingListNotFoundException(String message) {
        super(message);
    }
}
