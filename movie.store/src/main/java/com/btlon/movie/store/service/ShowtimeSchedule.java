package com.btlon.movie.store.service;

import com.btlon.movie.store.entity.Movie;
import com.btlon.movie.store.entity.Showtime;

import java.time.LocalDateTime;
import java.util.List;

final class ShowtimeSchedule {
    private ShowtimeSchedule() {
    }

    static boolean overlaps(LocalDateTime start, int durationMinutes, LocalDateTime otherStart, int otherDurationMinutes) {
        LocalDateTime end = start.plusMinutes(durationMinutes);
        LocalDateTime otherEnd = otherStart.plusMinutes(otherDurationMinutes);
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }

    static boolean roomHasOverlap(List<Showtime> inRoom, Long excludeShowtimeId,
                                  LocalDateTime start, Movie movie) {
        int duration = movie.getDuration();
        for (Showtime existing : inRoom) {
            if (excludeShowtimeId != null && excludeShowtimeId.equals(existing.getId())) {
                continue;
            }
            if (overlaps(start, duration, existing.getStartTime(), existing.getMovie().getDuration())) {
                return true;
            }
        }
        return false;
    }
}
