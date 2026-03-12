package com.planing.diet_service.Recipe.application.usecase;

import com.planing.diet_service.Recipe.application.ports.input.RecipeInputPort;
import com.planing.diet_service.Recipe.application.ports.output.RecipeOutputPort;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
@Slf4j
public class RecipeUseCase implements RecipeInputPort {

    private final RecipeOutputPort recipeOutputPort;

    @Override
    public List<Recipe> getAllRecipes() {
        log.info("Getting all recipes");
        return recipeOutputPort.findAll();
    }

    @Override
    public Recipe getRecipeById(Long id) {
        log.info("Getting recipe by id: {}", id);
        return recipeOutputPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found with id: " + id));
    }

    @Override
    public Recipe createRecipe(Recipe recipe) {
        log.info("Creating recipe: {}", recipe.getName());
        return recipeOutputPort.save(recipe);
    }

    @Override
    public Recipe updateRecipe(Long id, Recipe recipe) {
        log.info("Updating recipe with id: {}", id);
        if (!recipeOutputPort.existsById(id)) {
            throw new NoSuchElementException("Recipe not found with id: " + id);
        }
        recipe.setId(id);
        return recipeOutputPort.save(recipe);
    }

    @Override
    public void deleteRecipe(Long id) {
        log.info("Deleting recipe with id: {}", id);
        if (!recipeOutputPort.existsById(id)) {
            throw new NoSuchElementException("Recipe not found with id: " + id);
        }
        recipeOutputPort.deleteById(id);
    }
}
