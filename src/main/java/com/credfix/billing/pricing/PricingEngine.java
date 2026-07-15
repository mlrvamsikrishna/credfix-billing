package com.credfix.billing.pricing;

import com.credfix.billing.catalog.ServicePlan;
import com.credfix.billing.domain.InvoiceLineItem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Resolves strategies via Java SPI. New billing types are added by adding a new strategy class
 * and updating META-INF/services, without touching this class.
 */
public class PricingEngine {
    private final Map<String, PricingStrategy> strategies;

    public PricingEngine() {
        this.strategies = loadStrategies();
    }

    /**
     * Delegates pricing to the strategy mapped by plan.billingType().
     */
    public List<InvoiceLineItem> price(String userId, ServicePlan plan, Map<String, BigDecimal> usageByResource) {
        PricingStrategy strategy = strategies.get(plan.billingType());
        if (strategy == null) {
            throw new IllegalArgumentException("No pricing strategy registered for billingType=" + plan.billingType());
        }
        return strategy.price(userId, plan, usageByResource);
    }

    private Map<String, PricingStrategy> loadStrategies() {
        Map<String, PricingStrategy> loaded = new HashMap<>();
        ServiceLoader<PricingStrategy> loader = ServiceLoader.load(PricingStrategy.class);
        for (PricingStrategy strategy : loader) {
            // Last write wins if duplicates exist; keeping this explicit simplifies extension debugging.
            loaded.put(strategy.billingType(), strategy);
        }
        if (loaded.isEmpty()) {
            throw new IllegalStateException("No pricing strategies were discovered via ServiceLoader");
        }
        return Map.copyOf(loaded);
    }
}

