package com.btlon.movie.store.service;

import com.btlon.movie.store.dto.ShowtimeRequest;
import com.btlon.movie.store.dto.ShowtimeResponse;
import com.btlon.movie.store.dto.ShowtimeScheduleResponse;
import com.btlon.movie.store.dto.CinemaScheduleResponse;
import com.btlon.movie.store.dto.RoomScheduleResponse;
import com.btlon.movie.store.entity.Cinema;
import com.btlon.movie.store.repository.CinemaRepository;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ShowtimeService {
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final TicketRepository ticketRepository;
    private final CinemaRepository cinemaRepository;

    public ShowtimeService(ShowtimeRepository showtimeRepository, MovieRepository movieRepository,
                           RoomRepository roomRepository, TicketRepository ticketRepository,
                           CinemaRepository cinemaRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.ticketRepository = ticketRepository;
        this.cinemaRepository = cinemaRepository;
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

    public ShowtimeScheduleResponse getSchedule(LocalDate date, List<Long> cinemaIds, String keyword) {
        if (date == null) date = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (cinemaIds == null) cinemaIds = new ArrayList<>();
        for (Long id : cinemaIds) {
            if (id == null || id < 0) throw new BadRequestException("Cụm rạp không hợp lệ.");
        }
        String search = getSearchKeyword(keyword);

        // Lấy dữ liệu một lần, không gọi repository bên trong vòng lặp gom nhóm.
        List<Cinema> cinemas = new ArrayList<>(cinemaRepository.findAllByOrderByNameAscIdAsc());
        List<Room> rooms = roomRepository.findAllByOrderByNameAscIdAsc();
        List<Showtime> showtimes = showtimeRepository
                .findByStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAscIdAsc(
                        date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        for (Room room : rooms) {
            if (room.getCinemaId() != null) continue;
            cinemas.add(new Cinema("Chưa phân cụm", ""));
            break;
        }

        List<CinemaScheduleResponse> result = new ArrayList<>();
        int totalShowtimes = 0;
        for (Cinema cinema : cinemas) {
            // ID 0 dùng cho các phòng cũ chưa được gán cụm rạp.
            Long cinemaId = cinema.getId() == null ? 0L : cinema.getId();
            if (!cinemaIds.isEmpty() && !cinemaIds.contains(cinemaId)) continue;

            List<RoomScheduleResponse> roomSchedules = new ArrayList<>();
            int cinemaTotal = 0;
            for (Room room : rooms) {
                if (!Objects.equals(room.getCinemaId(), cinema.getId())) continue;
                String roomKeyword = getSearchKeyword(cinema.getName() + " " + room.getName());
                List<ShowtimeResponse> roomShowtimes = new ArrayList<>();
                for (Showtime showtime : showtimes) {
                    if (!room.getId().equals(showtime.getRoomId())) continue;
                    String text = getSearchKeyword(cinema.getName() + " " + room.getName()
                            + " " + showtime.getMovie().getTitle());
                    if (!text.contains(search)) continue;
                    roomShowtimes.add(ShowtimeResponse.from(showtime));
                }
                // Giữ phòng trống để quản trị viên thấy phòng nào chưa được lên lịch.
                if (!roomKeyword.contains(search) && roomShowtimes.isEmpty()) continue;
                roomSchedules.add(new RoomScheduleResponse(room.getId(), room.getName(),
                        room.getSeatCount(), roomShowtimes));
                cinemaTotal += roomShowtimes.size();
            }
            if (!search.isEmpty() && roomSchedules.isEmpty()) continue;
            result.add(new CinemaScheduleResponse(cinema.getId(), cinema.getName(), cinema.getAddress(),
                    cinemaTotal, roomSchedules));
            totalShowtimes += cinemaTotal;
        }
        return new ShowtimeScheduleResponse(date, totalShowtimes, result);
    }

    private String getSearchKeyword(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
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
        if (ShowtimeSchedule.roomHasOverlap(showtimeRepository.findByRoom_Id(room.get().getId()), null,
                request.getStartTime(), movie.get())) {
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
        if (ShowtimeSchedule.roomHasOverlap(showtimeRepository.findByRoom_Id(room.get().getId()), id,
                request.getStartTime(), movie.get())) {
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
        if (request.getMovieId() == null || request.getRoomId() == null || request.getStartTime() == null
                || request.getTicketPrice() == null) {
            throw new BadRequestException("movieId, roomId, startTime and ticketPrice are required");
        }
        if (request.getTicketPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("ticketPrice must not be negative");
        }
    }
}
