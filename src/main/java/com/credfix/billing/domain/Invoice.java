package com.credfix.billing.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable invoice aggregate containing line items, per-service subtotals, and grand total.
 */
public final class Invoice {
    private final String userId;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final List<InvoiceLineItem> lineItems;
    private final Map<String, Money> serviceSubtotals;
    private final Money total;

    public Invoice(String userId, Instant periodStart, Instant periodEnd, List<InvoiceLineItem> lineItems) {
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.periodStart = Objects.requireNonNull(periodStart, "periodStart is required");
        this.periodEnd = Objects.requireNonNull(periodEnd, "periodEnd is required");

        List<InvoiceLineItem> sorted = new ArrayList<>(Objects.requireNonNull(lineItems, "lineItems is required"));
        // Stable ordering helps deterministic output and reproducible tests.
        sorted.sort(Comparator.comparing(InvoiceLineItem::serviceType).thenComparing(InvoiceLineItem::resourceId));
        this.lineItems = List.copyOf(sorted);

        Map<String, Money> subtotals = new LinkedHashMap<>();
        Money totalAccumulator = Money.ZERO;
        for (InvoiceLineItem lineItem : this.lineItems) {
            subtotals.merge(lineItem.serviceType(), lineItem.charge(), Money::plus);
            totalAccumulator = totalAccumulator.plus(lineItem.charge());
        }

        this.serviceSubtotals = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(subtotals));
        this.total = totalAccumulator;
    }

    public String userId() {
        return userId;
    }

    public Instant periodStart() {
        return periodStart;
    }

    public Instant periodEnd() {
        return periodEnd;
    }

    public List<InvoiceLineItem> lineItems() {
        return lineItems;
    }

    public Map<String, Money> serviceSubtotals() {
        return serviceSubtotals;
    }

    public Money total() {
        return total;
    }
}


