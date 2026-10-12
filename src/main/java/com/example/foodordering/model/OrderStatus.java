package com.example.foodordering.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        if (this == next) {
            return true;
        }
        return switch (this) {
            case PENDING -> EnumSet.of(CONFIRMED, CANCELLED).contains(next);
            case CONFIRMED -> EnumSet.of(PREPARING, CANCELLED).contains(next);
            case PREPARING -> EnumSet.of(OUT_FOR_DELIVERY, CANCELLED).contains(next);
            case OUT_FOR_DELIVERY -> EnumSet.of(DELIVERED, CANCELLED).contains(next);
            case DELIVERED, CANCELLED -> false; // Terminal states
        };
    }

    public Set<OrderStatus> getValidNextStates() {
        return switch (this) {
            case PENDING -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(PREPARING, CANCELLED);
            case PREPARING -> EnumSet.of(OUT_FOR_DELIVERY, CANCELLED);
            case OUT_FOR_DELIVERY -> EnumSet.of(DELIVERED, CANCELLED);
            case DELIVERED, CANCELLED -> Collections.emptySet();
        };
    }
}
