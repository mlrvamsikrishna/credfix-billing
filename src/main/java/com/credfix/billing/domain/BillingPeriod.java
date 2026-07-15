package com.credfix.billing.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Billing interval represented as [startInclusive, endExclusive).
 */
public record BillingPeriod(Instant startInclusive, Instant endExclusive) {
    public BillingPeriod {
        Objects.requireNonNull(startInclusive, "startInclusive is required");
        Objects.requireNonNull(endExclusive, "endExclusive is required");
        if (!startInclusive.isBefore(endExclusive)) {
            throw new IllegalArgumentException("startInclusive must be before endExclusive");
        }
    }

    /**
     * True when timestamp is within [startInclusive, endExclusive).
     */
    public boolean contains(Instant timestamp) {
        return !timestamp.isBefore(startInclusive) && timestamp.isBefore(endExclusive);
    }
}

