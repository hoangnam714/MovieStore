package com.btlon.movie.store.service;

import com.btlon.movie.store.entity.Movie;
import com.btlon.movie.store.entity.MovieGenre;
import com.btlon.movie.store.dto.MovieRequest;
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

    public List<Movie> getAllMovies() {
        return movieRepository.findAllByOrderByTitleAsc();
    }

    public Optional<Movie> getMovieById(Long movieId) {
        return movieRepository.findById(movieId);
    }

    @Transactional
    public Optional<Movie> createMovie(MovieRequest request) {
        validate(request);
        if (request.getGenreId() == null) {
            return Optional.empty();
        }
        Optional<MovieGenre> genre = genreRepository.findById(request.getGenreId());
        if (genre.isEmpty()) return Optional.empty();

        Movie movie = new Movie(null, request.getTitle().trim(), request.getDuration(),
                request.getReleaseYear(), genre.get());
        return Optional.of(movieRepository.save(movie));
    }

    @Transactional
    public Optional<Movie> updateMovie(Long id, MovieRequest request) {
        validate(request);
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
        movie.setReleaseYear(request.getReleaseYear());
        movie.setGenre(genre.get());
        return Optional.of(movieRepository.save(movie));
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
        if (request.getReleaseYear() == null) {
            throw new BadRequestException("releaseYear is required");
        }
    }
}
