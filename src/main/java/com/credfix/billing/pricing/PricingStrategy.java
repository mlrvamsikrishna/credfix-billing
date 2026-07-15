package com.credfix.billing.pricing;

import com.credfix.billing.catalog.ServicePlan;
import com.credfix.billing.domain.InvoiceLineItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Strategy contract for one billing type.
 */
public interface PricingStrategy {
    /**
     * Unique billing type identifier used in configuration (for example: tiered).
     */
    String billingType();

    /**
     * Prices aggregated usage per resource and returns invoice line items.
     */
    List<InvoiceLineItem> price(
            String userId,
            ServicePlan plan,
            Map<String, BigDecimal> usageByResource
    );
}

