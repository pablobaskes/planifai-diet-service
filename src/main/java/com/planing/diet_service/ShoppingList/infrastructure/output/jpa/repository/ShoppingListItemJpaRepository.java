package com.planing.diet_service.ShoppingList.infrastructure.output.jpa.repository;

import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.entity.ShoppingListItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingListItemJpaRepository extends JpaRepository<ShoppingListItemEntity, Long> {
}
