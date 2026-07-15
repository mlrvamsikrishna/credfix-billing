package com.credfix.billing.catalog;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Runtime pricing configuration for one billable service.
 */
public record ServicePlan(
        String serviceType,
        String billingType,
        String unit,
        Map<String, String> params
) {
    public ServicePlan {
        Objects.requireNonNull(serviceType, "serviceType is required");
        Objects.requireNonNull(billingType, "billingType is required");
        Objects.requireNonNull(unit, "unit is required");
        params = params == null ? Map.of() : Collections.unmodifiableMap(params);
    }

    public String requireParam(String key) {
        String value = params.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing required parameter '" + key + "' for service " + serviceType
            );
        }
        return value;
    }
}

