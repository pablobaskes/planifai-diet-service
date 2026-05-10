package com.planing.diet_service.ShoppingList.domain.service;

import com.planing.diet_service.FoodPortion.domain.model.Unit;
import org.springframework.stereotype.Component;

/**
 * Servicio de dominio para conversión y compatibilidad de unidades.
 * MVP1: solo soporta KG ↔ G y L ↔ ML.
 * Unidades incompatibles se tratan como items distintos.
 */
@Component
public class UnitConversionService {

    /**
     * Determina si dos unidades son compatibles (convertibles entre sí).
     */
    public boolean areCompatible(Unit first, Unit second) {
        if (first == second) return true;
        return isWeightPair(first, second) || isVolumePair(first, second);
    }

    /**
     * Convierte una cantidad de la unidad origen a la unidad destino.
     * Solo funciona con unidades compatibles.
     *
     * @throws IllegalArgumentException si las unidades no son compatibles.
     */
    public double convert(double quantity, Unit from, Unit to) {
        if (from == to) return quantity;

        if (from == Unit.KG && to == Unit.G)  return quantity * 1000;
        if (from == Unit.G  && to == Unit.KG) return quantity / 1000;
        if (from == Unit.L  && to == Unit.ML) return quantity * 1000;
        if (from == Unit.ML && to == Unit.L)  return quantity / 1000;

        throw new IllegalArgumentException(
                "Units are not compatible: " + from + " and " + to);
    }

    /**
     * Devuelve la unidad canónica del grupo (la más pequeña).
     * G para el grupo de peso, ML para el grupo de volumen.
     * Para otras unidades, devuelve la misma unidad.
     */
    public Unit canonicalUnit(Unit unit) {
        return switch (unit) {
            case KG -> Unit.G;
            case L  -> Unit.ML;
            default -> unit;
        };
    }

    /**
     * Convierte una cantidad a su unidad canónica.
     */
    public double toCanonical(double quantity, Unit unit) {
        return convert(quantity, unit, canonicalUnit(unit));
    }

    // ── Helpers privados ──────────────────────────────────────

    private boolean isWeightPair(Unit a, Unit b) {
        return (a == Unit.G || a == Unit.KG) && (b == Unit.G || b == Unit.KG);
    }

    private boolean isVolumePair(Unit a, Unit b) {
        return (a == Unit.ML || a == Unit.L) && (b == Unit.ML || b == Unit.L);
    }
}
