package com.credfix.billing.pricing;

import com.credfix.billing.catalog.ServicePlan;
import com.credfix.billing.domain.InvoiceLineItem;
import com.credfix.billing.domain.Money;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Prices each resource independently using a constant rate from configuration.
 */
public class FlatPerUnitPricingStrategy implements PricingStrategy {
    @Override
    public String billingType() {
        return "flat_per_unit";
    }

    @Override
    public List<InvoiceLineItem> price(String userId, ServicePlan plan, Map<String, BigDecimal> usageByResource) {
        BigDecimal unitRate = new BigDecimal(plan.requireParam("rate"));

        return usageByResource.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    BigDecimal quantity = entry.getValue();
                    return new InvoiceLineItem(
                            userId,
                            plan.serviceType(),
                            entry.getKey(),
                            quantity,
                            plan.unit(),
                            unitRate,
                            Money.of(quantity.multiply(unitRate)),
                            "Flat per-unit"
                    );
                })
                .sorted(Comparator.comparing(InvoiceLineItem::resourceId))
                .toList();
    }
}

