package com.planing.diet_service.Food.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planing.diet.dto.FoodCategory;
import com.planing.diet.dto.FoodRequest;
import com.planing.diet.dto.FoodResponse;
import com.planing.diet_service.Food.application.ports.input.FoodInputPort;
import com.planing.diet_service.Food.domain.model.Food;
import com.planing.diet_service.Food.infrastructure.input.rest.mapper.FoodRestMapper;
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
class FoodRestAdapterTest {

    @Mock
    private FoodInputPort foodInputPort;

    @Mock
    private FoodRestMapper foodRestMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        FoodRestAdapter adapter = new FoodRestAdapter(foodInputPort, foodRestMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(adapter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /api/v1/foods forwards category and name filters")
    void getAllFoodsReturnsFilteredFoods() throws Exception {
        Food rice = domainFood(1L, "Brown rice", com.planing.diet_service.Food.domain.model.FoodCategory.GRAIN);
        FoodResponse response = response(1L, "Brown rice", FoodCategory.GRAIN);

        when(foodInputPort.getAllFoods(com.planing.diet_service.Food.domain.model.FoodCategory.GRAIN, "rice"))
                .thenReturn(List.of(rice));
        when(foodRestMapper.toResponseList(List.of(rice))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/foods")
                        .queryParam("category", "GRAIN")
                        .queryParam("name", "rice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Brown rice"))
                .andExpect(jsonPath("$[0].category").value("GRAIN"));

        verify(foodInputPort).getAllFoods(com.planing.diet_service.Food.domain.model.FoodCategory.GRAIN, "rice");
    }

    @Test
    @DisplayName("GET /api/v1/foods/{foodId} returns food")
    void getFoodByIdReturnsFood() throws Exception {
        Food food = domainFood(1L, "Chicken", com.planing.diet_service.Food.domain.model.FoodCategory.MEAT);

        when(foodInputPort.getFoodById(1L)).thenReturn(food);
        when(foodRestMapper.toResponse(food)).thenReturn(response(1L, "Chicken", FoodCategory.MEAT));

        mockMvc.perform(get("/api/v1/foods/{foodId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Chicken"))
                .andExpect(jsonPath("$.category").value("MEAT"));
    }

    @Test
    @DisplayName("GET /api/v1/foods/{foodId} maps missing food to 404")
    void getFoodByIdMapsMissingFoodTo404() throws Exception {
        when(foodInputPort.getFoodById(99L))
                .thenThrow(new NoSuchElementException("Food not found with id: 99"));

        mockMvc.perform(get("/api/v1/foods/{foodId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Food not found with id: 99"));
    }

    @Test
    @DisplayName("POST /api/v1/foods creates food")
    void createFoodCreatesFood() throws Exception {
        FoodRequest request = request("Spinach", FoodCategory.VEGETABLE);
        Food domainRequest = domainFood(null, "Spinach", com.planing.diet_service.Food.domain.model.FoodCategory.VEGETABLE);
        Food created = domainFood(1L, "Spinach", com.planing.diet_service.Food.domain.model.FoodCategory.VEGETABLE);

        when(foodRestMapper.toDomain(any(FoodRequest.class))).thenReturn(domainRequest);
        when(foodInputPort.createFood(domainRequest)).thenReturn(created);
        when(foodRestMapper.toResponse(created)).thenReturn(response(1L, "Spinach", FoodCategory.VEGETABLE));

        mockMvc.perform(post("/api/v1/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Spinach"));
    }

    @Test
    @DisplayName("PUT /api/v1/foods/{foodId} updates food")
    void updateFoodUpdatesFood() throws Exception {
        FoodRequest request = request("Updated rice", FoodCategory.GRAIN);
        Food domainRequest = domainFood(null, "Updated rice", com.planing.diet_service.Food.domain.model.FoodCategory.GRAIN);
        Food updated = domainFood(1L, "Updated rice", com.planing.diet_service.Food.domain.model.FoodCategory.GRAIN);

        when(foodRestMapper.toDomain(any(FoodRequest.class))).thenReturn(domainRequest);
        when(foodInputPort.updateFood(1L, domainRequest)).thenReturn(updated);
        when(foodRestMapper.toResponse(updated)).thenReturn(response(1L, "Updated rice", FoodCategory.GRAIN));

        mockMvc.perform(put("/api/v1/foods/{foodId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated rice"));
    }

    @Test
    @DisplayName("DELETE /api/v1/foods/{foodId} deletes food")
    void deleteFoodDeletesFood() throws Exception {
        mockMvc.perform(delete("/api/v1/foods/{foodId}", 1L))
                .andExpect(status().isNoContent());

        verify(foodInputPort).deleteFood(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/foods/{foodId} maps missing food to 404")
    void deleteFoodMapsMissingFoodTo404() throws Exception {
        org.mockito.Mockito.doThrow(new NoSuchElementException("Food not found with id: 99"))
                .when(foodInputPort).deleteFood(99L);

        mockMvc.perform(delete("/api/v1/foods/{foodId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Food not found with id: 99"));
    }

    private FoodRequest request(String name, FoodCategory category) {
        return new FoodRequest()
                .name(name)
                .category(category)
                .caloriesPer100g(100.0)
                .proteinPer100g(10.0)
                .carbsPer100g(12.0)
                .fatPer100g(2.0);
    }

    private FoodResponse response(Long id, String name, FoodCategory category) {
        return new FoodResponse()
                .id(id)
                .name(name)
                .category(category)
                .caloriesPer100g(100.0)
                .proteinPer100g(10.0)
                .carbsPer100g(12.0)
                .fatPer100g(2.0);
    }

    private Food domainFood(Long id, String name, com.planing.diet_service.Food.domain.model.FoodCategory category) {
        return Food.builder()
                .id(id)
                .name(name)
                .category(category)
                .caloriesPer100g(100.0)
                .proteinPer100g(10.0)
                .carbsPer100g(12.0)
                .fatPer100g(2.0)
                .build();
    }
}
