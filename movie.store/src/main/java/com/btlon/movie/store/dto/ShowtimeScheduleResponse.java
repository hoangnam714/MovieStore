package com.btlon.movie.store.dto;

import java.time.LocalDate;
import java.util.List;

public record ShowtimeScheduleResponse(LocalDate date, int totalShowtimes, List<CinemaScheduleResponse> cinemas) {
}
