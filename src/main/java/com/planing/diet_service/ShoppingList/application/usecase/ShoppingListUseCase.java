package com.planing.diet_service.ShoppingList.application.usecase;


import com.planing.diet_service.Diet.application.ports.output.DietOutputPort;
import com.planing.diet_service.Diet.domain.exception.MultipleActiveDietsFoundException;
import com.planing.diet_service.Diet.domain.exception.NoActiveDietFoundException;
import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Food.application.ports.output.FoodOutputPort;
import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.FoodPortion.domain.model.Unit;
import com.planing.diet_service.InventoryItem.application.ports.output.InventoryItemOutputPort;
import com.planing.diet_service.InventoryItem.domain.model.InventoryItem;
import com.planing.diet_service.InventoryItem.domain.model.StorageLocation;
import com.planing.diet_service.ShoppingList.application.ports.input.ShoppingListInputPort;
import com.planing.diet_service.ShoppingList.application.ports.output.ShoppingListOutputPort;
import com.planing.diet_service.ShoppingList.domain.exception.ShoppingListItemAlreadyPurchasedException;
import com.planing.diet_service.ShoppingList.domain.exception.ShoppingListNotFoundException;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingList;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingListItem;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingListStatus;
import com.planing.diet_service.ShoppingList.domain.service.UnitConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingListUseCase implements ShoppingListInputPort {

    private final ShoppingListOutputPort shoppingListOutputPort;
    private final InventoryItemOutputPort inventoryItemOutputPort;
    private final DietOutputPort dietOutputPort;
    private final FoodOutputPort foodOutputPort;
    private final UnitConversionService unitConversionService;

    // ─────────────────────────────────────────────────────────
    // Genera la lista de la compra para los próximos 7 días.
    // Si ya existe para hoy, devuelve la existente.
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ShoppingList generateWeeklyShoppingList() {
        LocalDate today   = LocalDate.now();
        LocalDate weekEnd = today.plusDays(6);

        // Idempotencia: si ya existe, devolver la existente
        return shoppingListOutputPort.findCurrentByWeekStart(today)
                .orElseGet(() -> generateNew(today, weekEnd));
    }

    @Override
    public ShoppingList getCurrentShoppingList() {
        LocalDate today = LocalDate.now();
        return shoppingListOutputPort.findCurrentByWeekStart(today)
                .orElseThrow(() -> new ShoppingListNotFoundException(
                        "No shopping list found for current week starting " + today));
    }

    // ─────────────────────────────────────────────────────────
    // Marca un item como comprado y actualiza el inventario.
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ShoppingList markItemAsPurchased(Long itemId) {
        ShoppingListItem item = shoppingListOutputPort.findItemById(itemId)
                .orElseThrow(() -> new ShoppingListNotFoundException(
                        "ShoppingListItem not found with id: " + itemId));

        if (item.isPurchased()) {
            throw new ShoppingListItemAlreadyPurchasedException(itemId);
        }

        purchaseItem(item);

        // Recuperar la lista completa para recalcular estado y guardar
        ShoppingList shoppingList = shoppingListOutputPort.findCurrentByWeekStart(LocalDate.now())
                .orElseThrow(() -> new ShoppingListNotFoundException(
                        "Shopping list not found for current week"));

        shoppingList.recalculateStatus();
        return shoppingListOutputPort.save(shoppingList);
    }

    // ─────────────────────────────────────────────────────────
    // Marca todos los items pendientes como comprados.
    // ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ShoppingList markAllItemsAsPurchased() {
        ShoppingList shoppingList = shoppingListOutputPort.findCurrentByWeekStart(LocalDate.now())
                .orElseThrow(() -> new ShoppingListNotFoundException(
                        "No shopping list found for current week"));

        shoppingList.getItems().stream()
                .filter(item -> !item.isPurchased())
                .forEach(this::purchaseItem);

        shoppingList.recalculateStatus();
        return shoppingListOutputPort.save(shoppingList);
    }

    // ─────────────────────────────────────────────────────────
    // Generación interna de la lista
    // ─────────────────────────────────────────────────────────

    private ShoppingList generateNew(LocalDate today, LocalDate weekEnd) {
        log.info("Generating new shopping list for week {} - {}", today, weekEnd);

        Diet diet = resolveActiveDiet(today, weekEnd);
        List<FoodPortion> allIngredients = extractIngredients(diet, today, weekEnd);
        Map<String, AggregatedIngredient> aggregated = aggregateIngredients(allIngredients);
        List<InventoryItem> inventory = inventoryItemOutputPort.findAll();
        List<ShoppingListItem> items = buildShoppingItems(aggregated, inventory);

        ShoppingList shoppingList = ShoppingList.builder()
                .weekStart(today)
                .status(ShoppingListStatus.PENDING)
                .items(items)
                .build();

        return shoppingListOutputPort.save(shoppingList);
    }

    private Diet resolveActiveDiet(LocalDate from, LocalDate to) {
        List<Diet> diets = dietOutputPort.findDietsByDateRange(from, to);

        if (diets.isEmpty()) {
            throw new NoActiveDietFoundException();
        }
        if (diets.size() > 1) {
            throw new MultipleActiveDietsFoundException(diets.size());
        }
        return diets.get(0);
    }

    private List<FoodPortion> extractIngredients(Diet diet, LocalDate from, LocalDate to) {
        List<FoodPortion> ingredients = new ArrayList<>();

        diet.getDays().stream()
                .filter(day -> !day.getDate().isBefore(from) && !day.getDate().isAfter(to))
                .forEach(day -> day.getMealSlots().forEach(slot -> {
                    if (slot.getRecipe() != null && slot.getRecipe().getIngredients() != null) {
                        ingredients.addAll(slot.getRecipe().getIngredients());
                    }
                }));

        return ingredients;
    }

    // ─────────────────────────────────────────────────────────
    // Agrega ingredientes por foodId + unidad canónica.
    // KG se convierte a G, L a ML para sumar correctamente.
    // ─────────────────────────────────────────────────────────
    private Map<String, AggregatedIngredient> aggregateIngredients(List<FoodPortion> portions) {
        Map<String, AggregatedIngredient> map = new HashMap<>();

        for (FoodPortion portion : portions) {
            if (portion.getFoodId() == null || portion.getUnit() == null) continue;

            Unit canonical = unitConversionService.canonicalUnit(portion.getUnit());
            double canonicalQty = unitConversionService.toCanonical(portion.getQuantity(), portion.getUnit());

            String key = portion.getFoodId() + "_" + canonical.name();

            map.merge(key,
                    new AggregatedIngredient(portion.getFoodId(), canonicalQty, canonical),
                    (existing, incoming) -> {
                        existing.quantity += incoming.quantity;
                        return existing;
                    });
        }
        return map;
    }

    private List<ShoppingListItem> buildShoppingItems(
            Map<String, AggregatedIngredient> aggregated,
            List<InventoryItem> inventory) {

        List<ShoppingListItem> items = new ArrayList<>();

        for (AggregatedIngredient agg : aggregated.values()) {
            double available = resolveAvailableQuantity(agg, inventory);
            double missing   = Math.max(agg.quantity - available, 0);

            if (missing <= 0) continue; // tenemos suficiente en inventario

            String foodName = foodOutputPort.findById(agg.foodId)
                    .map(f -> f.getName())
                    .orElse("Unknown food " + agg.foodId);

            items.add(ShoppingListItem.builder()
                    .foodId(agg.foodId)
                    .foodName(foodName)
                    .requiredQuantity(agg.quantity)
                    .availableQuantity(available)
                    .missingQuantity(missing)
                    .unit(agg.unit)
                    .purchased(false)
                    .build());
        }

        return items;
    }

    private double resolveAvailableQuantity(AggregatedIngredient agg, List<InventoryItem> inventory) {
        return inventory.stream()
                .filter(item -> item.getPortion() != null
                        && agg.foodId.equals(item.getPortion().getFoodId()))
                .filter(item -> unitConversionService.areCompatible(
                        item.getPortion().getUnit(), agg.unit))
                .mapToDouble(item -> unitConversionService.convert(
                        item.getPortion().getQuantity(),
                        item.getPortion().getUnit(),
                        agg.unit))
                .sum();
    }

    // ─────────────────────────────────────────────────────────
    // Lógica de compra de un item individual.
    // Reutilizada por markItemAsPurchased y markAllItemsAsPurchased.
    // ─────────────────────────────────────────────────────────
    private void purchaseItem(ShoppingListItem item) {
        inventoryItemOutputPort.findByFoodId(item.getFoodId())
                .ifPresentOrElse(
                        existing -> {
                            // Sumar al inventario existente (convertir si hace falta)
                            Unit inventoryUnit = existing.getPortion().getUnit();
                            double addQty = unitConversionService.areCompatible(item.getUnit(), inventoryUnit)
                                    ? unitConversionService.convert(item.getMissingQuantity(), item.getUnit(), inventoryUnit)
                                    : item.getMissingQuantity();
                            existing.getPortion().setQuantity(existing.getPortion().getQuantity() + addQty);
                            inventoryItemOutputPort.save(existing);
                        },
                        () -> {
                            // Crear nuevo inventoryItem
                            FoodPortion portion = FoodPortion.builder()
                                    .foodId(item.getFoodId())
                                    .quantity(item.getMissingQuantity())
                                    .unit(item.getUnit())
                                    .build();
                            InventoryItem newItem = InventoryItem.builder()
                                    .portion(portion)
                                    .location(StorageLocation.PANTRY)
                                    .build();
                            inventoryItemOutputPort.save(newItem);
                        });

        item.setPurchased(true);
    }

    // ── Inner record para agregación ──────────────────────────

    private static class AggregatedIngredient {
        Long foodId;
        double quantity;
        Unit unit;

        AggregatedIngredient(Long foodId, double quantity, Unit unit) {
            this.foodId   = foodId;
            this.quantity = quantity;
            this.unit     = unit;
        }
    }
}
