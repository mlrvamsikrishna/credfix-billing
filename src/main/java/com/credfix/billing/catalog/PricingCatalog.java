package com.credfix.billing.catalog;

import java.util.Optional;

/**
 * Read-only catalog used by ingestion and invoice services to resolve service pricing plans.
 */
public interface PricingCatalog {
    /**
     * Returns the configured plan for the given service type, if present.
     */
    Optional<ServicePlan> findPlan(String serviceType);
}

