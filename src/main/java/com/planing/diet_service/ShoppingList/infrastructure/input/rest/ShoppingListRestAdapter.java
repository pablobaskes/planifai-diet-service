package com.planing.diet_service.ShoppingList.infrastructure.input.rest;

import com.planing.diet.api.ShoppingListsApi;
import com.planing.diet.dto.ShoppingListResponse;
import com.planing.diet_service.ShoppingList.application.ports.input.ShoppingListInputPort;
import com.planing.diet_service.ShoppingList.infrastructure.input.rest.mapper.ShoppingListRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShoppingListRestAdapter implements ShoppingListsApi {
    private final ShoppingListInputPort shoppingListInputPort;
    private final ShoppingListRestMapper shoppingListRestMapper;

    @Override
    public ResponseEntity<ShoppingListResponse> generateWeeklyShoppingList() {
        return ResponseEntity.ok(
                shoppingListRestMapper.toResponse(
                        shoppingListInputPort.generateWeeklyShoppingList()
                )
        );
    }

    @Override
    public ResponseEntity<ShoppingListResponse> getCurrentShoppingList() {
        return ResponseEntity.ok(
                shoppingListRestMapper.toResponse(
                        shoppingListInputPort.getCurrentShoppingList()
                )
        );
    }

    @Override
    public ResponseEntity<ShoppingListResponse> purchaseShoppingListItem(Long itemId) {
        return ResponseEntity.ok(
                shoppingListRestMapper.toResponse(
                        shoppingListInputPort.markItemAsPurchased(itemId)
                )
        );
    }

    @Override
    public ResponseEntity<ShoppingListResponse> purchaseAllShoppingListItems() {
        return ResponseEntity.ok(
                shoppingListRestMapper.toResponse(
                        shoppingListInputPort.markAllItemsAsPurchased()
                )
        );
    }
}
