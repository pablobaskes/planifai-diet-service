package com.planing.diet_service.common.exception;

import com.planing.diet_service.Diet.domain.exception.MultipleActiveDietsFoundException;
import com.planing.diet_service.Diet.domain.exception.NoActiveDietFoundException;
import com.planing.diet_service.Food.domain.exception.FoodNotFoundException;
import com.planing.diet_service.ShoppingList.domain.exception.ShoppingListItemAlreadyPurchasedException;
import com.planing.diet_service.ShoppingList.domain.exception.ShoppingListNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 — recurso genérico no encontrado
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Not Found", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(ShoppingListNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShoppingListNotFound(ShoppingListNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Not Found", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(NoActiveDietFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoActiveDietFound(NoActiveDietFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Not Found", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler({
            ShoppingListItemAlreadyPurchasedException.class,
            MultipleActiveDietsFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleShoppingListConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, "Conflict", ex.getMessage(), LocalDateTime.now()));
    }

    // 422 — Food no existe al crear receta → el front redirige al formulario de Food
    @ExceptionHandler(FoodNotFoundException.class)
    public ResponseEntity<FoodNotFoundResponse> handleFoodNotFound(FoodNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new FoodNotFoundResponse(
                        422,
                        "Food Not Found",
                        ex.getMessage(),
                        ex.getFoodId(),
                        LocalDateTime.now()
                ));
    }

    // 400 — argumento ilegal
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Bad Request", ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null
                ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                : "Request validation failed.";
        return badRequest(message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParameter(MissingServletRequestParameterException ex) {
        return badRequest("Missing required request parameter: " + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return badRequest("Invalid value for parameter '" + ex.getName() + "'.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return badRequest("Malformed request body.");
    }

    private ResponseEntity<ErrorResponse> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Bad Request", message, LocalDateTime.now()));
    }

    // ── Response records ──────────────────────────────────────

    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp
    ) {}

    public record FoodNotFoundResponse(
            int status,
            String error,
            String message,
            Long missingFoodId,      // el front usa este campo para saber qué Food crear
            LocalDateTime timestamp
    ) {}
}
