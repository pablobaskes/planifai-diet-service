package com.planing.diet_service.common.exception;

import com.planing.diet_service.Diet.domain.exception.MultipleActiveDietsFoundException;
import com.planing.diet_service.Diet.domain.exception.NoActiveDietFoundException;
import com.planing.diet_service.Diet.domain.exception.OverlappingDietException;
import com.planing.diet_service.ShoppingList.domain.exception.ShoppingListItemAlreadyPurchasedException;
import com.planing.diet_service.ShoppingList.domain.exception.ShoppingListNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsShoppingListNotFoundTo404() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleShoppingListNotFound(new ShoppingListNotFoundException("No shopping list found"));

        assertError(response, HttpStatus.NOT_FOUND, "Not Found", "No shopping list found");
    }

    @Test
    void mapsNoActiveDietFoundTo404() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleNoActiveDietFound(new NoActiveDietFoundException());

        assertError(response, HttpStatus.NOT_FOUND, "Not Found", "No active diet found");
    }

    @Test
    void mapsShoppingListItemAlreadyPurchasedTo409() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleShoppingListConflict(new ShoppingListItemAlreadyPurchasedException(10L));

        assertError(response, HttpStatus.CONFLICT, "Conflict", "already purchased");
    }

    @Test
    void mapsMultipleActiveDietsFoundTo409() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleShoppingListConflict(new MultipleActiveDietsFoundException(2));

        assertError(response, HttpStatus.CONFLICT, "Conflict", "Expected exactly one active diet");
    }

    @Test
    void mapsOverlappingDietTo409() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleShoppingListConflict(new OverlappingDietException(
                        LocalDate.of(2026, 5, 11),
                        LocalDate.of(2026, 5, 17)));

        assertError(response, HttpStatus.CONFLICT, "Conflict", "already exists overlapping");
    }

    private void assertError(
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response,
            HttpStatus status,
            String error,
            String messagePart) {

        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(status.value());
        assertThat(response.getBody().error()).isEqualTo(error);
        assertThat(response.getBody().message()).contains(messagePart);
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
