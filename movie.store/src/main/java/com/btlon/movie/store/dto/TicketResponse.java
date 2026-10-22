package com.btlon.movie.store.dto;

import com.btlon.movie.store.entity.Ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private Long showtimeId;
    private String movieTitle;
    private String roomName;
    private LocalDateTime startTime;
    private Integer seatNumber;
    private BigDecimal price;
    private LocalDateTime bookedAt;

    public static TicketResponse from(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.id = ticket.getId();
        response.userId = ticket.getUserId();
        response.userFullName = ticket.getUser().getFullName();
        response.userEmail = ticket.getUser().getEmail();
        response.showtimeId = ticket.getShowtimeId();
        response.movieTitle = ticket.getShowtime().getMovie().getTitle();
        response.roomName = ticket.getShowtime().getRoom().getName();
        response.startTime = ticket.getShowtime().getStartTime();
        response.seatNumber = ticket.getSeatNumber();
        response.price = ticket.getPrice();
        response.bookedAt = ticket.getBookedAt();
        return response;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUserFullName() { return userFullName; }
    public String getUserEmail() { return userEmail; }
    public Long getShowtimeId() { return showtimeId; }
    public String getMovieTitle() { return movieTitle; }
    public String getRoomName() { return roomName; }
    public LocalDateTime getStartTime() { return startTime; }
    public Integer getSeatNumber() { return seatNumber; }
    public BigDecimal getPrice() { return price; }
    public LocalDateTime getBookedAt() { return bookedAt; }
}
