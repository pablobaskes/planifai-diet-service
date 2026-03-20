package com.planing.diet_service.MealSlot.domain.utils;

public final class DietConstants {

    private DietConstants() {}

    // ── Distribución calórica por franja ──────────────────────
    public static final double BREAKFAST_PCT = 0.25;
    public static final double LUNCH_PCT     = 0.40;
    public static final double DINNER_PCT    = 0.35;

    // ── Margen de tolerancia calórica en selección de recetas ─
    public static final double TOLERANCE_PCT = 0.30;

    // ── Calorías objetivo por defecto si no se especifica ─────
    public static final int DEFAULT_CALORIES_TARGET = 2000;
}
