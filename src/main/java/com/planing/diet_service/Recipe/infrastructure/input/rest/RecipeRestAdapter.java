package com.planing.diet_service.Recipe.infrastructure.input.rest;

import com.planing.diet.api.RecipesApi;
import com.planing.diet.dto.RecipeRequest;
import com.planing.diet.dto.RecipeResponse;
import com.planing.diet_service.Recipe.application.ports.input.RecipeInputPort;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import com.planing.diet_service.Recipe.infrastructure.input.rest.mapper.RecipeRestMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class RecipeRestAdapter implements RecipesApi {

    private final RecipeInputPort recipeInputPort;
    private final RecipeRestMapper recipeRestMapper;

    @Override
    public ResponseEntity<List<RecipeResponse>> getAllRecipes() {
        List<Recipe> recipes = recipeInputPort.getAllRecipes();
        return ResponseEntity.ok(recipeRestMapper.toResponseList(recipes));
    }

    @Override
    public ResponseEntity<RecipeResponse> getRecipeById(Long recipeId) {
        Recipe recipe = recipeInputPort.getRecipeById(recipeId);
        return ResponseEntity.ok(recipeRestMapper.toResponse(recipe));
    }

    @Override
    public ResponseEntity<RecipeResponse> createRecipe(RecipeRequest recipeRequest) {
        Recipe recipe = recipeInputPort.createRecipe(recipeRestMapper.toDomain(recipeRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(recipeRestMapper.toResponse(recipe));
    }

    @Override
    public ResponseEntity<RecipeResponse> updateRecipe(Long recipeId, RecipeRequest recipeRequest) {
        Recipe recipe = recipeInputPort.updateRecipe(recipeId, recipeRestMapper.toDomain(recipeRequest));
        return ResponseEntity.ok(recipeRestMapper.toResponse(recipe));
    }

    @Override
    public ResponseEntity<Void> deleteRecipe(Long recipeId) {
        recipeInputPort.deleteRecipe(recipeId);
        return ResponseEntity.noContent().build();
    }
}

