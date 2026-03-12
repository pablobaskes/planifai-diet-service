package com.planing.diet_service.Recipe.application.ports.input;

import com.planing.diet_service.Recipe.domain.model.Recipe;

import java.util.List;

public interface RecipeInputPort {

    List<Recipe> getAllRecipes();

    Recipe getRecipeById(Long id);

    Recipe createRecipe(Recipe recipe);

    Recipe updateRecipe(Long id, Recipe recipe);

    void deleteRecipe(Long id);
}

