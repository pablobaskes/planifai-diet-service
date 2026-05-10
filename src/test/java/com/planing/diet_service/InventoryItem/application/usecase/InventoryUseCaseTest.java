package com.planing.diet_service.InventoryItem.application.usecase;

import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.FoodPortion.domain.model.Unit;
import com.planing.diet_service.InventoryItem.application.ports.output.InventoryItemOutputPort;
import com.planing.diet_service.InventoryItem.domain.model.InventoryItem;
import com.planing.diet_service.InventoryItem.domain.model.StorageLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryUseCaseTest {

    @Mock
    private InventoryItemOutputPort inventoryItemOutputPort;

    @InjectMocks
    private InventoryUseCase inventoryUseCase;

    @Nested
    @DisplayName("getAllInventoryItems")
    class GetAllInventoryItems {

        @Test
        @DisplayName("returns all inventory items when no location filter is provided")
        void returnsAllInventoryItemsWithoutFilter() {
            InventoryItem pantryItem = inventoryItem(1L, 10L, 2.0, Unit.KG, StorageLocation.PANTRY);
            InventoryItem fridgeItem = inventoryItem(2L, 11L, 1.0, Unit.L, StorageLocation.FRIDGE);
            when(inventoryItemOutputPort.findAll()).thenReturn(List.of(pantryItem, fridgeItem));

            List<InventoryItem> result = inventoryUseCase.getAllInventoryItems(null);

            assertThat(result).containsExactly(pantryItem, fridgeItem);
            verify(inventoryItemOutputPort).findAll();
        }

        @Test
        @DisplayName("filters inventory items by location")
        void filtersInventoryItemsByLocation() {
            InventoryItem pantryItem = inventoryItem(1L, 10L, 2.0, Unit.KG, StorageLocation.PANTRY);
            InventoryItem fridgeItem = inventoryItem(2L, 11L, 1.0, Unit.L, StorageLocation.FRIDGE);
            when(inventoryItemOutputPort.findAll()).thenReturn(List.of(pantryItem, fridgeItem));

            List<InventoryItem> result = inventoryUseCase.getAllInventoryItems(StorageLocation.PANTRY);

            assertThat(result).containsExactly(pantryItem);
        }
    }

    @Nested
    @DisplayName("getInventoryItemById")
    class GetInventoryItemById {

        @Test
        @DisplayName("returns inventory item when found")
        void returnsInventoryItemWhenFound() {
            InventoryItem item = inventoryItem(1L, 10L, 2.0, Unit.KG, StorageLocation.PANTRY);
            when(inventoryItemOutputPort.findById(1L)).thenReturn(Optional.of(item));

            InventoryItem result = inventoryUseCase.getInventoryItemById(1L);

            assertThat(result).isSameAs(item);
        }

        @Test
        @DisplayName("throws NoSuchElementException when item does not exist")
        void throwsWhenInventoryItemDoesNotExist() {
            when(inventoryItemOutputPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryUseCase.getInventoryItemById(99L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("throws IllegalArgumentException for invalid id")
        void throwsForInvalidId() {
            assertThatThrownBy(() -> inventoryUseCase.getInventoryItemById(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");

            verify(inventoryItemOutputPort, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("createInventoryItem")
    class CreateInventoryItem {

        @Test
        @DisplayName("creates inventory item and clears incoming id")
        void createsInventoryItemAndClearsIncomingId() {
            InventoryItem request = inventoryItem(55L, 10L, 2.0, Unit.KG, StorageLocation.PANTRY);
            when(inventoryItemOutputPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            InventoryItem result = inventoryUseCase.createInventoryItem(request);

            assertThat(result.getId()).isNull();
            ArgumentCaptor<InventoryItem> captor = ArgumentCaptor.forClass(InventoryItem.class);
            verify(inventoryItemOutputPort).save(captor.capture());
            assertThat(captor.getValue().getId()).isNull();
            assertThat(captor.getValue().getPortion().getFoodId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when location is missing")
        void throwsWhenLocationIsMissing() {
            InventoryItem request = inventoryItem(null, 10L, 2.0, Unit.KG, null);

            assertThatThrownBy(() -> inventoryUseCase.createInventoryItem(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("location");

            verify(inventoryItemOutputPort, never()).save(any());
        }

        @Test
        @DisplayName("throws IllegalArgumentException when quantity is not positive")
        void throwsWhenQuantityIsNotPositive() {
            InventoryItem request = inventoryItem(null, 10L, 0.0, Unit.KG, StorageLocation.PANTRY);

            assertThatThrownBy(() -> inventoryUseCase.createInventoryItem(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("quantity");

            verify(inventoryItemOutputPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateInventoryItem")
    class UpdateInventoryItem {

        @Test
        @DisplayName("updates existing inventory item with path id")
        void updatesExistingInventoryItemWithPathId() {
            InventoryItem request = inventoryItem(null, 10L, 3.0, Unit.KG, StorageLocation.FRIDGE);
            when(inventoryItemOutputPort.existsById(1L)).thenReturn(true);
            when(inventoryItemOutputPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            InventoryItem result = inventoryUseCase.updateInventoryItem(1L, request);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getLocation()).isEqualTo(StorageLocation.FRIDGE);
            verify(inventoryItemOutputPort).save(request);
        }

        @Test
        @DisplayName("throws NoSuchElementException when updating missing item")
        void throwsWhenUpdatingMissingInventoryItem() {
            when(inventoryItemOutputPort.existsById(99L)).thenReturn(false);
            InventoryItem request = inventoryItem(null, 10L, 3.0, Unit.KG, StorageLocation.FRIDGE);

            assertThatThrownBy(() -> inventoryUseCase.updateInventoryItem(99L, request))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("99");

            verify(inventoryItemOutputPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteInventoryItem")
    class DeleteInventoryItem {

        @Test
        @DisplayName("deletes existing inventory item")
        void deletesExistingInventoryItem() {
            when(inventoryItemOutputPort.existsById(1L)).thenReturn(true);

            inventoryUseCase.deleteInventoryItem(1L);

            verify(inventoryItemOutputPort).deleteById(1L);
        }

        @Test
        @DisplayName("throws NoSuchElementException when deleting missing item")
        void throwsWhenDeletingMissingInventoryItem() {
            when(inventoryItemOutputPort.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> inventoryUseCase.deleteInventoryItem(99L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("99");

            verify(inventoryItemOutputPort, never()).deleteById(any());
        }
    }

    private InventoryItem inventoryItem(
            Long id,
            Long foodId,
            Double quantity,
            Unit unit,
            StorageLocation location) {

        return InventoryItem.builder()
                .id(id)
                .portion(FoodPortion.builder()
                        .foodId(foodId)
                        .quantity(quantity)
                        .unit(unit)
                        .build())
                .location(location)
                .build();
    }
}
