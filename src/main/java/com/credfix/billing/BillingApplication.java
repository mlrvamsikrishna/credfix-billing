package com.credfix.billing;

import com.credfix.billing.catalog.ConfigPricingCatalog;
import com.credfix.billing.catalog.PricingCatalog;
import com.credfix.billing.domain.Invoice;
import com.credfix.billing.domain.UsageEvent;
import com.credfix.billing.pricing.PricingEngine;
import com.credfix.billing.service.InvoiceFormatter;
import com.credfix.billing.service.InvoiceService;
import com.credfix.billing.service.UsageIngestionService;
import com.credfix.billing.store.InMemoryUsageStore;
import com.credfix.billing.store.UsageStore;

import java.math.BigDecimal;
import java.time.Instant;

public class BillingApplication {
    public static void main(String[] args) {
        PricingCatalog pricingCatalog = new ConfigPricingCatalog("/billing-config.properties");
        UsageStore usageStore = new InMemoryUsageStore();
        UsageIngestionService ingestionService = new UsageIngestionService(usageStore, pricingCatalog);
        InvoiceService invoiceService = new InvoiceService(usageStore, pricingCatalog, new PricingEngine());
        InvoiceFormatter formatter = new InvoiceFormatter();

        seedUsageEvents(ingestionService);

        Invoice aliceInvoice = invoiceService.generateInvoice(
                "alice",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z")
        );

        System.out.println(formatter.format(aliceInvoice));
    }

    private static void seedUsageEvents(UsageIngestionService ingestionService) {
        // Alice events (intentionally out of order to show order-independence)
        ingestionService.ingest(usage("alice", "api-gw-2", "api", "500000", "CALL", "2026-01-28T11:00:00Z"));
        ingestionService.ingest(usage("alice", "db-backup-1", "storage", "120", "GB_HOUR", "2026-01-05T10:15:00Z"));
        ingestionService.ingest(usage("alice", "worker-node-1", "compute", "150", "HOUR", "2026-01-03T03:00:00Z"));
        ingestionService.ingest(usage("alice", "api-gw-1", "api", "800000", "CALL", "2026-01-18T10:30:00Z"));
        ingestionService.ingest(usage("alice", "worker-node-2", "compute", "1000", "HOUR", "2026-01-20T08:00:00Z"));
        ingestionService.ingest(usage("alice", "db-backup-2", "storage", "80", "GB_HOUR", "2026-01-12T09:00:00Z"));

        // Boundary event exactly at endExclusive should not be billed for January.
        ingestionService.ingest(usage("alice", "db-backup-1", "storage", "500", "GB_HOUR", "2026-02-01T00:00:00Z"));

        // Bob events prove multi-user isolation.
        ingestionService.ingest(usage("bob", "db-backup-9", "storage", "50", "GB_HOUR", "2026-01-14T09:30:00Z"));
        ingestionService.ingest(usage("bob", "worker-node-9", "compute", "40", "HOUR", "2026-01-11T02:00:00Z"));
    }

    private static UsageEvent usage(
            String userId,
            String resourceId,
            String serviceType,
            String quantity,
            String unit,
            String timestamp
    ) {
        return new UsageEvent(
                userId,
                resourceId,
                serviceType,
                new BigDecimal(quantity),
                unit,
                Instant.parse(timestamp)
        );
    }
}

