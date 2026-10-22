package com.btlon.movie.store.dto;

import java.util.List;

public class TicketBookRequest {
    private Long userId;
    private Long showtimeId;
    private List<Integer> seatNumbers;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public List<Integer> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<Integer> seatNumbers) { this.seatNumbers = seatNumbers; }
}
