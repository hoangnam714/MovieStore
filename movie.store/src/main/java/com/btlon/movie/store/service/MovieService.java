package com.btlon.movie.store.service;

import com.btlon.movie.store.entity.Movie;
import com.btlon.movie.store.entity.MovieGenre;
import com.btlon.movie.store.dto.MovieRequest;
import com.btlon.movie.store.dto.MovieResponse;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import com.btlon.movie.store.repository.MovieGenreRepository;
import com.btlon.movie.store.repository.MovieRepository;
import com.btlon.movie.store.repository.ShowtimeRepository;
import com.btlon.movie.store.repository.TicketRepository;
import com.btlon.movie.store.web.BadRequestException;
import com.btlon.movie.store.web.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MovieService {
    public static final int MAX_BANNERS = 5;
    private final MovieGenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;

    public MovieService(MovieGenreRepository genreRepository, MovieRepository movieRepository,
                        ShowtimeRepository showtimeRepository, TicketRepository ticketRepository) {
        this.genreRepository = genreRepository;
        this.movieRepository = movieRepository;
        this.showtimeRepository = showtimeRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAllByOrderByTitleAsc().stream().map(MovieResponse::from).toList();
    }

    public Optional<MovieResponse> getMovieById(Long movieId) {
        return movieRepository.findById(movieId).map(MovieResponse::from);
    }

    public List<MovieResponse> getBanners() {
        return movieRepository.findBanners(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")),
                PageRequest.of(0, MAX_BANNERS)).stream().map(MovieResponse::from).toList();
    }

    @Transactional
    public Optional<MovieResponse> createMovie(MovieRequest request) {
        validate(request);
        genreRepository.lockBannerChanges();
        if (request.getGenreId() == null) {
            return Optional.empty();
        }
        Optional<MovieGenre> genre = genreRepository.findById(request.getGenreId());
        if (genre.isEmpty()) return Optional.empty();

        Movie movie = new Movie(null, request.getTitle().trim(), request.getDuration(),
                request.getReleaseDate().getYear(), genre.get());
        applyDetails(movie, request);
        return Optional.of(MovieResponse.from(movieRepository.save(movie)));
    }

    @Transactional
    public Optional<MovieResponse> updateMovie(Long id, MovieRequest request) {
        validate(request);
        genreRepository.lockBannerChanges();
        Optional<Movie> existing = movieRepository.findById(id);
        if (existing.isEmpty() || request.getGenreId() == null) {
            return Optional.empty();
        }
        Optional<MovieGenre> genre = genreRepository.findById(request.getGenreId());
        if (genre.isEmpty()) return Optional.empty();

        Movie movie = existing.get();
        if (!movie.getDuration().equals(request.getDuration())) {
            ensureDurationChangeAllowed(movie, request.getDuration());
        }
        movie.setTitle(request.getTitle().trim());
        movie.setDuration(request.getDuration());
        movie.setGenre(genre.get());
        applyDetails(movie, request);
        return Optional.of(MovieResponse.from(movieRepository.save(movie)));
    }

    private void applyDetails(Movie movie, MovieRequest request) {
        if (request.isFeatured()) {
            long others = movieRepository.findFeaturedForUpdate().stream()
                    .filter(item -> !item.getId().equals(movie.getId())).count();
            if (others >= MAX_BANNERS)
                throw new ConflictException("Chỉ được bật tối đa 5 phim trên banner. Hãy tắt một phim trước.");
        }
        movie.setReleaseDate(request.getReleaseDate());
        movie.setPosterUrl(trimToNull(request.getPosterUrl()));
        movie.setSubtitle(trimToNull(request.getSubtitle()));
        movie.setDescription(trimToNull(request.getDescription()));
        movie.setFeatured(request.isFeatured());
        movie.setBannerPosition(request.getBannerPosition());
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void ensureDurationChangeAllowed(Movie movie, int newDuration) {
        for (var showtime : showtimeRepository.findByMovie_Id(movie.getId())) {
            if (ticketRepository.existsByShowtime_Id(showtime.getId()) && newDuration < movie.getDuration()) {
                throw new ConflictException("Cannot shorten duration when tickets exist for a showtime");
            }
            Movie probe = new Movie(movie.getId(), movie.getTitle(), newDuration,
                    movie.getReleaseYear(), movie.getGenre());
            if (ShowtimeSchedule.roomHasOverlap(
                    showtimeRepository.findByRoom_Id(showtime.getRoom().getId()),
                    showtime.getId(),
                    showtime.getStartTime(),
                    probe)) {
                throw new ConflictException("New duration would overlap another showtime in the same room");
            }
        }
    }

    @Transactional
    public boolean deleteMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) return false;
        if (showtimeRepository.existsByMovie_Id(movieId)) {
            throw new ConflictException("Cannot delete movie with showtimes");
        }
        movieRepository.deleteById(movieId);
        return true;
    }

    private static void validate(MovieRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("title is required");
        }
        if (request.getDuration() == null || request.getDuration() <= 0) {
            throw new BadRequestException("duration must be greater than 0");
        }
        if (request.getReleaseDate() == null) {
            throw new BadRequestException("Vui lòng nhập ngày phát hành chính xác.");
        }
        if (request.getTitle().trim().length() > 150)
            throw new BadRequestException("Tên phim tối đa 150 ký tự.");
        if (request.getSubtitle() != null && request.getSubtitle().length() > 100)
            throw new BadRequestException("Tiêu đề phụ tối đa 100 ký tự.");
        if (request.getDescription() != null && request.getDescription().length() > 500)
            throw new BadRequestException("Mô tả tối đa 500 ký tự.");
        if (request.getBannerPosition() != null
                && (request.getBannerPosition() < 0 || request.getBannerPosition() > 99))
            throw new BadRequestException("Độ ưu tiên banner phải từ 0 đến 99.");
        String poster = trimToNull(request.getPosterUrl());
        if (request.isFeatured() && poster == null)
            throw new BadRequestException("Phim hiển thị trên banner phải có poster.");
        if (poster != null
                && (poster.length() > 1000 || !(poster.matches("/api/uploads/images/[a-f0-9-]{36}\\.(jpg|png|webp)")
                        || poster.startsWith("https://"))))
            throw new BadRequestException("Đường dẫn poster không hợp lệ.");
    }
}
