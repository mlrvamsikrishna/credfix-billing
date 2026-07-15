package com.credfix.billing.service;

import com.credfix.billing.catalog.PricingCatalog;
import com.credfix.billing.catalog.ServicePlan;
import com.credfix.billing.domain.BillingPeriod;
import com.credfix.billing.domain.Invoice;
import com.credfix.billing.domain.InvoiceLineItem;
import com.credfix.billing.domain.UsageEvent;
import com.credfix.billing.pricing.PricingEngine;
import com.credfix.billing.store.UsageStore;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class InvoiceService {
    private final UsageStore usageStore;
    private final PricingCatalog pricingCatalog;
    private final PricingEngine pricingEngine;

    public InvoiceService(UsageStore usageStore, PricingCatalog pricingCatalog, PricingEngine pricingEngine) {
        this.usageStore = usageStore;
        this.pricingCatalog = pricingCatalog;
        this.pricingEngine = pricingEngine;
    }

    public Invoice generateInvoice(String userId, Instant startInclusive, Instant endExclusive) {
        BillingPeriod period = new BillingPeriod(startInclusive, endExclusive);
        List<UsageEvent> events = usageStore.findByUserAndPeriod(userId, period);

        // Group by service/resource first, then hand each service bucket to its pricing strategy.
        Map<String, Map<String, BigDecimal>> usageByServiceResource = aggregate(events);
        List<InvoiceLineItem> lineItems = new ArrayList<>();

        for (Map.Entry<String, Map<String, BigDecimal>> serviceEntry : usageByServiceResource.entrySet()) {
            String serviceType = serviceEntry.getKey();
            ServicePlan plan = pricingCatalog.findPlan(serviceType)
                    .orElseThrow(() -> new IllegalStateException("No pricing plan for service: " + serviceType));
            lineItems.addAll(pricingEngine.price(userId, plan, serviceEntry.getValue()));
        }

        return new Invoice(userId, startInclusive, endExclusive, lineItems);
    }

    private Map<String, Map<String, BigDecimal>> aggregate(List<UsageEvent> events) {
        // TreeMap keeps service processing order deterministic for stable invoices and tests.
        Map<String, Map<String, BigDecimal>> usageByServiceResource = new TreeMap<>();

        for (UsageEvent event : events) {
            usageByServiceResource
                    .computeIfAbsent(event.serviceType(), key -> new HashMap<>())
                    .merge(event.resourceId(), event.quantity(), BigDecimal::add);
        }

        return usageByServiceResource;
    }
}

