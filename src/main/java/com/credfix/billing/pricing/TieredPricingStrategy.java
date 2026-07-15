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
 * Tiers are configured as: "100:0.10,900:0.08,INF:0.05"
 * meaning first 100 units at 0.10, next 900 at 0.08, remaining at 0.05.
 */
public class TieredPricingStrategy implements PricingStrategy {
    private static final BigDecimal INF_SENTINEL = new BigDecimal("-1");

    @Override
    public String billingType() {
        return "tiered";
    }

    @Override
    public List<InvoiceLineItem> price(String userId, ServicePlan plan, Map<String, BigDecimal> usageByResource) {
        List<Tier> tiers = parseTiers(plan.requireParam("tiers"));
        // One cursor is shared across resources so tiers are consumed cumulatively per service.
        TierCursor cursor = new TierCursor(tiers);

        Map<String, BigDecimal> ordered = new TreeMap<>(usageByResource);
        List<InvoiceLineItem> items = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : ordered.entrySet()) {
            BigDecimal quantity = entry.getValue();
            BigDecimal charge = cursor.consume(quantity);
            BigDecimal unitPrice = quantity.signum() == 0
                    ? BigDecimal.ZERO
                    : charge.divide(quantity, Money.SCALE, RoundingMode.HALF_UP);

            items.add(new InvoiceLineItem(
                    userId,
                    plan.serviceType(),
                    entry.getKey(),
                    quantity,
                    plan.unit(),
                    unitPrice,
                    Money.of(charge),
                    "Tiered"
            ));
        }
        return items;
    }

    private List<Tier> parseTiers(String rawTiers) {
        List<Tier> tiers = new ArrayList<>();
        for (String token : rawTiers.split(",")) {
            String[] parts = token.trim().split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid tier token: " + token);
            }

            BigDecimal limit = "INF".equalsIgnoreCase(parts[0].trim())
                    ? INF_SENTINEL
                    : new BigDecimal(parts[0].trim());
            BigDecimal rate = new BigDecimal(parts[1].trim());
            tiers.add(new Tier(limit, rate));
        }

        if (tiers.isEmpty()) {
            throw new IllegalArgumentException("At least one tier is required");
        }
        Tier lastTier = tiers.get(tiers.size() - 1);
        if (!isUnlimitedLimit(lastTier.quantityLimit())) {
            // Without an open-ended last tier, high usage could leave remaining units unpriced.
            throw new IllegalArgumentException("Tier config must end with INF to cover unbounded usage");
        }
        return tiers;
    }

    private boolean isUnlimitedLimit(BigDecimal quantityLimit) {
        return quantityLimit.compareTo(INF_SENTINEL) == 0;
    }

    private record Tier(BigDecimal quantityLimit, BigDecimal rate) {
    }

    private static final class TierCursor {
        private final List<Tier> tiers;
        private int index;
        private BigDecimal remainingInCurrentTier;

        private TierCursor(List<Tier> tiers) {
            this.tiers = tiers;
            this.index = 0;
            this.remainingInCurrentTier = normalizeLimit(tiers.get(0).quantityLimit());
        }

        private BigDecimal consume(BigDecimal quantity) {
            BigDecimal remaining = quantity;
            BigDecimal charge = BigDecimal.ZERO;

            while (remaining.signum() > 0) {
                Tier tier = tiers.get(index);
                BigDecimal consume = isUnlimited(remainingInCurrentTier)
                        ? remaining
                        : remaining.min(remainingInCurrentTier);

                charge = charge.add(consume.multiply(tier.rate()));
                remaining = remaining.subtract(consume);

                if (!isUnlimited(remainingInCurrentTier)) {
                    remainingInCurrentTier = remainingInCurrentTier.subtract(consume);
                    if (remainingInCurrentTier.signum() == 0 && index < tiers.size() - 1) {
                        index += 1;
                        remainingInCurrentTier = normalizeLimit(tiers.get(index).quantityLimit());
                    } else if (remainingInCurrentTier.signum() == 0) {
                        throw new IllegalStateException("Tier limits exhausted; ensure last tier is INF");
                    }
                }
            }
            return charge;
        }

        private static boolean isUnlimited(BigDecimal quantityLimit) {
            return quantityLimit.compareTo(INF_SENTINEL) == 0;
        }

        private static BigDecimal normalizeLimit(BigDecimal quantityLimit) {
            return quantityLimit.compareTo(INF_SENTINEL) == 0 ? INF_SENTINEL : quantityLimit;
        }
    }
}


