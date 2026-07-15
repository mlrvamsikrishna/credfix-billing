# Submission Guide

## 1) GitHub repo setup

```bash
cd /Users/vamsikrishna.majeti/Downloads/credfix-billing
git init
git add .
git commit -m "Implement usage-based billing system"
git branch -M main
git remote add origin <your-repo-url>
git push -u origin main
```

Optional `.gitignore` if not already added:

```gitignore
target/
.idea/
*.iml
```

## 2) What to include in assignment submission

- Link to the GitHub repository
- 3-5 line architecture summary
- Run steps and expected output
- Mention key design choices and trade-offs
- Mention what you would do next in production

## 3) Ready-to-paste submission answer (short version)

Use this for a job portal text box or short email note:

> Hi Team,  
> Please find my submission here: `<YOUR_GITHUB_REPO_LINK>`.  
> I implemented a config-driven usage-based billing engine in Java 17 with clear separation between usage ingestion, pricing strategies, invoice assembly, and storage. The solution supports flat per-unit, tiered, and fixed subscription + overage pricing, and also includes a fourth pricing model to demonstrate extensibility without changes to the core pricing engine. Money is handled safely using `BigDecimal`, billing periods follow strict `[start, end)` boundaries, and tests cover multi-user isolation and pricing correctness.  
> Thank you for your time — I’d be happy to walk through the design decisions if helpful.

## 4) Ready-to-paste submission answer (long version)

Use this if the portal allows a fuller explanation:

> Hi Team,  
> Please find my assignment submission here: `<YOUR_GITHUB_REPO_LINK>`.  
>  
> I designed the solution as a modular Java 17 billing system with four clearly separated parts: usage ingestion, pricing strategy resolution, invoice assembly, and usage storage. Pricing is fully configuration-driven through `billing-config.properties`, so service rates and tiers can be changed without modifying the pricing engine. To satisfy the extensibility requirement, I used a `PricingStrategy` abstraction with Java SPI (`ServiceLoader`) so that adding a new billing model only requires a new strategy implementation, an SPI registration entry, and configuration updates — with no changes to existing pricing code.  
>  
> The implementation covers the three required pricing models: flat per-unit, tiered, and fixed subscription + overage. I also added a fourth model (`graduated_with_cap`) as a proof point for the open/closed design requirement. For correctness, money is represented using `BigDecimal`, usage events are processed deterministically even if they arrive out of order, and invoice generation strictly honors the `[start, end)` billing period rule. The demo and tests include two users, multiple services, period-boundary validation, and invoice subtotal/total verification.  
>  
> I also documented the architecture, run steps, and design trade-offs in the repository README for easy review. Thank you for the opportunity.

## 5) Ready-to-use architecture summary

"This solution separates usage ingestion, pricing strategies, invoice assembly, and storage behind interfaces. Pricing is config-driven and strategy-based using Java SPI (`ServiceLoader`) so new billing models can be added without modifying existing engine code. Money uses `BigDecimal` to avoid floating-point issues. Invoices respect strict `[start, end)` period filtering and deterministic ordering for stable outputs."

## 6) Ready-to-use production follow-ups

- Persist events and invoices in a database
- Add idempotency keys and deduplication for ingestion
- Add validation schemas for pricing config
- Add contract tests for each pricing strategy
- Add API layer and auth

## 7) If interviewer asks "how did you ensure extensibility?"

Answer:

"I implemented `PricingStrategy` and used Java SPI discovery in `PricingEngine`. Each strategy declares its billing type and is loaded at runtime. To add a new model, I only add a new strategy class + one line in `META-INF/services` + config entries. No existing strategy or engine code changes are required."

## 8) If interviewer asks "how do I run it?"

Answer:

> The main entry point is `BillingApplication`. From the project root, run:

```bash
cd /Users/vamsikrishna.majeti/Downloads/credfix-billing
mvn -q test
mvn -q package
java -jar target/credfix-billing-1.0.0.jar
```

> In IntelliJ, open the project from `pom.xml`, ensure the Project SDK is JDK 17, then run the `main` method in `BillingApplication`.

## 9) If interviewer asks "why was IntelliJ showing `Cannot resolve symbol 'java'`?"

Answer:

> That issue is typically caused by IntelliJ not attaching the correct JDK or not importing the Maven project correctly. `java.*` packages come from the JDK, not from Maven dependencies, so adding a Maven dependency would not fix it. The correct fix is to set the Project SDK to JDK 17, reload the Maven project, and if needed invalidate IntelliJ caches.

## 10) Suggested GitHub repo description

> Config-driven Java 17 usage-based billing engine with pluggable pricing strategies, deterministic invoice generation, and BigDecimal money handling.

## 11) Suggested email subject line

- `Assignment Submission - Usage-Based Billing System`
- `Credfix Assignment Submission - Java Billing Engine`

## 12) Final review checklist before submitting

- Replace `<YOUR_GITHUB_REPO_LINK>` in the message templates
- Ensure the repo is public or accessible to reviewers
- Confirm `README.md` renders correctly on GitHub
- Run tests once more before pushing
- Keep the submission message concise and confident

