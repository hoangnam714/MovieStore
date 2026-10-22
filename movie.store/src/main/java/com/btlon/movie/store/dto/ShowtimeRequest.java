package com.btlon.movie.store.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowtimeRequest {
    private Long movieId;
    private Long roomId;
    private LocalDateTime startTime;
    private BigDecimal ticketPrice;

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public BigDecimal getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }
}
