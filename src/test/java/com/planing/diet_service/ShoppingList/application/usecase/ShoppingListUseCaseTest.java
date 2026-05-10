package com.planing.diet_service.ShoppingList.application.usecase;

import com.planing.diet_service.Diet.application.ports.output.DietOutputPort;
import com.planing.diet_service.Food.application.ports.output.FoodOutputPort;
import com.planing.diet_service.InventoryItem.application.ports.output.InventoryItemOutputPort;
import com.planing.diet_service.ShoppingList.application.ports.output.ShoppingListOutputPort;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingList;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingListStatus;
import com.planing.diet_service.ShoppingList.domain.service.UnitConversionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingListUseCaseTest {

    @Mock
    private ShoppingListOutputPort shoppingListOutputPort;

    @Mock
    private InventoryItemOutputPort inventoryItemOutputPort;

    @Mock
    private DietOutputPort dietOutputPort;

    @Mock
    private FoodOutputPort foodOutputPort;

    @Mock
    private UnitConversionService unitConversionService;

    @InjectMocks
    private ShoppingListUseCase shoppingListUseCase;

    @Test
    void generateWeeklyShoppingListReturnsExistingListForCurrentWeek() {
        LocalDate today = LocalDate.now();
        ShoppingList existing = ShoppingList.builder()
                .id(1L)
                .weekStart(today)
                .status(ShoppingListStatus.PENDING)
                .build();

        when(shoppingListOutputPort.findCurrentByWeekStart(today)).thenReturn(Optional.of(existing));

        ShoppingList result = shoppingListUseCase.generateWeeklyShoppingList();

        assertThat(result).isSameAs(existing);
        verify(shoppingListOutputPort).findCurrentByWeekStart(today);
        verify(shoppingListOutputPort, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(dietOutputPort, inventoryItemOutputPort, foodOutputPort, unitConversionService);
    }
}
