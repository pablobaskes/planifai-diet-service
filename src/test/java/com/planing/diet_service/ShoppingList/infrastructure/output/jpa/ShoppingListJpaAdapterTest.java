package com.planing.diet_service.ShoppingList.infrastructure.output.jpa;

import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.entity.ShoppingListEntity;
import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.mapper.ShoppingListJpaMapper;
import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.repository.ShoppingListItemJpaRepository;
import com.planing.diet_service.ShoppingList.infrastructure.output.jpa.repository.ShoppingListJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingListJpaAdapterTest {

    @Mock
    private ShoppingListJpaRepository shoppingListJpaRepository;

    @Mock
    private ShoppingListItemJpaRepository shoppingListItemJpaRepository;

    @Mock
    private ShoppingListJpaMapper shoppingListJpaMapper;

    @Test
    void deleteByWeekStartBetweenLoadsManagedListsWithItemsBeforeDeleting() {
        LocalDate from = LocalDate.of(2026, 5, 11);
        LocalDate to = from.plusDays(6);
        ShoppingListEntity list = new ShoppingListEntity();
        list.setId(1L);

        when(shoppingListJpaRepository.findAllByWeekStartBetweenWithItems(from, to))
                .thenReturn(List.of(list));

        adapter().deleteByWeekStartBetween(from, to);

        verify(shoppingListJpaRepository).findAllByWeekStartBetweenWithItems(from, to);
        verify(shoppingListJpaRepository).deleteAll(List.of(list));
    }

    private ShoppingListJpaAdapter adapter() {
        return new ShoppingListJpaAdapter(
                shoppingListJpaRepository,
                shoppingListItemJpaRepository,
                shoppingListJpaMapper);
    }
}
