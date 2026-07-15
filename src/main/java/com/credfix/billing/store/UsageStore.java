package com.credfix.billing.store;

import com.credfix.billing.domain.BillingPeriod;
import com.credfix.billing.domain.UsageEvent;

import java.util.List;

public interface UsageStore {
    void append(UsageEvent usageEvent);

    List<UsageEvent> findByUserAndPeriod(String userId, BillingPeriod period);
}

