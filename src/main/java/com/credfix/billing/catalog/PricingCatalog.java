package com.credfix.billing.catalog;

import java.util.Optional;

public interface PricingCatalog {
    Optional<ServicePlan> findPlan(String serviceType);
}

