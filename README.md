# Credfix Usage-Based Billing System

A Java 17 implementation of a config-driven usage billing engine for cloud-style services.

## Problem Overview

The goal is to build a billing system that can:

- ingest usage events per `(user, resource, service)`
- apply service-specific pricing models
- generate invoices for a billing period `[start, end)`

The design must stay extensible, modular, and correct under real billing constraints (safe money math, out-of-order events, config-driven pricing, and clear separation of concerns).

## Assignment Requirements Coverage

### Functional requirements

- **Usage tracking**: supported via `UsageEvent` and `UsageIngestionService`
- **Pricing models**:
  - `flat_per_unit`
  - `tiered`
  - `fixed_subscription_overage`
- **Invoice generation**:
  - per-resource line items
  - per-service subtotals
  - invoice total
  - strict period filtering `[start, end)`

### Constraints and specifications

1. **Safe money representation**
   - Implemented with `Money` using `BigDecimal`
2. **Config-driven pricing**
   - Loaded from `src/main/resources/billing-config.properties`
3. **Out-of-order events**
   - Store/query flow sorts events deterministically before billing
4. **Swappable persistence layer**
   - `UsageStore` interface + `InMemoryUsageStore` implementation
5. **No external billing libraries**
   - Only Java standard library for billing logic

### Candidate deliverables

- Working implementation with demo driver: `BillingApplication`
- Test demonstrating behavior: `BillingSystemTest`
- Two users + three required services + generated invoice
- Clear separation: ingestion, pricing, invoice assembly

### Evaluation criteria mapping

- **Extensibility**
  - `PricingEngine` resolves strategies via Java SPI (`ServiceLoader`)
  - New billing type = new `PricingStrategy` + SPI registration + config entry
  - No engine `switch` modification
- **Modularity**
  - Pricing logic decoupled from storage (`UsageStore`) and formatting (`InvoiceFormatter`)
- **Config-driven design**
  - Rates/tiers/plan parameters come from runtime config
- **Correctness**
  - Boundary handling with `BillingPeriod.contains` (`start` inclusive, `end` exclusive)
  - Tier calculations done cumulatively and deterministically
  - Currency-safe arithmetic with `BigDecimal`
- **Readability**
  - Small focused classes, explicit domain model, and straightforward service flow

## Architecture

```text
UsageEvent -> UsageIngestionService -> UsageStore
                                      |
                                      v
                           InvoiceService -> PricingEngine -> PricingStrategy (SPI)
                                      |                               |
                                      v                               v
                                   Invoice                    ServicePlan (from config)
```

## Project Structure

- `src/main/java/com/credfix/billing/domain` - billing domain models (`UsageEvent`, `Invoice`, `Money`, ...)
- `src/main/java/com/credfix/billing/store` - usage storage abstraction and in-memory implementation
- `src/main/java/com/credfix/billing/catalog` - runtime pricing catalog from configuration
- `src/main/java/com/credfix/billing/pricing` - pricing strategy contract and implementations
- `src/main/java/com/credfix/billing/service` - ingestion, invoice generation, and formatting services
- `src/main/resources/billing-config.properties` - pricing configuration
- `src/main/resources/META-INF/services/com.credfix.billing.pricing.PricingStrategy` - SPI strategy registry

## Pricing Models Implemented

### 1) Flat per-unit

- Strategy: `FlatPerUnitPricingStrategy`
- Formula: `charge = quantity * rate`

### 2) Tiered

- Strategy: `TieredPricingStrategy`
- Example config: `100:0.10,900:0.08,INF:0.05`
- Tiers are consumed cumulatively across resources for a service in deterministic order

### 3) Fixed subscription + overage

- Strategy: `FixedSubscriptionOveragePricingStrategy`
- Formula:
  - base monthly fee line item
  - overage charge after included quantity is exhausted

### 4) Extensibility demonstration

- Strategy: `GraduatedWithCapPricingStrategy`
- Included to prove the extension model required by the assignment
- Added without modifying `PricingEngine`

## Configuration

Pricing is externally defined in:

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

## How to Run

### Prerequisites

- JDK 17
- Maven 3.8+

### Run tests and demo

```bash
cd /Users/vamsikrishna.majeti/Downloads/credfix-billing
mvn -q test
mvn -q package
java -jar target/credfix-billing-1.0.0.jar
```

### IDE run target

Run `main` in:

- `src/main/java/com/credfix/billing/BillingApplication.java`

## IntelliJ Note (`import java...` red error)

If IntelliJ shows `Cannot resolve symbol 'java'`, this is usually JDK/project setup, not missing Maven dependency.

- open project from `pom.xml`
- set project SDK to JDK 17
- reload Maven project
- invalidate caches if needed

`java.*` packages come from the JDK, not a Maven dependency.

## Test Coverage

`BillingSystemTest` covers:

- all required pricing models in one invoice flow
- period boundary exclusion (`timestamp == endExclusive` is not billed)
- multi-user isolation
- extension path via `graduated_with_cap` strategy

## Design Decisions and Trade-offs

- **Strategy + SPI** was chosen for extension safety and runtime discovery
- **In-memory store** keeps scope aligned to assignment while preserving swapability
- **Deterministic ordering** improves reproducibility and test stability
- **Money wrapper** centralizes precision and formatting behavior
