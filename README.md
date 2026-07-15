# Credfix Usage-Based Billing System

![Java 17](https://img.shields.io/badge/Java-17-informational)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36)
![Architecture](https://img.shields.io/badge/Design-Strategy%20%2B%20SPI-blue)
![Money Safety](https://img.shields.io/badge/Money-BigDecimal-success)

Config-driven Java 17 billing engine for cloud-style usage charging, built to showcase extensibility, correctness, and clean separation of concerns.

## Why this project stands out

- **Extensible by design:** add a new billing model as a new `PricingStrategy` + config entry, without modifying `PricingEngine`
- **Config-driven:** rates, tiers, and subscription settings live in `billing-config.properties`
- **Correct money handling:** uses `BigDecimal` through the `Money` value object
- **Deterministic billing:** invoices are stable even if usage events arrive out of order
- **Recruiter-friendly demo:** includes a runnable app, test coverage, and a fourth pricing model to prove open/closed design

## What this demonstrates

- Usage ingestion for `(user, resource, service)` usage events with timestamps
- Pricing strategy layer separated from storage and invoice assembly
- Three billing models:
  - `flat_per_unit`
  - `tiered`
  - `fixed_subscription_overage`
- Plus one optional extension model for recruiter demo:
  - `graduated_with_cap`
- Invoice generation for a billing period `[start, end)` with:
  - per-resource line items
  - per-service subtotals
  - invoice total
- Safe money handling via `BigDecimal` (no binary floating point)
- Out-of-order event ingestion with deterministic invoice results
- In-memory storage behind an interface for easy persistence swap

## Assignment requirement coverage

| Requirement | Status | Where it is implemented |
| --- | --- | --- |
| Usage tracking for `(user, resource)` scoped events | ✅ | `UsageEvent`, `UsageIngestionService`, `UsageStore` |
| Flat per-unit pricing | ✅ | `FlatPerUnitPricingStrategy` |
| Tiered pricing | ✅ | `TieredPricingStrategy` |
| Fixed subscription + overage pricing | ✅ | `FixedSubscriptionOveragePricingStrategy` |
| Add fourth model without changing existing pricing engine code | ✅ | `GraduatedWithCapPricingStrategy` via Java SPI |
| Per-resource line items | ✅ | `Invoice`, `InvoiceLineItem`, `InvoiceService` |
| Per-service subtotals | ✅ | `InvoiceService` aggregation |
| Total invoice amount | ✅ | `Invoice` total assembly |
| `[start, end)` billing period enforcement | ✅ | `InvoiceService`, covered in tests |
| Safe money representation | ✅ | `Money` (`BigDecimal`) |
| Config-driven pricing | ✅ | `ConfigPricingCatalog`, `billing-config.properties` |
| Out-of-order event correctness | ✅ | deterministic sorting before billing |
| Swappable persistence abstraction | ✅ | `UsageStore` interface + `InMemoryUsageStore` |
| Small driver/test with two users and three services | ✅ | `BillingApplication`, `BillingSystemTest` |

## Project structure

- `src/main/java/com/credfix/billing/catalog` - runtime pricing config model/loading
- `src/main/java/com/credfix/billing/domain` - domain objects (`UsageEvent`, `Invoice`, `Money`)
- `src/main/java/com/credfix/billing/pricing` - strategy interface + implementations + engine
- `src/main/java/com/credfix/billing/service` - ingestion and invoice application services
- `src/main/java/com/credfix/billing/store` - usage repository abstraction + in-memory store
- `src/main/resources/billing-config.properties` - external pricing configuration
- `src/main/resources/META-INF/services/com.credfix.billing.pricing.PricingStrategy` - SPI registration

## Architecture at a glance

```text
UsageEvent -> UsageIngestionService -> UsageStore
                                      |
                                      v
                           InvoiceService -> PricingEngine -> PricingStrategy (SPI)
                                      |                               |
                                      v                               v
                                  Invoice                    ServicePlan from catalog
```

## Pricing configuration

The system resolves service plans at runtime from:

- `src/main/resources/billing-config.properties`

Example:

```properties
services=storage,compute,api

service.storage.billingType=flat_per_unit
service.storage.unit=GB_HOUR
service.storage.rate=0.02

service.compute.billingType=tiered
service.compute.unit=HOUR
service.compute.tiers=100:0.10,900:0.08,INF:0.05

service.api.billingType=fixed_subscription_overage
service.api.unit=CALL
service.api.subscriptionFee=50.00
service.api.includedQuantity=1000000
service.api.overageRate=0.001
```

## How extensibility works

To add a new billing type, do not modify existing pricing engine code.

1. Implement a new class that implements `PricingStrategy`.
2. Return a unique billing type string from `billingType()`.
3. Add your class name to:
   - `src/main/resources/META-INF/services/com.credfix.billing.pricing.PricingStrategy`
4. Add a new service entry in `billing-config.properties` referencing your new billing type.

`PricingEngine` discovers implementations via Java `ServiceLoader`.

The repository already includes `GraduatedWithCapPricingStrategy` as a proof point that
adding a fourth type required no changes to `PricingEngine`.

## Quick start

```bash
cd /Users/vamsikrishna.majeti/Downloads/credfix-billing
mvn -q test
mvn -q package
java -jar target/credfix-billing-1.0.0.jar
```

## What to run

- Main application: `src/main/java/com/credfix/billing/BillingApplication.java`
- Maven package output: `target/credfix-billing-1.0.0.jar`

If you prefer running from IntelliJ:

1. Open the Maven project from `pom.xml`
2. Ensure Project SDK is set to JDK 17
3. Run the `main` method in `BillingApplication`

## Expected demo behavior

The demo application:

- ingests usage for **two users**
- covers **three required billing models**
- generates **one invoice** for a billing period
- prints per-resource charges, per-service subtotals, and the grand total

## IntelliJ fix for red `java.*` imports

If IntelliJ shows `Cannot resolve symbol 'java'` in all classes, it is usually an SDK/indexing setup issue (not source code).

1. Open project as Maven project from `pom.xml`.
2. Set Project SDK to JDK 17:
   - `File` -> `Project Structure` -> `Project` -> `SDK = 17`
3. Re-import Maven:
   - Maven tool window -> Reload All Maven Projects
4. Invalidate caches if needed:
   - `File` -> `Invalidate Caches...` -> `Invalidate and Restart`

Quick terminal sanity check (already validated in this project):

```bash
cd /Users/vamsikrishna.majeti/Downloads/credfix-billing
mvn -q test
```

If this passes while IDE still shows red imports, the issue is IDE SDK/indexing.

Important: **do not add a Maven dependency for `java.*` imports.**
Those packages come from the JDK, not from Maven Central. A red `import java...` error usually means IntelliJ is not attached to the correct JDK.

## GitHub submission checklist

```bash
cd /Users/vamsikrishna.majeti/Downloads/credfix-billing
git init
git add .
git commit -m "Implement config-driven usage-based billing engine"
git branch -M main
git remote add origin <your-github-repo-url>
git push -u origin main
```

Before pushing, add a local `.gitignore` if needed for Java/IDE artifacts:

```gitignore
target/
.idea/
*.iml
```

## Suggested repository extras before submission

- Add a concise repository description on GitHub: `Config-driven Java 17 usage-based billing engine with pluggable pricing strategies`
- Pin the repository if this is a hiring-focused profile
- Keep commits clean and descriptive
- If time permits, add one screenshot of the printed invoice to the repo `README`

## Notes on correctness

- Billing period check is inclusive of start and exclusive of end.
- Tier arithmetic is applied cumulatively per service across resources in deterministic resource order.
- Subscription-overage model emits a service-level `SUBSCRIPTION` line item plus per-resource overage lines.
- Usage events are sorted by timestamp/service/resource for deterministic processing regardless of ingest order.

## Test coverage

`BillingSystemTest` covers:

- all three pricing models in one invoice
- period boundary exclusion (`timestamp == end` is not billed)
- multi-user isolation
- expected service subtotals and grand total

## Design choices and trade-offs

- **Why Strategy + SPI?** Keeps pricing logic open for extension and closed for modification.
- **Why in-memory storage?** Matches assignment scope while preserving a clean interface for future DB-backed persistence.
- **Why deterministic ordering?** Makes invoices predictable and testable even with out-of-order ingestion.
- **Why a `Money` value object?** Centralizes safe arithmetic and formatting.

## What to tell recruiters/interviewers

Use these points in your submission message or walkthrough:

- **Extensibility:** pricing is strategy-based + Java SPI (`ServiceLoader`). New billing type = new class + config + SPI entry, no `switch` edits.
- **Config-driven design:** rates/tiers/subscription params live in `billing-config.properties`, not in pricing engine logic.
- **Correctness controls:** `BigDecimal` money model, strict `[start, end)` period filter, deterministic ordering for stable invoices.
- **Modularity:** ingestion (`UsageIngestionService`), pricing (`pricing` package), invoice assembly (`InvoiceService`), storage (`UsageStore`) are decoupled.
- **Proof of extension:** `GraduatedWithCapPricingStrategy` was added without changing `PricingEngine`.

## If asked “what would you do next in production?”

- persist usage/invoices in a database
- add idempotency keys and deduplication safeguards for ingestion
- validate pricing configuration on startup with clearer failure messages
- expose invoice generation via REST/gRPC API
- add observability: structured logs, metrics, audit trail, and reconciliation jobs

## Suggested short submission note

"Implemented a config-driven usage billing engine in Java 17 with pluggable pricing strategies (SPI), covering flat, tiered, and fixed+overage models, plus a fourth extension model to prove open/closed design. Added deterministic invoice generation, period-boundary correctness, safe money arithmetic via BigDecimal, and test coverage for multi-user isolation and boundary cases."



