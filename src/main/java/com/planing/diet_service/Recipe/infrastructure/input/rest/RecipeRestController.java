package com.planing.diet_service.Recipe.infrastructure.input.rest;

import com.planing.diet.api.RecipesApi;
import com.planing.diet.dto.RecipeRequest;
import com.planing.diet.dto.RecipeResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class RecipeRestController implements RecipesApi {
    @Override
    public ResponseEntity<RecipeResponse> createRecipe(RecipeRequest recipeRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteRecipe(Long recipeId) {
        return null;
    }

    @Override
    public ResponseEntity<List<RecipeResponse>> getAllRecipes() {
        return null;
    }

    @Override
    public ResponseEntity<RecipeResponse> getRecipeById(Long recipeId) {
        return null;
    }

    @Override
    public ResponseEntity<RecipeResponse> updateRecipe(Long recipeId, RecipeRequest recipeRequest) {
        return null;
    }
}
