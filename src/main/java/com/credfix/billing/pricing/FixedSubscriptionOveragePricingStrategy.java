package com.credfix.billing.pricing;

import com.credfix.billing.catalog.ServicePlan;
import com.credfix.billing.domain.InvoiceLineItem;
import com.credfix.billing.domain.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FixedSubscriptionOveragePricingStrategy implements PricingStrategy {
    @Override
    public String billingType() {
        return "fixed_subscription_overage";
    }

    @Override
    public List<InvoiceLineItem> price(String userId, ServicePlan plan, Map<String, BigDecimal> usageByResource) {
        BigDecimal subscriptionFee = new BigDecimal(plan.requireParam("subscriptionFee"));
        BigDecimal includedQuantity = new BigDecimal(plan.requireParam("includedQuantity"));
        BigDecimal overageRate = new BigDecimal(plan.requireParam("overageRate"));

        BigDecimal includedRemaining = includedQuantity;
        List<InvoiceLineItem> items = new ArrayList<>();

        Map<String, BigDecimal> ordered = new TreeMap<>(usageByResource);
        for (Map.Entry<String, BigDecimal> entry : ordered.entrySet()) {
            BigDecimal quantity = entry.getValue();
            BigDecimal overageQuantity = BigDecimal.ZERO;

            if (includedRemaining.signum() > 0) {
                BigDecimal covered = quantity.min(includedRemaining);
                includedRemaining = includedRemaining.subtract(covered);
                overageQuantity = quantity.subtract(covered);
            } else {
                overageQuantity = quantity;
            }

            items.add(new InvoiceLineItem(
                    userId,
                    plan.serviceType(),
                    entry.getKey(),
                    quantity,
                    plan.unit(),
                    overageRate,
                    Money.of(overageQuantity.multiply(overageRate)),
                    "Subscription overage"
            ));
        }

        // Service-level fixed fee is represented as a separate line item for transparency.
        items.add(new InvoiceLineItem(
                userId,
                plan.serviceType(),
                "SUBSCRIPTION",
                BigDecimal.ONE,
                "MONTH",
                subscriptionFee,
                Money.of(subscriptionFee),
                "Fixed monthly subscription"
        ));

        return items;
    }
}

