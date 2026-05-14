package com.planing.diet_service.ShoppingList.infrastructure.output.jpa;

import com.planing.diet_service.ShoppingList.application.ports.output.ShoppingListOutputPort;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingList;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingListItem;
import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.entity.ShoppingListEntity;
import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.entity.ShoppingListItemEntity;
import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.mapper.ShoppingListJpaMapper;
import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.repository.ShoppingListItemJpaRepository;
import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.repository.ShoppingListJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShoppingListJpaAdapter implements ShoppingListOutputPort {

    private final ShoppingListJpaRepository shoppingListJpaRepository;
    private final ShoppingListItemJpaRepository shoppingListItemJpaRepository;
    private final ShoppingListJpaMapper shoppingListJpaMapper;

    @Override
    public Optional<ShoppingList> findCurrentByWeekStart(LocalDate weekStart) {
        return shoppingListJpaRepository.findByWeekStart(weekStart)
                .map(shoppingListJpaMapper::toDomain);
    }

    @Override
    public Optional<ShoppingList> findByWeekStartAndDietId(LocalDate weekStart, Long dietId) {
        return shoppingListJpaRepository.findByWeekStartAndDietId(weekStart, dietId)
                .map(shoppingListJpaMapper::toDomain);
    }

    @Override
    public ShoppingList save(ShoppingList shoppingList) {
        ShoppingListEntity entity = shoppingListJpaMapper.toEntity(shoppingList);

        // Sincronizar items manualmente para mantener la FK bidireccional
        if (shoppingList.getItems() != null) {
            shoppingList.getItems().forEach(item -> {
                ShoppingListItemEntity itemEntity = shoppingListJpaMapper.toEntity(item);
                itemEntity.setShoppingList(entity);
                entity.getItems().add(itemEntity);
            });
        }

        return shoppingListJpaMapper.toDomain(shoppingListJpaRepository.save(entity));
    }

    @Override
    public Optional<ShoppingList> findById(Long id) {
        return shoppingListJpaRepository.findById(id)
                .map(shoppingListJpaMapper::toDomain);
    }

    @Override
    public Optional<ShoppingListItem> findItemById(Long itemId) {
        return shoppingListItemJpaRepository.findById(itemId)
                .map(shoppingListJpaMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shoppingListJpaRepository.deleteById(id);
    }
}
