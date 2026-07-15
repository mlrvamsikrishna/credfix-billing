package com.credfix.billing.store;

import com.credfix.billing.domain.BillingPeriod;
import com.credfix.billing.domain.UsageEvent;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory implementation hidden behind UsageStore, so persistence can be swapped later.
 */
public class InMemoryUsageStore implements UsageStore {
    private final List<UsageEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public void append(UsageEvent usageEvent) {
        events.add(usageEvent);
    }

    @Override
    public List<UsageEvent> findByUserAndPeriod(String userId, BillingPeriod period) {
        return events.stream()
                .filter(event -> event.userId().equals(userId))
                .filter(event -> period.contains(event.timestamp()))
                .sorted(Comparator.comparing(UsageEvent::timestamp)
                        .thenComparing(UsageEvent::serviceType)
                        .thenComparing(UsageEvent::resourceId))
                .toList();
    }
}

