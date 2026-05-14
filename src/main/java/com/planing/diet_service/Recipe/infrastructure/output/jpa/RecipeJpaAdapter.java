package com.planing.diet_service.Recipe.infrastructure.output.jpa;

import com.planing.diet_service.Recipe.application.ports.output.RecipeOutputPort;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.RecipeEntity;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.mapper.RecipeJpaMapper;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.repository.RecipeJpaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class RecipeJpaAdapter implements RecipeOutputPort {

    private final RecipeJpaRepository recipeJpaRepository;
    private final RecipeJpaMapper recipeJpaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Recipe> findAll() {
        List<RecipeEntity> recipes = recipeJpaRepository.findAllWithIngredients();
        loadTags(recipes);

        return recipes
                .stream()
                .map(recipeJpaMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Recipe> findById(Long id) {
        Optional<RecipeEntity> recipe = recipeJpaRepository.findByIdWithIngredients(id);
        recipe.ifPresent(entity -> recipeJpaRepository.findByIdInWithTags(List.of(entity.getId())));

        return recipe
                .map(recipeJpaMapper::toDomain);
    }

    @Override
    public Recipe save(Recipe recipe) {
        RecipeEntity entity = recipeJpaMapper.toEntity(recipe);
        RecipeEntity saved = recipeJpaRepository.save(entity);
        return recipeJpaMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        recipeJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return recipeJpaRepository.existsById(id);
    }

    private void loadTags(List<RecipeEntity> recipes) {
        List<Long> recipeIds = recipes.stream()
                .map(RecipeEntity::getId)
                .toList();
        if (!recipeIds.isEmpty()) {
            recipeJpaRepository.findByIdInWithTags(recipeIds);
        }
    }
}

