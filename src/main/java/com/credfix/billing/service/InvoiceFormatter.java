package com.credfix.billing.service;

import com.credfix.billing.domain.Invoice;
import com.credfix.billing.domain.InvoiceLineItem;
import com.credfix.billing.domain.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Renders an invoice as plain text for demo output and quick manual validation.
 */
public class InvoiceFormatter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
            .withZone(ZoneOffset.UTC);

    /**
     * Produces a human-readable invoice table with subtotals and total.
     */
    public String format(Invoice invoice) {
        StringBuilder out = new StringBuilder();
        out.append("Invoice for user=").append(invoice.userId()).append('\n');
        out.append("Period: ")
                .append(DATE_FORMATTER.format(invoice.periodStart()))
                .append(" to ")
                .append(DATE_FORMATTER.format(invoice.periodEnd()))
                .append(" [start inclusive, end exclusive]")
                .append('\n')
                .append('\n');

        out.append(String.format("%-12s %-16s %-14s %-12s %-12s %s%n",
                "Service", "Resource", "Quantity", "Unit", "UnitPrice", "Charge"));

        for (InvoiceLineItem item : invoice.lineItems()) {
            out.append(String.format("%-12s %-16s %-14s %-12s %-12s %s%n",
                    item.serviceType(),
                    item.resourceId(),
                    stripZeros(item.quantity()),
                    item.unit(),
                    stripZeros(item.unitPrice()),
                    money(item.charge())
            ));
        }

        out.append("\nSubtotals:\n");
        for (Map.Entry<String, Money> subtotal : invoice.serviceSubtotals().entrySet()) {
            out.append(" - ").append(subtotal.getKey()).append(": ").append(money(subtotal.getValue())).append('\n');
        }

        out.append("\nTOTAL: ").append(money(invoice.total())).append('\n');
        return out.toString();
    }

    private static String stripZeros(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private static String money(Money money) {
        return money.amount().setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

