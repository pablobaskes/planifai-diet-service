package com.planing.diet_service.ShoppingList.infrastructure.input.rest;

import com.planing.diet.dto.ShoppingListItemResponse;
import com.planing.diet.dto.ShoppingListResponse;
import com.planing.diet.dto.ShoppingListStatus;
import com.planing.diet.dto.Unit;
import com.planing.diet_service.ShoppingList.application.ports.input.ShoppingListInputPort;
import com.planing.diet_service.ShoppingList.domain.exception.ShoppingListItemAlreadyPurchasedException;
import com.planing.diet_service.ShoppingList.domain.exception.ShoppingListNotFoundException;
import com.planing.diet_service.ShoppingList.domain.model.ShoppingList;
import com.planing.diet_service.ShoppingList.infrastructure.input.rest.mapper.ShoppingListRestMapper;
import com.planing.diet_service.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ShoppingListRestAdapterTest {

    @Mock
    private ShoppingListInputPort shoppingListInputPort;

    @Mock
    private ShoppingListRestMapper shoppingListRestMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ShoppingListRestAdapter adapter = new ShoppingListRestAdapter(shoppingListInputPort, shoppingListRestMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(adapter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/diets/{dietId}/shopping-lists/generate returns generated list")
    void generateWeeklyShoppingListReturnsGeneratedList() throws Exception {
        ShoppingList domain = domainList();
        ShoppingListResponse response = response(ShoppingListStatus.PENDING, false);

        when(shoppingListInputPort.generateWeeklyShoppingList(42L)).thenReturn(domain);
        when(shoppingListRestMapper.toResponse(domain)).thenReturn(response);

        mockMvc.perform(post("/api/v1/diets/{dietId}/shopping-lists/generate", 42L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].missingQuantity").value(1500.0));

        verify(shoppingListInputPort).generateWeeklyShoppingList(42L);
    }

    @Test
    @DisplayName("GET /api/v1/shopping-lists/current returns current list")
    void getCurrentShoppingListReturnsCurrentList() throws Exception {
        ShoppingList domain = domainList();
        ShoppingListResponse response = response(ShoppingListStatus.PENDING, false);

        when(shoppingListInputPort.getCurrentShoppingList()).thenReturn(domain);
        when(shoppingListRestMapper.toResponse(domain)).thenReturn(response);

        mockMvc.perform(get("/api/v1/shopping-lists/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(shoppingListInputPort).getCurrentShoppingList();
    }

    @Test
    @DisplayName("PATCH /api/v1/shopping-lists/items/{itemId}/purchase returns updated list")
    void purchaseShoppingListItemReturnsUpdatedList() throws Exception {
        ShoppingList domain = domainList();
        ShoppingListResponse response = response(ShoppingListStatus.COMPLETED, true);

        when(shoppingListInputPort.markItemAsPurchased(10L)).thenReturn(domain);
        when(shoppingListRestMapper.toResponse(domain)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/shopping-lists/items/{itemId}/purchase", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.items[0].purchased").value(true));

        verify(shoppingListInputPort).markItemAsPurchased(10L);
    }

    @Test
    @DisplayName("PATCH /api/v1/shopping-lists/purchase-all returns updated list")
    void purchaseAllShoppingListItemsReturnsUpdatedList() throws Exception {
        ShoppingList domain = domainList();
        ShoppingListResponse response = response(ShoppingListStatus.COMPLETED, true);

        when(shoppingListInputPort.markAllItemsAsPurchased()).thenReturn(domain);
        when(shoppingListRestMapper.toResponse(domain)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/shopping-lists/purchase-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.items[0].purchased").value(true));

        verify(shoppingListInputPort).markAllItemsAsPurchased();
    }

    @Test
    @DisplayName("POST /api/v1/diets/{dietId}/shopping-lists/generate maps missing diet to 404")
    void generateWeeklyShoppingListMapsMissingDietTo404() throws Exception {
        when(shoppingListInputPort.generateWeeklyShoppingList(99L))
                .thenThrow(new NoSuchElementException("Diet not found with id: 99"));

        mockMvc.perform(post("/api/v1/diets/{dietId}/shopping-lists/generate", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Diet not found with id: 99"));
    }

    @Test
    @DisplayName("GET /api/v1/shopping-lists/current maps missing list to 404")
    void getCurrentShoppingListMapsMissingListTo404() throws Exception {
        when(shoppingListInputPort.getCurrentShoppingList())
                .thenThrow(new ShoppingListNotFoundException("No shopping list found for current week"));

        mockMvc.perform(get("/api/v1/shopping-lists/current"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No shopping list found for current week"));
    }

    @Test
    @DisplayName("PATCH /api/v1/shopping-lists/items/{itemId}/purchase maps missing item to 404")
    void purchaseShoppingListItemMapsMissingItemTo404() throws Exception {
        when(shoppingListInputPort.markItemAsPurchased(99L))
                .thenThrow(new ShoppingListNotFoundException("ShoppingListItem not found with id: 99"));

        mockMvc.perform(patch("/api/v1/shopping-lists/items/{itemId}/purchase", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("ShoppingListItem not found with id: 99"));
    }

    @Test
    @DisplayName("PATCH /api/v1/shopping-lists/items/{itemId}/purchase maps already purchased item to 409")
    void purchaseShoppingListItemMapsAlreadyPurchasedTo409() throws Exception {
        when(shoppingListInputPort.markItemAsPurchased(10L))
                .thenThrow(new ShoppingListItemAlreadyPurchasedException(10L));

        mockMvc.perform(patch("/api/v1/shopping-lists/items/{itemId}/purchase", 10L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("ShoppingListItem with id 10 is already purchased."));
    }

    @Test
    @DisplayName("PATCH /api/v1/shopping-lists/purchase-all maps missing list to 404")
    void purchaseAllShoppingListItemsMapsMissingListTo404() throws Exception {
        when(shoppingListInputPort.markAllItemsAsPurchased())
                .thenThrow(new ShoppingListNotFoundException("No shopping list found for current week"));

        mockMvc.perform(patch("/api/v1/shopping-lists/purchase-all"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No shopping list found for current week"));
    }

    private ShoppingList domainList() {
        return ShoppingList.builder()
                .id(1L)
                .dietId(42L)
                .weekStart(LocalDate.now())
                .status(com.planing.diet_service.ShoppingList.domain.model.ShoppingListStatus.PENDING)
                .build();
    }

    private ShoppingListResponse response(ShoppingListStatus status, boolean purchased) {
        return new ShoppingListResponse()
                .id(1L)
                .weekStart(LocalDate.now())
                .status(status)
                .items(List.of(new ShoppingListItemResponse()
                        .id(10L)
                        .foodId(20L)
                        .foodName("Rice")
                        .requiredQuantity(2000.0)
                        .availableQuantity(500.0)
                        .missingQuantity(1500.0)
                        .unit(Unit.G)
                        .purchased(purchased)));
    }
}
