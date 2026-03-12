package com.planing.diet_service.Recipe.infrastructure.output.jpa.mapper;

import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.FoodPortion.domain.model.Unit;
import com.planing.diet_service.FoodPortion.infrastructure.output.jpa.entity.FoodPortionEmbedded;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.RecipeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecipeJpaMapper {

    Recipe toDomain(RecipeEntity entity);

    RecipeEntity toEntity(Recipe recipe);

    @Mapping(target = "unit", source = "unit")
    FoodPortion toDomain(FoodPortionEmbedded embedded);

    @Mapping(target = "unit", source = "unit")
    FoodPortionEmbedded toEmbedded(FoodPortion portion);

    default Unit toUnitDomain(Unit unit) {
        if (unit == null) return null;
        return Unit.valueOf(unit.name());
    }
}