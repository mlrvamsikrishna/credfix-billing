package com.credfix.billing.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record InvoiceLineItem(
        String userId,
        String serviceType,
        String resourceId,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPrice,
        Money charge,
        String description
) {
    public InvoiceLineItem {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(serviceType, "serviceType is required");
        Objects.requireNonNull(resourceId, "resourceId is required");
        Objects.requireNonNull(quantity, "quantity is required");
        Objects.requireNonNull(unit, "unit is required");
        Objects.requireNonNull(unitPrice, "unitPrice is required");
        Objects.requireNonNull(charge, "charge is required");
        Objects.requireNonNull(description, "description is required");
    }
}

