package com.planing.diet_service.Recipe.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planing.diet.dto.FoodPortionDto;
import com.planing.diet.dto.MeasureUnit;
import com.planing.diet.dto.NutritionSummaryDto;
import com.planing.diet.dto.RecipeRequest;
import com.planing.diet.dto.RecipeResponse;
import com.planing.diet_service.Food.domain.exception.FoodNotFoundException;
import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.FoodPortion.domain.model.Unit;
import com.planing.diet_service.Recipe.application.ports.input.RecipeInputPort;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import com.planing.diet_service.Recipe.infrastructure.input.rest.mapper.RecipeRestMapper;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.NutritionSummaryEmbedded;
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

import java.math.BigDecimal;
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
class RecipeRestAdapterTest {

    @Mock
    private RecipeInputPort recipeInputPort;

    @Mock
    private RecipeRestMapper recipeRestMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        RecipeRestAdapter adapter = new RecipeRestAdapter(recipeInputPort, recipeRestMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(adapter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /api/v1/recipes returns recipes with nutrition summary")
    void getAllRecipesReturnsRecipesWithNutritionSummary() throws Exception {
        Recipe recipe = domainRecipe(1L, "Chicken rice", 695.0);
        RecipeResponse response = response(1L, "Chicken rice", 695.0);

        when(recipeInputPort.getAllRecipes()).thenReturn(List.of(recipe));
        when(recipeRestMapper.toResponseList(List.of(recipe))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nutritionSummary.totalCalories").value(695.0));
    }

    @Test
    @DisplayName("GET /api/v1/recipes/{recipeId} returns recipe")
    void getRecipeByIdReturnsRecipe() throws Exception {
        Recipe recipe = domainRecipe(1L, "Chicken rice", 695.0);
        when(recipeInputPort.getRecipeById(1L)).thenReturn(recipe);
        when(recipeRestMapper.toResponse(recipe)).thenReturn(response(1L, "Chicken rice", 695.0));

        mockMvc.perform(get("/api/v1/recipes/{recipeId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nutritionSummary.totalProtein").value(69.0));
    }

    @Test
    @DisplayName("GET /api/v1/recipes/{recipeId} maps missing recipe to 404")
    void getRecipeByIdMapsMissingRecipeTo404() throws Exception {
        when(recipeInputPort.getRecipeById(99L))
                .thenThrow(new NoSuchElementException("Recipe not found with id: 99"));

        mockMvc.perform(get("/api/v1/recipes/{recipeId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Recipe not found with id: 99"));
    }

    @Test
    @DisplayName("POST /api/v1/recipes creates recipe with calculated nutrition")
    void createRecipeCreatesRecipeWithNutritionSummary() throws Exception {
        RecipeRequest request = request("Chicken rice");
        Recipe domainRequest = domainRecipe(null, "Chicken rice", null);
        Recipe created = domainRecipe(1L, "Chicken rice", 695.0);

        when(recipeRestMapper.toDomain(any(RecipeRequest.class))).thenReturn(domainRequest);
        when(recipeInputPort.createRecipe(domainRequest)).thenReturn(created);
        when(recipeRestMapper.toResponse(created)).thenReturn(response(1L, "Chicken rice", 695.0));

        mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nutritionSummary.totalCalories").value(695.0))
                .andExpect(jsonPath("$.nutritionSummary.totalFat").value(7.9));
    }

    @Test
    @DisplayName("POST /api/v1/recipes maps missing ingredient food to 422")
    void createRecipeMapsMissingIngredientFoodTo422() throws Exception {
        RecipeRequest request = request("Missing food");
        Recipe domainRequest = domainRecipe(null, "Missing food", null);

        when(recipeRestMapper.toDomain(any(RecipeRequest.class))).thenReturn(domainRequest);
        when(recipeInputPort.createRecipe(domainRequest)).thenThrow(new FoodNotFoundException(99L));

        mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.missingFoodId").value(99));
    }

    @Test
    @DisplayName("PUT /api/v1/recipes/{recipeId} updates recipe and nutrition")
    void updateRecipeUpdatesRecipeWithNutritionSummary() throws Exception {
        RecipeRequest request = request("Updated chicken rice");
        Recipe domainRequest = domainRecipe(null, "Updated chicken rice", null);
        Recipe updated = domainRecipe(1L, "Updated chicken rice", 347.5);

        when(recipeRestMapper.toDomain(any(RecipeRequest.class))).thenReturn(domainRequest);
        when(recipeInputPort.updateRecipe(1L, domainRequest)).thenReturn(updated);
        when(recipeRestMapper.toResponse(updated)).thenReturn(response(1L, "Updated chicken rice", 347.5));

        mockMvc.perform(put("/api/v1/recipes/{recipeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nutritionSummary.totalCalories").value(347.5));

        verify(recipeInputPort).updateRecipe(1L, domainRequest);
    }

    @Test
    @DisplayName("DELETE /api/v1/recipes/{recipeId} deletes recipe")
    void deleteRecipeDeletesRecipe() throws Exception {
        mockMvc.perform(delete("/api/v1/recipes/{recipeId}", 1L))
                .andExpect(status().isNoContent());

        verify(recipeInputPort).deleteRecipe(1L);
    }

    @Test
    @DisplayName("DELETE /api/v1/recipes/{recipeId} maps missing recipe to 404")
    void deleteRecipeMapsMissingRecipeTo404() throws Exception {
        org.mockito.Mockito.doThrow(new NoSuchElementException("Recipe not found with id: 99"))
                .when(recipeInputPort).deleteRecipe(99L);

        mockMvc.perform(delete("/api/v1/recipes/{recipeId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Recipe not found with id: 99"));
    }

    private RecipeRequest request(String name) {
        return new RecipeRequest()
                .name(name)
                .ingredients(List.of(new FoodPortionDto()
                        .foodId(1L)
                        .quantity(200.0)
                        .unit(MeasureUnit.G)))
                .servings(BigDecimal.ONE);
    }

    private RecipeResponse response(Long id, String name, Double calories) {
        return new RecipeResponse()
                .id(id)
                .name(name)
                .ingredients(List.of(new FoodPortionDto()
                        .foodId(1L)
                        .quantity(200.0)
                        .unit(MeasureUnit.G)))
                .nutritionSummary(new NutritionSummaryDto()
                        .totalCalories(calories)
                        .totalProtein(69.0)
                        .totalCarbs(80.0)
                        .totalFat(7.9))
                .servings(BigDecimal.ONE);
    }

    private Recipe domainRecipe(Long id, String name, Double calories) {
        Recipe recipe = Recipe.builder()
                .id(id)
                .name(name)
                .ingredients(List.of(FoodPortion.builder()
                        .foodId(1L)
                        .quantity(200.0)
                        .unit(Unit.G)
                        .build()))
                .servings(1)
                .build();

        if (calories != null) {
            NutritionSummaryEmbedded nutrition = new NutritionSummaryEmbedded();
            nutrition.setTotalCalories(calories);
            nutrition.setTotalProtein(69.0);
            nutrition.setTotalCarbs(80.0);
            nutrition.setTotalFat(7.9);
            recipe.setNutritionSummary(nutrition);
        }
        return recipe;
    }
}
