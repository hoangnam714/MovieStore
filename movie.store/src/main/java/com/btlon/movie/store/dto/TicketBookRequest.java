package com.btlon.movie.store.dto;

import java.util.List;

public class TicketBookRequest {
    /** Required for guests; the customer account comes from the bearer token, never from the body. */
    private String customerPhone;
    private String discountCode;
    private Long showtimeId;
    private List<Integer> seatNumbers;

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public List<Integer> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<Integer> seatNumbers) { this.seatNumbers = seatNumbers; }
}
