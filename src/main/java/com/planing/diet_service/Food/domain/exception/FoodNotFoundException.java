package com.planing.diet_service.Food.domain.exception;

public class FoodNotFoundException extends RuntimeException {

    private final Long foodId;

    public FoodNotFoundException(Long foodId) {
        super("Food not found with id: " + foodId + ". Please create the food first.");
        this.foodId = foodId;
    }

    public Long getFoodId() {
        return foodId;
    }
}
