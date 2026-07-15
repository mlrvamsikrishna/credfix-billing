package com.credfix.billing.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record UsageEvent(
        String userId,
        String resourceId,
        String serviceType,
        BigDecimal quantity,
        String unit,
        Instant timestamp
) {
    public UsageEvent {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(resourceId, "resourceId is required");
        Objects.requireNonNull(serviceType, "serviceType is required");
        Objects.requireNonNull(quantity, "quantity is required");
        Objects.requireNonNull(unit, "unit is required");
        Objects.requireNonNull(timestamp, "timestamp is required");

        if (quantity.signum() < 0) {
            throw new IllegalArgumentException("quantity must be non-negative");
        }
    }
}

