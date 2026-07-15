package com.credfix.billing.store;

import com.credfix.billing.domain.BillingPeriod;
import com.credfix.billing.domain.UsageEvent;

import java.util.List;

/**
 * Storage abstraction for usage events.
 */
public interface UsageStore {
    /**
     * Appends one usage event.
     */
    void append(UsageEvent usageEvent);

    /**
     * Returns events for a user that fall within the billing period.
     */
    List<UsageEvent> findByUserAndPeriod(String userId, BillingPeriod period);
}

