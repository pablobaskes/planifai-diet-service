package com.planing.diet_service.InventoryItem.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planing.diet.dto.FoodPortionDto;
import com.planing.diet.dto.InventoryItemRequest;
import com.planing.diet.dto.InventoryItemResponse;
import com.planing.diet.dto.MeasureUnit;
import com.planing.diet.dto.StorageLocation;
import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.FoodPortion.domain.model.Unit;
import com.planing.diet_service.InventoryItem.application.ports.input.InventoryInputPort;
import com.planing.diet_service.InventoryItem.domain.model.InventoryItem;
import com.planing.diet_service.InventoryItem.infrastructure.input.rest.mapper.InventoryRestMapper;
import com.planing.diet_service.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventoryRestAdapterTest {

    @Mock
    private InventoryInputPort inventoryInputPort;

    @Mock
    private InventoryRestMapper inventoryRestMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        InventoryRestAdapter adapter = new InventoryRestAdapter(inventoryInputPort, inventoryRestMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(adapter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /api/v1/inventory returns filtered inventory items")
    void getAllInventoryItemsReturnsFilteredInventoryItems() throws Exception {
        InventoryItem item = domainItem(1L, 10L, 2.0, Unit.KG,
                com.planing.diet_service.InventoryItem.domain.model.StorageLocation.PANTRY);
        InventoryItemResponse response = response(1L, 10L, 2.0, MeasureUnit.KG, StorageLocation.PANTRY);

        when(inventoryRestMapper.toDomain(StorageLocation.PANTRY))
                .thenReturn(com.planing.diet_service.InventoryItem.domain.model.StorageLocation.PANTRY);
        when(inventoryInputPort.getAllInventoryItems(
                com.planing.diet_service.InventoryItem.domain.model.StorageLocation.PANTRY))
                .thenReturn(List.of(item));
        when(inventoryRestMapper.toResponseList(List.of(item))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/inventory")
                        .queryParam("location", "PANTRY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].portion.foodId").value(10))
                .andExpect(jsonPath("$[0].location").value("PANTRY"));

        verify(inventoryInputPort).getAllInventoryItems(
                com.planing.diet_service.InventoryItem.domain.model.StorageLocation.PANTRY);
    }

    @Test
    @DisplayName("GET /api/v1/inventory/{itemId} returns inventory item")
    void getInventoryItemByIdReturnsInventoryItem() throws Exception {
        InventoryItem item = domainItem(1L, 10L, 2.0, Unit.KG,
                com.planing.diet_service.InventoryItem.domain.model.StorageLocation.PANTRY);
        when(inventoryInputPort.getInventoryItemById(1L)).thenReturn(item);
        when(inventoryRestMapper.toResponse(item))
                .thenReturn(response(1L, 10L, 2.0, MeasureUnit.KG, StorageLocation.PANTRY));

        mockMvc.perform(get("/api/v1/inventory/{itemId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.portion.quantity").value(2.0))
                .andExpect(jsonPath("$.location").value("PANTRY"));
    }

    @Test
    @DisplayName("GET /api/v1/inventory/{itemId} maps missing item to 404")
    void getInventoryItemByIdMapsMissingItemTo404() throws Exception {
        when(inventoryInputPort.getInventoryItemById(99L))
                .thenThrow(new NoSuchElementException("Inventory item not found with id: 99"));

        mockMvc.perform(get("/api/v1/inventory/{itemId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Inventory item not found with id: 99"));
    }

    @Test
    @DisplayName("POST /api/v1/inventory creates inventory item")
    void createInventoryItemCreatesInventoryItem() throws Exception {
        InventoryItemRequest request = request(10L, 2.0, MeasureUnit.KG, StorageLocation.PANTRY);
        InventoryItem domainRequest = domainItem(null, 10L, 2.0, Unit.KG,
                com.planing.diet_service.InventoryItem.domain.model.StorageLocation.PANTRY);
        InventoryItem created = domainItem(1L, 10L, 2.0, Unit.KG,
                com.planing.diet_service.InventoryItem.domain.model.StorageLocation.PANTRY);

        when(inventoryRestMapper.toDomain(any(InventoryItemRequest.class))).thenReturn(domainRequest);
        when(inventoryInputPort.createInventoryItem(domainRequest)).thenReturn(created);
        when(inventoryRestMapper.toResponse(created))
                .thenReturn(response(1L, 10L, 2.0, MeasureUnit.KG, StorageLocation.PANTRY));

        mockMvc.perform(post("/api/v1/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.portion.foodId").value(10))
                .andExpect(jsonPath("$.location").value("PANTRY"));
    }

    @Test
    @DisplayName("POST /api/v1/inventory maps invalid item to 400")
    void createInventoryItemMapsInvalidItemTo400() throws Exception {
        InventoryItemRequest request = request(10L, 0.0, MeasureUnit.KG, StorageLocation.PANTRY);
        InventoryItem domainRequest = domainItem(null, 10L, 0.0, Unit.KG,
                com.planing.diet_service.InventoryItem.domain.model.StorageLocation.PANTRY);

        when(inventoryRestMapper.toDomain(any(InventoryItemRequest.class))).thenReturn(domainRequest);
        when(inventoryInputPort.createInventoryItem(domainRequest))
                .thenThrow(new IllegalArgumentException("Inventory item quantity must be positive."));

        mockMvc.perform(post("/api/v1/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Inventory item quantity must be positive."));
    }

    @Test
    @DisplayName("PUT /api/v1/inventory/{itemId} updates inventory item")
    void updateInventoryItemUpdatesInventoryItem() throws Exception {
        InventoryItemRequest request = request(10L, 3.0, MeasureUnit.KG, StorageLocation.FRIDGE);
        InventoryItem domainRequest = domainItem(null, 10L, 3.0, Unit.KG,
                com.planing.diet_service.InventoryItem.domain.model.StorageLocation.FRIDGE);
        InventoryItem updated = domainItem(1L, 10L, 3.0, Unit.KG,
                com.planing.diet_service.InventoryItem.domain.model.StorageLocation.FRIDGE);

        when(inventoryRestMapper.toDomain(any(InventoryItemRequest.class))).thenReturn(domainRequest);
        when(inventoryInputPort.updateInventoryItem(1L, domainRequest)).thenReturn(updated);
        when(inventoryRestMapper.toResponse(updated))
                .thenReturn(response(1L, 10L, 3.0, MeasureUnit.KG, StorageLocation.FRIDGE));

        mockMvc.perform(put("/api/v1/inventory/{itemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.portion.quantity").value(3.0))
                .andExpect(jsonPath("$.location").value("FRIDGE"));
    }

    @Test
    @DisplayName("DELETE /api/v1/inventory/{itemId} deletes inventory item")
    void deleteInventoryItemDeletesInventoryItem() throws Exception {
        mockMvc.perform(delete("/api/v1/inventory/{itemId}", 1L))
                .andExpect(status().isNoContent());

        verify(inventoryInputPort).deleteInventoryItem(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/inventory/{itemId} maps missing item to 404")
    void deleteInventoryItemMapsMissingItemTo404() throws Exception {
        org.mockito.Mockito.doThrow(new NoSuchElementException("Inventory item not found with id: 99"))
                .when(inventoryInputPort).deleteInventoryItem(99L);

        mockMvc.perform(delete("/api/v1/inventory/{itemId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Inventory item not found with id: 99"));
    }

    private InventoryItemRequest request(Long foodId, Double quantity, MeasureUnit unit, StorageLocation location) {
        return new InventoryItemRequest()
                .portion(new FoodPortionDto()
                        .foodId(foodId)
                        .quantity(quantity)
                        .unit(unit))
                .location(location);
    }

    private InventoryItemResponse response(Long id, Long foodId, Double quantity, MeasureUnit unit, StorageLocation location) {
        return new InventoryItemResponse()
                .id(id)
                .portion(new FoodPortionDto()
                        .foodId(foodId)
                        .quantity(quantity)
                        .unit(unit))
                .location(location);
    }

    private InventoryItem domainItem(
            Long id,
            Long foodId,
            Double quantity,
            Unit unit,
            com.planing.diet_service.InventoryItem.domain.model.StorageLocation location) {

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
