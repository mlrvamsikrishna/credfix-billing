package com.credfix.billing;

import com.credfix.billing.catalog.ConfigPricingCatalog;
import com.credfix.billing.catalog.PricingCatalog;
import com.credfix.billing.catalog.ServicePlan;
import com.credfix.billing.domain.Invoice;
import com.credfix.billing.domain.Money;
import com.credfix.billing.domain.UsageEvent;
import com.credfix.billing.pricing.PricingEngine;
import com.credfix.billing.service.InvoiceService;
import com.credfix.billing.service.UsageIngestionService;
import com.credfix.billing.store.InMemoryUsageStore;
import com.credfix.billing.store.UsageStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BillingSystemTest {
    @Test
    void generatesInvoiceWithAllPricingModelsAndPeriodFiltering() {
        PricingCatalog pricingCatalog = new ConfigPricingCatalog("/billing-config.properties");
        UsageStore usageStore = new InMemoryUsageStore();
        UsageIngestionService ingestionService = new UsageIngestionService(usageStore, pricingCatalog);
        InvoiceService invoiceService = new InvoiceService(usageStore, pricingCatalog, new PricingEngine());

        ingestionService.ingest(usage("alice", "db-backup-1", "storage", "120", "GB_HOUR", "2026-01-05T10:15:00Z"));
        ingestionService.ingest(usage("alice", "db-backup-2", "storage", "80", "GB_HOUR", "2026-01-12T09:00:00Z"));
        ingestionService.ingest(usage("alice", "worker-node-1", "compute", "150", "HOUR", "2026-01-03T03:00:00Z"));
        ingestionService.ingest(usage("alice", "worker-node-2", "compute", "1000", "HOUR", "2026-01-20T08:00:00Z"));
        ingestionService.ingest(usage("alice", "api-gw-1", "api", "800000", "CALL", "2026-01-18T10:30:00Z"));
        ingestionService.ingest(usage("alice", "api-gw-2", "api", "500000", "CALL", "2026-01-28T11:00:00Z"));
        ingestionService.ingest(usage("alice", "db-backup-1", "storage", "500", "GB_HOUR", "2026-02-01T00:00:00Z"));

        // Second user events must not affect Alice's invoice.
        ingestionService.ingest(usage("bob", "worker-node-9", "compute", "40", "HOUR", "2026-01-11T02:00:00Z"));

        Invoice invoice = invoiceService.generateInvoice(
                "alice",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z")
        );

        assertEquals(Money.of("4.000000").amount(), invoice.serviceSubtotals().get("storage").amount());
        assertEquals(Money.of("89.500000").amount(), invoice.serviceSubtotals().get("compute").amount());
        assertEquals(Money.of("350.000000").amount(), invoice.serviceSubtotals().get("api").amount());
        assertEquals(Money.of("443.500000").amount(), invoice.total().amount());
        assertEquals(7, invoice.lineItems().size());
    }

    @Test
    void supportsNewBillingTypeViaSpiWithoutEngineChanges() {
        PricingEngine pricingEngine = new PricingEngine();
        ServicePlan plan = new ServicePlan(
                "cdn",
                "graduated_with_cap",
                "GB",
                Map.of("rate", "0.40", "maxCharge", "100.00")
        );

        var items = pricingEngine.price(
                "alice",
                plan,
                Map.of("cdn-asset-a", new BigDecimal("300"), "cdn-asset-b", new BigDecimal("100"))
        );

        assertEquals(2, items.size());
        assertEquals(Money.of("100.000000").amount(), items.get(0).charge().plus(items.get(1).charge()).amount());
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


