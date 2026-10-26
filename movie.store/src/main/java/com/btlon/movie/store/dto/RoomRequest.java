package com.btlon.movie.store.dto;

public class RoomRequest {
    private String name;
    private Integer seatCount;
    private Long cinemaId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSeatCount() { return seatCount; }
    public void setSeatCount(Integer seatCount) { this.seatCount = seatCount; }
    public Long getCinemaId() { return cinemaId; }
    public void setCinemaId(Long cinemaId) { this.cinemaId = cinemaId; }
}
