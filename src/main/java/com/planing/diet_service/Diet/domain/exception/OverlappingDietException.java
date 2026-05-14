package com.planing.diet_service.Diet.domain.exception;

import java.time.LocalDate;

public class OverlappingDietException extends RuntimeException {
    public OverlappingDietException(LocalDate initDate, LocalDate endDate) {
        super("A diet already exists overlapping the requested range " + initDate + " to " + endDate
                + ". Delete the existing diet before creating a new overlapping diet.");
    }
}
