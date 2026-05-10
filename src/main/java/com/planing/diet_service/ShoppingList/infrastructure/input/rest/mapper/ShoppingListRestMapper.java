package com.planing.diet_service.ShoppingList.infrastructure.input.rest.mapper;

import com.planing.diet.dto.ShoppingListItemResponse;
import com.planing.diet.dto.ShoppingListResponse;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingList;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingListItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShoppingListRestMapper {

    ShoppingListResponse toResponse(ShoppingList shoppingList);

    ShoppingListItemResponse toResponse(ShoppingListItem shoppingListItem);

    List<ShoppingListItemResponse> toItemResponseList(List<ShoppingListItem> shoppingListItems);
}
