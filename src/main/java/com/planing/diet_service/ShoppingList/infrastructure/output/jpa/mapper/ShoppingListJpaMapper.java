package com.planing.diet_service.ShoppingList.infrastructure.output.jpa.mapper;

import com.planing.diet_service.ShoppingList.domain.model.ShoppingList;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingListItem;
import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.entity.ShoppingListEntity;
import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.entity.ShoppingListItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShoppingListJpaMapper {

    // ── ShoppingList ──────────────────────────────────────────

    ShoppingList toDomain(ShoppingListEntity entity);

    @Mapping(target = "items", ignore = true)
    ShoppingListEntity toEntity(ShoppingList domain);

    // ── ShoppingListItem ──────────────────────────────────────

    ShoppingListItem toDomain(ShoppingListItemEntity entity);

    @Mapping(target = "shoppingList", ignore = true)
    ShoppingListItemEntity toEntity(ShoppingListItem domain);

    List<ShoppingListItem> toDomainItems(List<ShoppingListItemEntity> entities);
}
