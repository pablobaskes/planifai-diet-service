package com.planing.diet_service.Recipe.infrastructure.output.jpa;

import com.planing.diet_service.Recipe.application.ports.output.RecipeOutputPort;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.RecipeEntity;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.mapper.RecipeJpaMapper;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.repository.RecipeJpaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class RecipeJpaAdapter implements RecipeOutputPort {

    private final RecipeJpaRepository recipeJpaRepository;
    private final RecipeJpaMapper recipeJpaMapper;

    @Override
    public List<Recipe> findAll() {
        return recipeJpaRepository.findAll()
                .stream()
                .map(recipeJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Recipe> findById(Long id) {
        return recipeJpaRepository.findById(id)
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
}

