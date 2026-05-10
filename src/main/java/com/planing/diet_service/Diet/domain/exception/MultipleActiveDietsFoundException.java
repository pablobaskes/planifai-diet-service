package com.planing.diet_service.Diet.domain.exception;


public class MultipleActiveDietsFoundException extends RuntimeException {
    public MultipleActiveDietsFoundException(int count) {
        super("Expected exactly one active diet but found " + count + ". Please ensure only one diet is active per week.");
    }
}
