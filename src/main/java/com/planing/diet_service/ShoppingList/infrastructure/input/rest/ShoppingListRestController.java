package com.planing.diet_service.ShoppingList.infrastructure.input.rest;

import com.planing.diet.api.ShoppingListsApi;
import com.planing.diet.dto.FoodPortionDto;
import com.planing.diet.dto.ShoppingListRequest;
import com.planing.diet.dto.ShoppingListResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ShoppingListRestController implements ShoppingListsApi {
    @Override
    public ResponseEntity<ShoppingListResponse> addItemToShoppingList(Long listId, FoodPortionDto foodPortionDto) {
        return null;
    }

    @Override
    public ResponseEntity<ShoppingListResponse> createShoppingList(ShoppingListRequest shoppingListRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteShoppingList(Long listId) {
        return null;
    }

    @Override
    public ResponseEntity<List<ShoppingListResponse>> getAllShoppingLists() {
        return null;
    }

    @Override
    public ResponseEntity<ShoppingListResponse> getShoppingListById(Long listId) {
        return null;
    }

    @Override
    public ResponseEntity<ShoppingListResponse> removeItemFromShoppingList(Long listId, Long foodId) {
        return null;
    }

    @Override
    public ResponseEntity<ShoppingListResponse> updateShoppingList(Long listId, ShoppingListRequest shoppingListRequest) {
        return null;
    }
}
