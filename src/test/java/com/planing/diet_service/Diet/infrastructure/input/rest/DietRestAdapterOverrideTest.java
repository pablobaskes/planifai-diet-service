package com.planing.diet_service.Diet.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planing.diet.dto.MealSlotDetailResponse;
import com.planing.diet.dto.MealSlotRecipeOverrideRequest;
import com.planing.diet.dto.MealType;
import com.planing.diet.dto.RecipeResponse;
import com.planing.diet_service.Diet.application.ports.input.DietInputPort;
import com.planing.diet_service.Diet.infrastructure.input.rest.mapper.DietRestMapper;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.Recipe.domain.model.Recipe;
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

import java.util.NoSuchElementException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DietRestAdapterOverrideTest {

    @Mock
    private DietInputPort dietInputPort;

    @Mock
    private DietRestMapper dietRestMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        DietRestAdapter adapter = new DietRestAdapter(dietInputPort, dietRestMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(adapter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("PATCH /api/v1/meal-slots/{slotId}/recipe updates slot recipe")
    void overrideMealSlotRecipeUpdatesSlotRecipe() throws Exception {
        MealSlot updatedSlot = mealSlot(10L, 2L, "Lunch B",
                com.planing.diet_service.MealSlot.domain.utils.MealType.LUNCH);

        when(dietInputPort.overrideMealSlotRecipe(10L, 2L)).thenReturn(updatedSlot);
        when(dietRestMapper.toDetailResponse(updatedSlot))
                .thenReturn(response(10L, 2L, "Lunch B", MealType.LUNCH));

        mockMvc.perform(patch("/api/v1/meal-slots/{slotId}/recipe", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MealSlotRecipeOverrideRequest(2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.type").value("LUNCH"))
                .andExpect(jsonPath("$.recipe.id").value(2))
                .andExpect(jsonPath("$.recipe.name").value("Lunch B"));

        verify(dietInputPort).overrideMealSlotRecipe(10L, 2L);
    }

    @Test
    @DisplayName("PATCH maps missing slot to 404")
    void overrideMealSlotRecipeMapsMissingSlotTo404() throws Exception {
        when(dietInputPort.overrideMealSlotRecipe(99L, 2L))
                .thenThrow(new NoSuchElementException("MealSlot not found with id: 99"));

        mockMvc.perform(patch("/api/v1/meal-slots/{slotId}/recipe", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MealSlotRecipeOverrideRequest(2L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("MealSlot not found with id: 99"));
    }

    @Test
    @DisplayName("PATCH maps missing recipe to 404")
    void overrideMealSlotRecipeMapsMissingRecipeTo404() throws Exception {
        when(dietInputPort.overrideMealSlotRecipe(10L, 99L))
                .thenThrow(new NoSuchElementException("Recipe not found with id: 99"));

        mockMvc.perform(patch("/api/v1/meal-slots/{slotId}/recipe", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MealSlotRecipeOverrideRequest(99L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Recipe not found with id: 99"));
    }

    @Test
    @DisplayName("PATCH maps MealType incompatibility to 400")
    void overrideMealSlotRecipeMapsMealTypeIncompatibilityTo400() throws Exception {
        when(dietInputPort.overrideMealSlotRecipe(10L, 2L))
                .thenThrow(new IllegalArgumentException(
                        "Recipe mealType DINNER is not compatible with meal slot type BREAKFAST."));

        mockMvc.perform(patch("/api/v1/meal-slots/{slotId}/recipe", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MealSlotRecipeOverrideRequest(2L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Recipe mealType DINNER is not compatible with meal slot type BREAKFAST."));
    }

    @Test
    @DisplayName("PATCH maps missing request body to 400")
    void overrideMealSlotRecipeMapsMissingBodyTo400() throws Exception {
        mockMvc.perform(patch("/api/v1/meal-slots/{slotId}/recipe", 10L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private MealSlot mealSlot(
            Long slotId,
            Long recipeId,
            String recipeName,
            com.planing.diet_service.MealSlot.domain.utils.MealType type) {

        Recipe recipe = new Recipe();
        recipe.setId(recipeId);
        recipe.setName(recipeName);
        recipe.setMealType(type);

        MealSlot mealSlot = new MealSlot();
        mealSlot.setId(slotId);
        mealSlot.setType(type);
        mealSlot.setRecipe(recipe);
        return mealSlot;
    }

    private MealSlotDetailResponse response(Long slotId, Long recipeId, String recipeName, MealType type) {
        return new MealSlotDetailResponse()
                .id(slotId)
                .type(type)
                .recipe(new RecipeResponse()
                        .id(recipeId)
                        .name(recipeName));
    }
}
