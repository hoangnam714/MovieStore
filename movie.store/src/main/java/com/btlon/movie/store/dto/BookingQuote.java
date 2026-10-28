package com.btlon.movie.store.dto;

import java.math.BigDecimal;

/**
 * Price breakdown of one booking. Amounts are per-ticket values multiplied by
 * seatCount.
 */
public class BookingQuote {
    private final int seatCount;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;
    private final int memberDiscountPercent;
    private final String discountCode;
    private final int codeDiscountPercent;
    private final BigDecimal discountAmount;
    private final int vatPercent;
    private final BigDecimal vatAmount;
    private final BigDecimal total;
    private final BigDecimal ticketDiscount;
    private final BigDecimal ticketVat;
    private final BigDecimal ticketTotal;

    public BookingQuote(int seatCount, BigDecimal unitPrice, int memberDiscountPercent, String discountCode,
            int codeDiscountPercent, BigDecimal ticketDiscount, int vatPercent, BigDecimal ticketVat) {
        BigDecimal seats = BigDecimal.valueOf(seatCount);
        this.seatCount = seatCount;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(seats);
        this.memberDiscountPercent = memberDiscountPercent;
        this.discountCode = discountCode;
        this.codeDiscountPercent = codeDiscountPercent;
        this.discountAmount = ticketDiscount.multiply(seats);
        this.vatPercent = vatPercent;
        this.vatAmount = ticketVat.multiply(seats);
        this.total = subtotal.subtract(discountAmount).add(vatAmount);
        this.ticketDiscount = ticketDiscount;
        this.ticketVat = ticketVat;
        this.ticketTotal = unitPrice.subtract(ticketDiscount).add(ticketVat);
    }

    public int getSeatCount() {
        return seatCount;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public int getMemberDiscountPercent() {
        return memberDiscountPercent;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public int getCodeDiscountPercent() {
        return codeDiscountPercent;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public int getVatPercent() {
        return vatPercent;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getTicketDiscount() {
        return ticketDiscount;
    }

    public BigDecimal getTicketVat() {
        return ticketVat;
    }

    public BigDecimal getTicketTotal() {
        return ticketTotal;
    }
}
