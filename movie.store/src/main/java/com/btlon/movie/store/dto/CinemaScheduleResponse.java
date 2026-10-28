package com.btlon.movie.store.dto;

import java.util.List;

public record CinemaScheduleResponse(Long id, String name, String address,
        int totalShowtimes, List<RoomScheduleResponse> rooms) {
}
