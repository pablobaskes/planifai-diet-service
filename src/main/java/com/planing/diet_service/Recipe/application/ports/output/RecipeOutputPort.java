package com.planing.diet_service.Recipe.application.ports.output;

import com.planing.diet_service.Recipe.domain.model.Recipe;

import java.util.List;
import java.util.Optional;

public interface RecipeOutputPort {

    List<Recipe> findAll();

    Optional<Recipe> findById(Long id);

    Recipe save(Recipe recipe);

    void deleteById(Long id);

    boolean existsById(Long id);
}

