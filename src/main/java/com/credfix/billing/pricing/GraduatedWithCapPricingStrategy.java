package com.credfix.billing.pricing;

import com.credfix.billing.catalog.ServicePlan;
import com.credfix.billing.domain.InvoiceLineItem;
import com.credfix.billing.domain.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Example extension model: normal per-unit billing with an upper monthly cap.
 * Params: rate, maxCharge
 */
public class GraduatedWithCapPricingStrategy implements PricingStrategy {
    @Override
    public String billingType() {
        return "graduated_with_cap";
    }

    @Override
    public List<InvoiceLineItem> price(String userId, ServicePlan plan, Map<String, BigDecimal> usageByResource) {
        BigDecimal rate = new BigDecimal(plan.requireParam("rate"));
        BigDecimal maxCharge = new BigDecimal(plan.requireParam("maxCharge"));

        BigDecimal capRemaining = maxCharge;
        List<InvoiceLineItem> items = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : new TreeMap<>(usageByResource).entrySet()) {
            BigDecimal quantity = entry.getValue();
            BigDecimal rawCharge = quantity.multiply(rate);
            BigDecimal appliedCharge = rawCharge.min(capRemaining.max(BigDecimal.ZERO));
            capRemaining = capRemaining.subtract(appliedCharge);

            BigDecimal effectiveUnitPrice = quantity.signum() == 0
                    ? BigDecimal.ZERO
                    : appliedCharge.divide(quantity, Money.SCALE, RoundingMode.HALF_UP);

            items.add(new InvoiceLineItem(
                    userId,
                    plan.serviceType(),
                    entry.getKey(),
                    quantity,
                    plan.unit(),
                    effectiveUnitPrice,
                    Money.of(appliedCharge),
                    "Graduated with cap"
            ));
        }

        return items;
    }
}

