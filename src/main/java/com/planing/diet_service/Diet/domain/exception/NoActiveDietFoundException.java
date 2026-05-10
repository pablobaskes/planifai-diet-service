package com.planing.diet_service.Diet.domain.exception;

public class NoActiveDietFoundException extends RuntimeException {
    public NoActiveDietFoundException() {
        super("No active diet found for the current week. Please create a diet first.");
    }
}