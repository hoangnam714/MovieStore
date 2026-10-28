package com.btlon.movie.store.dto;

import java.util.List;

public record RoomScheduleResponse(Long id, String name, Integer seatCount, List<ShowtimeResponse> showtimes) {
}
