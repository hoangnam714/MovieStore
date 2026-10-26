package com.btlon.movie.store.service;

import com.btlon.movie.store.dto.BookingQuote;
import com.btlon.movie.store.entity.DiscountCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Per-ticket pricing: discount = price x (member % + code %), VAT on the discounted price.
 * Amounts are rounded to whole VND so stored tickets add up exactly to the booking total.
 */
@Component
public class TicketPricing {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private final int memberDiscountPercent;
    private final int vatPercent;

    public TicketPricing(@Value("${moviestore.pricing.member-discount-percent:5}") int memberDiscountPercent,
                         @Value("${moviestore.pricing.vat-percent:10}") int vatPercent) {
        this.memberDiscountPercent = memberDiscountPercent;
        this.vatPercent = vatPercent;
    }

    public BookingQuote quote(BigDecimal unitPrice, int seatCount, boolean member, DiscountCode code) {
        int memberPercent = member ? memberDiscountPercent : 0;
        int codePercent = code == null ? 0 : code.getDiscountPercent();
        int totalPercent = Math.min(100, memberPercent + codePercent);
        BigDecimal discount = percentOf(unitPrice, totalPercent);
        BigDecimal vat = percentOf(unitPrice.subtract(discount), vatPercent);
        return new BookingQuote(seatCount, unitPrice, memberPercent, code == null ? null : code.getCode(),
                codePercent, discount, vatPercent, vat);
    }

    private static BigDecimal percentOf(BigDecimal amount, int percent) {
        return amount.multiply(BigDecimal.valueOf(percent)).divide(HUNDRED, 0, RoundingMode.HALF_UP);
    }
}
