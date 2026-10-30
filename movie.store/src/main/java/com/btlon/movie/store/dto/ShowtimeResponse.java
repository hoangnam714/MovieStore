package com.btlon.movie.store.dto;

import com.btlon.movie.store.entity.Showtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowtimeResponse {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private Integer movieDuration;
    private Long roomId;
    private String roomName;
    private Integer seatCount;
    private LocalDateTime startTime;
    private BigDecimal ticketPrice;

    public static ShowtimeResponse from(Showtime showtime) {
        ShowtimeResponse response = new ShowtimeResponse();
        response.id = showtime.getId();
        response.movieId = showtime.getMovieId();
        response.movieTitle = showtime.getMovie().getTitle();
        response.movieDuration = showtime.getMovie().getDuration();
        response.roomId = showtime.getRoomId();
        response.roomName = showtime.getRoom().getName();
        response.seatCount = showtime.getRoom().getSeatCount();
        response.startTime = showtime.getStartTime();
        response.ticketPrice = showtime.getTicketPrice();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public Integer getMovieDuration() {
        return movieDuration;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }
}
