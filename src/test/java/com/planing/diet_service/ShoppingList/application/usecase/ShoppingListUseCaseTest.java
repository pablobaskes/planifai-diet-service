package com.planing.diet_service.ShoppingList.application.usecase;

import com.planing.diet_service.Diet.application.ports.output.DietOutputPort;
import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.Food.application.ports.output.FoodOutputPort;
import com.planing.diet_service.Food.domain.model.Food;
import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.FoodPortion.domain.model.Unit;
import com.planing.diet_service.InventoryItem.application.ports.output.InventoryItemOutputPort;
import com.planing.diet_service.InventoryItem.domain.model.InventoryItem;
import com.planing.diet_service.InventoryItem.domain.model.StorageLocation;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import com.planing.diet_service.ShoppingList.application.ports.output.ShoppingListOutputPort;
import com.planing.diet_service.ShoppingList.domain.exception.ShoppingListItemAlreadyPurchasedException;
import com.planing.diet_service.ShoppingList.domain.exception.ShoppingListNotFoundException;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingList;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingListItem;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingListStatus;
import com.planing.diet_service.ShoppingList.domain.service.UnitConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    private ShoppingListUseCase shoppingListUseCase;

    @BeforeEach
    void setUp() {
        shoppingListUseCase = new ShoppingListUseCase(
                shoppingListOutputPort,
                inventoryItemOutputPort,
                dietOutputPort,
                foodOutputPort,
                new UnitConversionService());
    }

    @Nested
    @DisplayName("generateWeeklyShoppingList")
    class GenerateWeeklyShoppingList {

        @Test
        void returnsExistingListForCurrentWeek() {
            LocalDate today = LocalDate.now();
            ShoppingList existing = shoppingList(1L, today, ShoppingListStatus.PENDING, List.of());

            when(shoppingListOutputPort.findByWeekStartAndDietId(today, 1L)).thenReturn(Optional.of(existing));

            ShoppingList result = shoppingListUseCase.generateWeeklyShoppingList(1L);

            assertThat(result).isSameAs(existing);
            verify(shoppingListOutputPort).findByWeekStartAndDietId(today, 1L);
            verify(shoppingListOutputPort, never()).save(any());
            verifyNoInteractions(dietOutputPort, inventoryItemOutputPort, foodOutputPort);
        }

        @Test
        void generatesMissingItemsAfterSubtractingCompatibleInventory() {
            LocalDate today = LocalDate.now();
            FoodPortion riceNeeded = portion(10L, 2.0, Unit.KG);
            Diet activeDiet = dietWithIngredients(today, riceNeeded);
            InventoryItem inventory = inventoryItem(10L, 500.0, Unit.G);

            when(shoppingListOutputPort.findByWeekStartAndDietId(today, 1L)).thenReturn(Optional.empty());
            when(dietOutputPort.findDietByIdForShoppingList(1L)).thenReturn(Optional.of(activeDiet));
            when(inventoryItemOutputPort.findAll()).thenReturn(List.of(inventory));
            when(foodOutputPort.findById(10L)).thenReturn(Optional.of(Food.builder().id(10L).name("Rice").build()));
            when(shoppingListOutputPort.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingList result = shoppingListUseCase.generateWeeklyShoppingList(1L);

            assertThat(result.getDietId()).isEqualTo(1L);
            assertThat(result.getWeekStart()).isEqualTo(today);
            assertThat(result.getStatus()).isEqualTo(ShoppingListStatus.PENDING);
            assertThat(result.getItems()).hasSize(1);

            ShoppingListItem item = result.getItems().get(0);
            assertThat(item.getFoodId()).isEqualTo(10L);
            assertThat(item.getFoodName()).isEqualTo("Rice");
            assertThat(item.getRequiredQuantity()).isEqualTo(2000.0);
            assertThat(item.getAvailableQuantity()).isEqualTo(500.0);
            assertThat(item.getMissingQuantity()).isEqualTo(1500.0);
            assertThat(item.getUnit()).isEqualTo(Unit.G);
            assertThat(item.isPurchased()).isFalse();

            ArgumentCaptor<ShoppingList> savedList = ArgumentCaptor.forClass(ShoppingList.class);
            verify(shoppingListOutputPort).save(savedList.capture());
            assertThat(savedList.getValue().getItems()).hasSize(1);
        }

        @Test
        void throwsWhenRequestedDietDoesNotExist() {
            LocalDate today = LocalDate.now();
            when(shoppingListOutputPort.findByWeekStartAndDietId(today, 99L)).thenReturn(Optional.empty());
            when(dietOutputPort.findDietByIdForShoppingList(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shoppingListUseCase.generateWeeklyShoppingList(99L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("Diet not found with id: 99");

            verify(shoppingListOutputPort, never()).save(any());
            verifyNoInteractions(inventoryItemOutputPort, foodOutputPort);
        }

        @Test
        void generatesFromSelectedDietWhenMultipleDietsExistForWeek() {
            LocalDate today = LocalDate.now();
            Diet selectedDiet = dietWithIngredients(today, portion(10L, 100.0, Unit.G));

            when(shoppingListOutputPort.findByWeekStartAndDietId(today, 2L)).thenReturn(Optional.empty());
            when(dietOutputPort.findDietByIdForShoppingList(2L)).thenReturn(Optional.of(selectedDiet));
            when(inventoryItemOutputPort.findAll()).thenReturn(List.of());
            when(foodOutputPort.findById(10L)).thenReturn(Optional.of(Food.builder().id(10L).name("Rice").build()));
            when(shoppingListOutputPort.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingList result = shoppingListUseCase.generateWeeklyShoppingList(2L);

            assertThat(result.getDietId()).isEqualTo(2L);
            verify(dietOutputPort).findDietByIdForShoppingList(2L);
            verify(dietOutputPort, never()).findDietsByDateRangeForShoppingList(any(), any());
        }
    }

    @Nested
    @DisplayName("getCurrentShoppingList")
    class GetCurrentShoppingList {

        @Test
        void returnsCurrentList() {
            LocalDate today = LocalDate.now();
            ShoppingList current = shoppingList(1L, today, ShoppingListStatus.PENDING, List.of());

            when(shoppingListOutputPort.findCurrentByWeekStart(today)).thenReturn(Optional.of(current));

            assertThat(shoppingListUseCase.getCurrentShoppingList()).isSameAs(current);
        }

        @Test
        void throwsWhenCurrentListDoesNotExist() {
            LocalDate today = LocalDate.now();
            when(shoppingListOutputPort.findCurrentByWeekStart(today)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shoppingListUseCase.getCurrentShoppingList())
                    .isInstanceOf(ShoppingListNotFoundException.class)
                    .hasMessageContaining("No shopping list found for current week");
        }
    }

    @Nested
    @DisplayName("markItemAsPurchased")
    class MarkItemAsPurchased {

        @Test
        void marksItemPurchasedAndCreatesInventoryWhenFoodIsMissing() {
            LocalDate today = LocalDate.now();
            ShoppingListItem item = item(1L, 10L, 2.0, Unit.G, false);
            ShoppingList current = shoppingList(1L, today, ShoppingListStatus.PENDING, List.of(item));

            when(shoppingListOutputPort.findItemById(1L)).thenReturn(Optional.of(item));
            when(inventoryItemOutputPort.findByFoodId(10L)).thenReturn(Optional.empty());
            when(shoppingListOutputPort.findCurrentByWeekStart(today)).thenReturn(Optional.of(current));
            when(shoppingListOutputPort.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingList result = shoppingListUseCase.markItemAsPurchased(1L);

            assertThat(item.isPurchased()).isTrue();
            assertThat(result.getStatus()).isEqualTo(ShoppingListStatus.COMPLETED);

            ArgumentCaptor<InventoryItem> savedInventory = ArgumentCaptor.forClass(InventoryItem.class);
            verify(inventoryItemOutputPort).save(savedInventory.capture());
            assertThat(savedInventory.getValue().getLocation()).isEqualTo(StorageLocation.PANTRY);
            assertThat(savedInventory.getValue().getPortion().getFoodId()).isEqualTo(10L);
            assertThat(savedInventory.getValue().getPortion().getQuantity()).isEqualTo(2.0);
            assertThat(savedInventory.getValue().getPortion().getUnit()).isEqualTo(Unit.G);
            verify(shoppingListOutputPort).save(current);
        }

        @Test
        void marksItemPurchasedAndAddsConvertedQuantityToExistingInventory() {
            LocalDate today = LocalDate.now();
            ShoppingListItem item = item(1L, 10L, 500.0, Unit.G, false);
            ShoppingList current = shoppingList(1L, today, ShoppingListStatus.PENDING, List.of(item));
            InventoryItem existingInventory = inventoryItem(10L, 1.0, Unit.KG);

            when(shoppingListOutputPort.findItemById(1L)).thenReturn(Optional.of(item));
            when(inventoryItemOutputPort.findByFoodId(10L)).thenReturn(Optional.of(existingInventory));
            when(shoppingListOutputPort.findCurrentByWeekStart(today)).thenReturn(Optional.of(current));
            when(shoppingListOutputPort.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingList result = shoppingListUseCase.markItemAsPurchased(1L);

            assertThat(result.getStatus()).isEqualTo(ShoppingListStatus.COMPLETED);
            assertThat(existingInventory.getPortion().getQuantity()).isEqualTo(1.5);
            verify(inventoryItemOutputPort).save(existingInventory);
        }

        @Test
        void marksMatchingCurrentListItemWhenLoadedItemIsDetached() {
            LocalDate today = LocalDate.now();
            ShoppingListItem loadedItem = item(1L, 10L, 2.0, Unit.G, false);
            ShoppingListItem currentListItem = item(1L, 10L, 2.0, Unit.G, false);
            ShoppingList current = shoppingList(1L, today, ShoppingListStatus.PENDING, List.of(currentListItem));

            when(shoppingListOutputPort.findItemById(1L)).thenReturn(Optional.of(loadedItem));
            when(inventoryItemOutputPort.findByFoodId(10L)).thenReturn(Optional.empty());
            when(shoppingListOutputPort.findCurrentByWeekStart(today)).thenReturn(Optional.of(current));
            when(shoppingListOutputPort.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingList result = shoppingListUseCase.markItemAsPurchased(1L);

            assertThat(loadedItem.isPurchased()).isTrue();
            assertThat(currentListItem.isPurchased()).isTrue();
            assertThat(result.getStatus()).isEqualTo(ShoppingListStatus.COMPLETED);
            verify(shoppingListOutputPort).save(current);
        }

        @Test
        void throwsWhenPurchasedItemIsNotInCurrentList() {
            LocalDate today = LocalDate.now();
            ShoppingListItem loadedItem = item(1L, 10L, 2.0, Unit.G, false);
            ShoppingList current = shoppingList(1L, today, ShoppingListStatus.PENDING, List.of(item(2L, 11L, 2.0, Unit.G, false)));

            when(shoppingListOutputPort.findItemById(1L)).thenReturn(Optional.of(loadedItem));
            when(shoppingListOutputPort.findCurrentByWeekStart(today)).thenReturn(Optional.of(current));

            assertThatThrownBy(() -> shoppingListUseCase.markItemAsPurchased(1L))
                    .isInstanceOf(ShoppingListNotFoundException.class)
                    .hasMessageContaining("not found in current shopping list");

            verify(shoppingListOutputPort, never()).save(any());
            verifyNoInteractions(inventoryItemOutputPort);
        }

        @Test
        void throwsWhenItemDoesNotExist() {
            when(shoppingListOutputPort.findItemById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shoppingListUseCase.markItemAsPurchased(99L))
                    .isInstanceOf(ShoppingListNotFoundException.class)
                    .hasMessageContaining("ShoppingListItem not found");

            verifyNoInteractions(inventoryItemOutputPort);
            verify(shoppingListOutputPort, never()).save(any());
        }

        @Test
        void throwsWhenItemIsAlreadyPurchased() {
            ShoppingListItem item = item(1L, 10L, 2.0, Unit.G, true);
            when(shoppingListOutputPort.findItemById(1L)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> shoppingListUseCase.markItemAsPurchased(1L))
                    .isInstanceOf(ShoppingListItemAlreadyPurchasedException.class);

            verifyNoInteractions(inventoryItemOutputPort);
            verify(shoppingListOutputPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("markAllItemsAsPurchased")
    class MarkAllItemsAsPurchased {

        @Test
        void purchasesOnlyPendingItemsAndCompletesList() {
            LocalDate today = LocalDate.now();
            ShoppingListItem pending = item(1L, 10L, 2.0, Unit.G, false);
            ShoppingListItem purchased = item(2L, 11L, 1.0, Unit.G, true);
            ShoppingList current = shoppingList(1L, today, ShoppingListStatus.PARTIALLY_COMPLETED,
                    List.of(pending, purchased));

            when(shoppingListOutputPort.findCurrentByWeekStart(today)).thenReturn(Optional.of(current));
            when(inventoryItemOutputPort.findByFoodId(10L)).thenReturn(Optional.empty());
            when(shoppingListOutputPort.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingList result = shoppingListUseCase.markAllItemsAsPurchased();

            assertThat(pending.isPurchased()).isTrue();
            assertThat(purchased.isPurchased()).isTrue();
            assertThat(result.getStatus()).isEqualTo(ShoppingListStatus.COMPLETED);
            verify(inventoryItemOutputPort).findByFoodId(10L);
            verify(inventoryItemOutputPort, never()).findByFoodId(11L);
            verify(shoppingListOutputPort).save(current);
        }

        @Test
        void throwsWhenCurrentListDoesNotExist() {
            LocalDate today = LocalDate.now();
            when(shoppingListOutputPort.findCurrentByWeekStart(today)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shoppingListUseCase.markAllItemsAsPurchased())
                    .isInstanceOf(ShoppingListNotFoundException.class)
                    .hasMessageContaining("No shopping list found for current week");

            verifyNoInteractions(inventoryItemOutputPort);
            verify(shoppingListOutputPort, never()).save(any());
        }
    }

    private ShoppingList shoppingList(Long id, LocalDate weekStart, ShoppingListStatus status, List<ShoppingListItem> items) {
        return ShoppingList.builder()
                .id(id)
                .dietId(1L)
                .weekStart(weekStart)
                .status(status)
                .items(items)
                .build();
    }

    private ShoppingListItem item(Long id, Long foodId, Double missingQuantity, Unit unit, boolean purchased) {
        return ShoppingListItem.builder()
                .id(id)
                .foodId(foodId)
                .foodName("Food " + foodId)
                .requiredQuantity(missingQuantity)
                .availableQuantity(0.0)
                .missingQuantity(missingQuantity)
                .unit(unit)
                .purchased(purchased)
                .build();
    }

    private Diet dietWithIngredients(LocalDate day, FoodPortion... ingredients) {
        Recipe recipe = Recipe.builder()
                .id(1L)
                .name("Recipe")
                .ingredients(List.of(ingredients))
                .build();
        MealSlot slot = new MealSlot(1L, MealType.LUNCH, recipe, null);
        DietDay dietDay = new DietDay(1L, day, null, List.of(slot));
        slot.setDietDay(dietDay);
        return new Diet(1L, "Weekly diet", null, 2000, day, day.plusDays(6), List.of(dietDay));
    }

    private FoodPortion portion(Long foodId, Double quantity, Unit unit) {
        return FoodPortion.builder()
                .foodId(foodId)
                .quantity(quantity)
                .unit(unit)
                .build();
    }

    private InventoryItem inventoryItem(Long foodId, Double quantity, Unit unit) {
        return InventoryItem.builder()
                .portion(portion(foodId, quantity, unit))
                .location(StorageLocation.PANTRY)
                .build();
    }
}
