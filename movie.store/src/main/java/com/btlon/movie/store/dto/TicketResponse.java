package com.btlon.movie.store.dto;

import com.btlon.movie.store.entity.Ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TicketResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String customerPhone;
    private Long showtimeId;
    private String movieTitle;
    private String roomName;
    private Long cinemaId;
    private String cinemaName;
    private LocalDateTime startTime;
    private Integer seatNumber;
    private BigDecimal price;
    private String discountCode;
    private BigDecimal discountAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;
    private LocalDateTime bookedAt;

    public static TicketResponse from(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.id = ticket.getId();
        response.userId = ticket.getUserId();
        if (ticket.getUser() != null) {
            response.userFullName = ticket.getUser().getFullName();
            response.userEmail = ticket.getUser().getEmail();
        }
        response.customerPhone = ticket.getCustomerPhone();
        response.showtimeId = ticket.getShowtimeId();
        response.movieTitle = ticket.getShowtime().getMovie().getTitle();
        response.roomName = ticket.getShowtime().getRoom().getName();
        response.cinemaId = ticket.getShowtime().getRoom().getCinemaId();
        response.cinemaName = ticket.getShowtime().getRoom().getCinemaName();
        response.startTime = ticket.getShowtime().getStartTime();
        response.seatNumber = ticket.getSeatNumber();
        response.price = ticket.getPrice();
        response.discountCode = ticket.getDiscountCode() == null ? null : ticket.getDiscountCode().getCode();
        response.discountAmount = ticket.getDiscountAmount();
        response.vatAmount = ticket.getVatAmount();
        response.totalAmount = ticket.getTotalAmount();
        response.bookedAt = ticket.getBookedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public boolean isGuest() {
        return userId == null;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getRoomName() {
        return roomName;
    }

    public Long getCinemaId() {
        return cinemaId;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }
}
