package com.credfix.billing.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable money value backed by BigDecimal to avoid floating-point errors.
 */
public final class Money {
    public static final int SCALE = 6;
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount is required");
        return new Money(amount);
    }

    public static Money of(String amount) {
        return of(new BigDecimal(amount));
    }

    public Money plus(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor));
    }

    public BigDecimal amount() {
        return amount;
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}

