package com.planing.diet_service.Diet.application.usecase;

import com.planing.diet_service.Diet.application.ports.input.DietInputPort;
import com.planing.diet_service.Diet.application.ports.output.DietOutputPort;
import com.planing.diet_service.Diet.domain.model.Diet;
import com.planing.diet_service.Diet.domain.model.DietDay;
import com.planing.diet_service.MealSlot.application.ports.output.MealSlotJpaOutputPort;
import com.planing.diet_service.MealSlot.domain.model.MealSlot;
import com.planing.diet_service.MealSlot.domain.utils.MealType;
import com.planing.diet_service.Recipe.application.ports.output.RecipeOutputPort;
import com.planing.diet_service.Recipe.domain.model.Recipe;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

import static com.planing.diet_service.MealSlot.domain.utils.DietConstants.*;

@Service
@AllArgsConstructor
@Slf4j
public class DietUseCase implements DietInputPort {

    private final DietOutputPort dietOutputPort;
    private final MealSlotJpaOutputPort mealSlotJpaOutputPort;
    private final RecipeOutputPort recipeOutputPort;

    // ── Diet ──────────────────────────────

    @Override
    public List<Diet> getAllDiets() {
        log.info("Getting all diets");
        return dietOutputPort.findAllDiets();
    }

    @Override
    public Diet getDietById(Long id) {
        log.info("Getting diet by id: {}", id);
        return dietOutputPort.findDietById(id)
                .orElseThrow(() -> new NoSuchElementException("Diet not found with id: " + id));
    }

    @Override
    public Diet createDiet(Diet diet) {
        log.info("Creating diet: {} from {} to {}", diet.getName(), diet.getInitDate(), diet.getEndDate());
        diet.validate();
        Diet savedDiet = dietOutputPort.saveDiet(diet);
        savedDiet.setDays(generateDietDays(savedDiet));
        return savedDiet;
    }

    @Override
    public Diet updateDiet(Long id, Diet diet) {
        log.info("Updating diet with id: {}", id);
        if (!dietOutputPort.dietExistsById(id)) {
            throw new NoSuchElementException("Diet not found with id: " + id);
        }
        diet.setId(id);
        return dietOutputPort.saveDiet(diet);
    }

    @Override
    public void deleteDiet(Long id) {
        log.info("Deleting diet with id: {}", id);
        if (!dietOutputPort.dietExistsById(id)) {
            throw new NoSuchElementException("Diet not found with id: " + id);
        }
        dietOutputPort.deleteDietById(id);
    }

    @Override
    public List<Diet> getDietsByDateRange(LocalDate from, LocalDate to) {
        log.info("Getting diets between {} and {}", from, to);
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both 'from' and 'to' dates are required.");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("'to' date cannot be before 'from' date.");
        }
        return dietOutputPort.findDietsByDateRange(from, to);
    }

    // ── DietDay ───────────────────────────

    @Override
    public List<DietDay> getDaysByDiet(Long dietId) {
        log.info("Getting days for diet id: {}", dietId);
        if (!dietOutputPort.dietExistsById(dietId)) {
            throw new NoSuchElementException("Diet not found with id: " + dietId);
        }
        return dietOutputPort.findDaysByDietId(dietId);
    }

    @Override
    public DietDay getDietDayById(Long dietId, Long dayId) {
        log.info("Getting diet day {} for diet {}", dayId, dietId);
        if (!dietOutputPort.dietExistsById(dietId)) {
            throw new NoSuchElementException("Diet not found with id: " + dietId);
        }
        return dietOutputPort.findDietDayById(dayId)
                .orElseThrow(() -> new NoSuchElementException("DietDay not found with id: " + dayId));
    }

    @Override
    public DietDay createDietDay(Long dietId, DietDay dietDay) {
        log.info("Creating diet day for diet id: {}", dietId);
        dietDay.setDiet(getDietById(dietId));
        return dietOutputPort.saveDietDay(dietDay);
    }

    @Override
    public DietDay updateDietDay(Long dietId, Long dayId, DietDay dietDay) {
        log.info("Updating diet day {} for diet {}", dayId, dietId);
        if (!dietOutputPort.dietExistsById(dietId)) {
            throw new NoSuchElementException("Diet not found with id: " + dietId);
        }
        if (!dietOutputPort.dietDayExistsById(dayId)) {
            throw new NoSuchElementException("DietDay not found with id: " + dayId);
        }
        dietDay.setId(dayId);
        dietDay.setDiet(getDietById(dietId));
        return dietOutputPort.saveDietDay(dietDay);
    }

    @Override
    public void deleteDietDay(Long dietId, Long dayId) {
        log.info("Deleting diet day {} for diet {}", dayId, dietId);
        if (!dietOutputPort.dietExistsById(dietId)) {
            throw new NoSuchElementException("Diet not found with id: " + dietId);
        }
        if (!dietOutputPort.dietDayExistsById(dayId)) {
            throw new NoSuchElementException("DietDay not found with id: " + dayId);
        }
        dietOutputPort.deleteDietDayById(dayId);
    }

    @Override
    public MealSlot overrideMealSlotRecipe(Long slotId, Long recipeId) {
        log.info("Overriding meal slot {} with recipe {}", slotId, recipeId);
        if (slotId == null || slotId <= 0) {
            throw new IllegalArgumentException("Meal slot id must be positive.");
        }
        if (recipeId == null || recipeId <= 0) {
            throw new IllegalArgumentException("Recipe id must be positive.");
        }

        MealSlot mealSlot = mealSlotJpaOutputPort.findMealSlotById(slotId)
                .orElseThrow(() -> new NoSuchElementException("MealSlot not found with id: " + slotId));
        Recipe recipe = recipeOutputPort.findById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found with id: " + recipeId));

        validateMealTypeCompatibility(mealSlot, recipe);

        mealSlot.setRecipe(recipe);
        return mealSlotJpaOutputPort.saveMealSlot(mealSlot);
    }

    private void validateMealTypeCompatibility(MealSlot mealSlot, Recipe recipe) {
        if (mealSlot.getType() != null
                && recipe.getMealType() != null
                && mealSlot.getType() != recipe.getMealType()) {
            throw new IllegalArgumentException(
                    "Recipe mealType " + recipe.getMealType()
                            + " is not compatible with meal slot type " + mealSlot.getType() + ".");
        }
    }

    // ─────────────────────────────────────────────────────────
    // Generación de días y asignación de MealSlots
    // ─────────────────────────────────────────────────────────

    private List<DietDay> generateDietDays(Diet diet) {
        List<DietDay> days = new ArrayList<>();
        LocalDate current = diet.getInitDate();

        // Sets de recetas ya usadas por MealType — se mantienen durante
        // toda la generación de la dieta para evitar repeticiones
        Map<MealType, Set<Long>> usedRecipeIds = new EnumMap<>(MealType.class);
        usedRecipeIds.put(MealType.BREAKFAST, new HashSet<>());
        usedRecipeIds.put(MealType.LUNCH,     new HashSet<>());
        usedRecipeIds.put(MealType.DINNER,    new HashSet<>());

        // Obtener candidatos una sola vez — evita N llamadas a BD por día
        List<Recipe> breakfastCandidates = mealSlotJpaOutputPort.findRecipesByMealType(MealType.BREAKFAST);
        List<Recipe> lunchCandidates     = mealSlotJpaOutputPort.findRecipesByMealType(MealType.LUNCH);
        List<Recipe> dinnerCandidates    = mealSlotJpaOutputPort.findRecipesByMealType(MealType.DINNER);

        while (!current.isAfter(diet.getEndDate())) {
            days.add(buildAndPersistDietDay(
                    current, diet,
                    breakfastCandidates, lunchCandidates, dinnerCandidates,
                    usedRecipeIds));
            current = current.plusDays(1);
        }

        log.info("Generated {} diet days for diet '{}'", days.size(), diet.getName());
        return days;
    }

    private DietDay buildAndPersistDietDay(
            LocalDate date,
            Diet diet,
            List<Recipe> breakfastCandidates,
            List<Recipe> lunchCandidates,
            List<Recipe> dinnerCandidates,
            Map<MealType, Set<Long>> usedRecipeIds) {

        // 1. Persistir DietDay vacío → obtener ID de BD
        DietDay dietDay = new DietDay();
        dietDay.setDate(date);
        dietDay.setDiet(diet);
        dietDay.setMealSlots(new ArrayList<>());
        DietDay savedDietDay = dietOutputPort.saveDietDay(dietDay);

        int dailyTarget = diet.getCaloriesTarget() != null
                ? diet.getCaloriesTarget()
                : DEFAULT_CALORIES_TARGET;

        List<MealSlot> savedSlots = new ArrayList<>();

        // 2. BREAKFAST — objetivo fijo (25% del diario)
        double breakfastTarget = dailyTarget * BREAKFAST_PCT;
        MealSlot breakfast = MealSlot.selectBest(
                breakfastCandidates, MealType.BREAKFAST,
                breakfastTarget, savedDietDay,
                usedRecipeIds.get(MealType.BREAKFAST));

        double breakfastCalories = 0;
        if (breakfast != null) {
            MealSlot saved = mealSlotJpaOutputPort.saveMealSlot(breakfast);
            savedSlots.add(saved);
            breakfastCalories = saved.getCalories();
            usedRecipeIds.get(MealType.BREAKFAST).add(saved.getRecipe().getId());
        }

        // 3. LUNCH — objetivo ajustado dinámicamente
        // Reservamos el 35% para cena, el resto va para comida
        double remainingAfterBreakfast = dailyTarget - breakfastCalories;
        double dinnerReserve = dailyTarget * DINNER_PCT;
        double lunchTarget = Math.max(remainingAfterBreakfast - dinnerReserve, 0);

        MealSlot lunch = MealSlot.selectBest(
                lunchCandidates, MealType.LUNCH,
                lunchTarget, savedDietDay,
                usedRecipeIds.get(MealType.LUNCH));

        double lunchCalories = 0;
        if (lunch != null) {
            MealSlot saved = mealSlotJpaOutputPort.saveMealSlot(lunch);
            savedSlots.add(saved);
            lunchCalories = saved.getCalories();
            usedRecipeIds.get(MealType.LUNCH).add(saved.getRecipe().getId());
        }

        // 4. DINNER — objetivo = lo que queda del día
        double dinnerTarget = Math.max(dailyTarget - breakfastCalories - lunchCalories, 0);

        MealSlot dinner = MealSlot.selectBest(
                dinnerCandidates, MealType.DINNER,
                dinnerTarget, savedDietDay,
                usedRecipeIds.get(MealType.DINNER));

        if (dinner != null) {
            MealSlot saved = mealSlotJpaOutputPort.saveMealSlot(dinner);
            savedSlots.add(saved);
            usedRecipeIds.get(MealType.DINNER).add(saved.getRecipe().getId());

            log.debug("Day {} | target={}kcal | breakfast={}kcal lunch={}kcal dinner={}kcal | total={}kcal",
                    date, dailyTarget,
                    breakfastCalories, lunchCalories, saved.getCalories(),
                    breakfastCalories + lunchCalories + saved.getCalories());
        }

        savedDietDay.setMealSlots(savedSlots);
        return savedDietDay;
    }
}
