package com.credfix.billing.service;

import com.credfix.billing.catalog.PricingCatalog;
import com.credfix.billing.domain.UsageEvent;
import com.credfix.billing.store.UsageStore;

public class UsageIngestionService {
    private final UsageStore usageStore;
    private final PricingCatalog pricingCatalog;

    public UsageIngestionService(UsageStore usageStore, PricingCatalog pricingCatalog) {
        this.usageStore = usageStore;
        this.pricingCatalog = pricingCatalog;
    }

    public void ingest(UsageEvent usageEvent) {
        var plan = pricingCatalog.findPlan(usageEvent.serviceType())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown serviceType in usage event: " + usageEvent.serviceType()
                ));

        if (!plan.unit().equals(usageEvent.unit())) {
            throw new IllegalArgumentException(
                    "Unit mismatch for service " + usageEvent.serviceType()
                            + ": expected=" + plan.unit()
                            + ", actual=" + usageEvent.unit()
            );
        }

        usageStore.append(usageEvent);
    }
}


