package com.credfix.billing.pricing;

import com.credfix.billing.catalog.ServicePlan;
import com.credfix.billing.domain.InvoiceLineItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PricingStrategy {
    String billingType();

    List<InvoiceLineItem> price(
            String userId,
            ServicePlan plan,
            Map<String, BigDecimal> usageByResource
    );
}

