package com.btlon.movie.store.service;

import com.btlon.movie.store.dto.ShowtimeRequest;
import com.btlon.movie.store.dto.ShowtimeResponse;
import com.btlon.movie.store.entity.Movie;
import com.btlon.movie.store.entity.Room;
import com.btlon.movie.store.entity.Showtime;
import com.btlon.movie.store.repository.MovieRepository;
import com.btlon.movie.store.repository.RoomRepository;
import com.btlon.movie.store.repository.ShowtimeRepository;
import com.btlon.movie.store.repository.TicketRepository;
import com.btlon.movie.store.web.BadRequestException;
import com.btlon.movie.store.web.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ShowtimeService {
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final TicketRepository ticketRepository;

    public ShowtimeService(ShowtimeRepository showtimeRepository, MovieRepository movieRepository,
                           RoomRepository roomRepository, TicketRepository ticketRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<ShowtimeResponse> getAllShowtimes() {
        return showtimeRepository.findAllByOrderByStartTimeAsc().stream()
                .map(ShowtimeResponse::from)
                .toList();
    }

    public List<ShowtimeResponse> getUpcomingShowtimes() {
        return showtimeRepository.findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now()).stream()
                .map(ShowtimeResponse::from)
                .toList();
    }

    public Optional<ShowtimeResponse> getShowtimeById(Long id) {
        return showtimeRepository.findById(id).map(ShowtimeResponse::from);
    }

    @Transactional
    public Optional<ShowtimeResponse> createShowtime(ShowtimeRequest request) {
        validate(request);
        Optional<Movie> movie = movieRepository.findById(request.getMovieId());
        if (movie.isEmpty()) {
            return Optional.empty();
        }
        Optional<Room> room = roomRepository.findByIdForUpdate(request.getRoomId());
        if (room.isEmpty()) {
            return Optional.empty();
        }
        if (ShowtimeSchedule.roomHasOverlap(
                showtimeRepository.findByRoom_Id(room.get().getId()),
                null,
                request.getStartTime(),
                movie.get())) {
            throw new ConflictException("Showtime overlaps another in the same room");
        }
        Showtime showtime = new Showtime();
        showtime.setMovie(movie.get());
        showtime.setRoom(room.get());
        showtime.setStartTime(request.getStartTime());
        showtime.setTicketPrice(request.getTicketPrice());
        return Optional.of(ShowtimeResponse.from(showtimeRepository.save(showtime)));
    }

    @Transactional
    public Optional<ShowtimeResponse> updateShowtime(Long id, ShowtimeRequest request) {
        validate(request);
        Optional<Showtime> existing = showtimeRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        Showtime showtime = existing.get();
        if (ticketRepository.existsByShowtime_Id(id)) {
            if (!showtime.getMovie().getId().equals(request.getMovieId())
                    || !showtime.getRoom().getId().equals(request.getRoomId())
                    || !showtime.getStartTime().equals(request.getStartTime())) {
                throw new ConflictException("Cannot change movie, room or start time when tickets exist");
            }
            showtime.setTicketPrice(request.getTicketPrice());
            return Optional.of(ShowtimeResponse.from(showtimeRepository.save(showtime)));
        }
        Optional<Movie> movie = movieRepository.findById(request.getMovieId());
        if (movie.isEmpty()) {
            return Optional.empty();
        }
        Optional<Room> room = roomRepository.findByIdForUpdate(request.getRoomId());
        if (room.isEmpty()) {
            return Optional.empty();
        }
        if (ShowtimeSchedule.roomHasOverlap(
                showtimeRepository.findByRoom_Id(room.get().getId()),
                id,
                request.getStartTime(),
                movie.get())) {
            throw new ConflictException("Showtime overlaps another in the same room");
        }
        showtime.setMovie(movie.get());
        showtime.setRoom(room.get());
        showtime.setStartTime(request.getStartTime());
        showtime.setTicketPrice(request.getTicketPrice());
        return Optional.of(ShowtimeResponse.from(showtimeRepository.save(showtime)));
    }

    @Transactional
    public boolean deleteShowtime(Long id) {
        if (!showtimeRepository.existsById(id)) {
            return false;
        }
        if (ticketRepository.existsByShowtime_Id(id)) {
            throw new ConflictException("Cannot delete showtime with tickets");
        }
        showtimeRepository.deleteById(id);
        return true;
    }

    private static void validate(ShowtimeRequest request) {
        if (request.getMovieId() == null || request.getRoomId() == null
                || request.getStartTime() == null || request.getTicketPrice() == null) {
            throw new BadRequestException("movieId, roomId, startTime and ticketPrice are required");
        }
        if (request.getTicketPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("ticketPrice must not be negative");
        }
    }
}
