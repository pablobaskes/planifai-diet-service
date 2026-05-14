package com.planing.diet_service.Diet.infrastructure.output.jpa;


import com.planing.diet_service.Diet.application.ports.output.DietOutputPort;
import com.planing.diet_service.Diet.domain.model.Diet;

import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietEntity;
import com.planing.diet_service.Diet.infrastructure.output.jpa.mapper.DietJpaMapper;
import com.planing.diet_service.Diet.infrastructure.output.jpa.repository.DietJpaRepository;
import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.Diet.infrastructure.output.jpa.entity.DietDayEntity;
import com.planing.diet_service.Diet.infrastructure.output.jpa.repository.DietDayJpaRepository;
import com.planing.diet_service.FoodPortion.domain.model.FoodPortion;
import com.planing.diet_service.FoodPortion.infrastructure.output.jpa.entity.FoodPortionEmbedded;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.infrastructure.output.jpa.entity.MealSlotEntity;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.entity.RecipeEntity;
import com.planing.diet_service.Recipe.infrastructure.output.jpa.repository.RecipeJpaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class DietJpaAdapter implements DietOutputPort {

    private final DietJpaRepository dietJpaRepository;
    private final DietDayJpaRepository dietDayJpaRepository;
    private final RecipeJpaRepository recipeJpaRepository;
    private final DietJpaMapper dietJpaMapper;

    // ── Diet ──────────────────────────────

    @Override
    public List<Diet> findAllDiets() {
        return dietJpaRepository.findAll()
                .stream()
                .map(dietJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Diet> findDietById(Long id) {
        return dietJpaRepository.findById(id)
                .map(dietJpaMapper::toDomain);
    }

    @Override
    public Diet saveDiet(Diet diet) {
        DietEntity entity = dietJpaMapper.toEntity(diet);
        DietEntity saved = dietJpaRepository.save(entity);
        return dietJpaMapper.toDomain(saved);
    }

    @Override
    public void deleteDietById(Long id) {
        dietJpaRepository.deleteById(id);
    }

    @Override
    public boolean dietExistsById(Long id) {
        return dietJpaRepository.existsById(id);
    }

    @Override
    public List<Diet> findDietsByDateRange(LocalDate from, LocalDate to) {
        List<DietEntity> dietEntities = dietJpaRepository.findDietsBetween(from, to);

        return dietEntities.stream()
                .map(dietEntity -> {
                    List<DietDayEntity> daysWithSlots = dietDayJpaRepository
                            .findByDietIdWithMealSlotsAndRecipe(dietEntity.getId());
                    dietEntity.setDays(daysWithSlots);
                    return dietJpaMapper.toDomain(dietEntity);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Diet> findDietsByDateRangeForShoppingList(LocalDate from, LocalDate to) {
        List<Long> dietIds = dietJpaRepository.findDietIdsBetween(from, to);
        if (dietIds.size() != 1) {
            return dietIds.stream()
                    .map(id -> {
                        Diet diet = new Diet();
                        diet.setId(id);
                        return diet;
                    })
                    .toList();
        }

        DietEntity dietEntity = dietJpaRepository.findById(dietIds.get(0))
                .orElseThrow(() -> new IllegalStateException("Diet id disappeared while loading shopping list"));
        List<DietDayEntity> daysWithSlots = dietDayJpaRepository
                .findByDietIdWithMealSlotsAndRecipe(dietEntity.getId());

        List<Long> recipeIds = daysWithSlots.stream()
                .flatMap(day -> day.getMealSlots().stream())
                .map(MealSlotEntity::getRecipe)
                .filter(recipe -> recipe != null && recipe.getId() != null)
                .map(RecipeEntity::getId)
                .distinct()
                .toList();
        if (!recipeIds.isEmpty()) {
            recipeJpaRepository.findByIdInWithIngredients(recipeIds);
        }

        return List.of(toShoppingDiet(dietEntity, daysWithSlots));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Diet> findDietByIdForShoppingList(Long dietId) {
        return dietJpaRepository.findById(dietId)
                .map(dietEntity -> {
                    List<DietDayEntity> daysWithSlots = dietDayJpaRepository
                            .findByDietIdWithMealSlotsAndRecipe(dietEntity.getId());

                    List<Long> recipeIds = daysWithSlots.stream()
                            .flatMap(day -> day.getMealSlots().stream())
                            .map(MealSlotEntity::getRecipe)
                            .filter(recipe -> recipe != null && recipe.getId() != null)
                            .map(RecipeEntity::getId)
                            .distinct()
                            .toList();
                    if (!recipeIds.isEmpty()) {
                        recipeJpaRepository.findByIdInWithIngredients(recipeIds);
                    }

                    return toShoppingDiet(dietEntity, daysWithSlots);
                });
    }

    // ── DietDay ───────────────────────────

    @Override
    public List<DietDay> findDaysByDietId(Long dietId) {
        return dietDayJpaRepository.findByDietId(dietId)
                .stream()
                .map(dietJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<DietDay> findDietDayById(Long dayId) {
        return dietDayJpaRepository.findById(dayId)
                .map(dietJpaMapper::toDomain);
    }

    @Override
    public DietDay saveDietDay(DietDay dietDay) {
        DietDayEntity entity = dietJpaMapper.toEntity(dietDay);
        // Recuperamos la entidad Diet para mantener la FK correctamente
        if (dietDay.getDiet() != null && dietDay.getDiet().getId() != null) {
            dietJpaRepository.findById(dietDay.getDiet().getId())
                    .ifPresent(entity::setDiet);
        }
        DietDayEntity saved = dietDayJpaRepository.save(entity);
        return dietJpaMapper.toDomain(saved);
    }

    @Override
    public void deleteDietDayById(Long dayId) {
        dietDayJpaRepository.deleteById(dayId);
    }

    @Override
    public boolean dietDayExistsById(Long dayId) {
        return dietDayJpaRepository.existsById(dayId);
    }

    private Diet toShoppingDiet(DietEntity entity, List<DietDayEntity> dayEntities) {
        Diet diet = new Diet();
        diet.setId(entity.getId());
        diet.setName(entity.getName());
        diet.setDescription(entity.getDescription());
        diet.setCaloriesTarget(entity.getCaloriesTarget());
        diet.setInitDate(entity.getInitDate());
        diet.setEndDate(entity.getEndDate());

        List<DietDay> days = new ArrayList<>();
        for (DietDayEntity dayEntity : dayEntities) {
            DietDay day = new DietDay();
            day.setId(dayEntity.getId());
            day.setDate(dayEntity.getDate());
            day.setDiet(diet);

            List<MealSlot> slots = new ArrayList<>();
            for (MealSlotEntity slotEntity : dayEntity.getMealSlots()) {
                MealSlot slot = new MealSlot();
                slot.setId(slotEntity.getId());
                slot.setType(slotEntity.getType());
                slot.setRecipe(toShoppingRecipe(slotEntity.getRecipe()));
                slot.setDietDay(day);
                slots.add(slot);
            }
            day.setMealSlots(slots);
            days.add(day);
        }
        diet.setDays(days);
        return diet;
    }

    private Recipe toShoppingRecipe(RecipeEntity entity) {
        if (entity == null) {
            return null;
        }
        return Recipe.builder()
                .id(entity.getId())
                .name(entity.getName())
                .mealType(entity.getMealType())
                .nutritionSummary(entity.getNutritionSummary())
                .servings(entity.getServings())
                .ingredients(entity.getIngredients().stream()
                        .map(this::toFoodPortion)
                        .toList())
                .build();
    }

    private FoodPortion toFoodPortion(FoodPortionEmbedded embedded) {
        return FoodPortion.builder()
                .foodId(embedded.getFoodId())
                .quantity(embedded.getQuantity())
                .unit(embedded.getUnit())
                .weightPerUnit(embedded.getWeightPerUnit())
                .build();
    }

}

