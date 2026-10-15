package com.btlon.movie.store.service;

import com.btlon.movie.store.entity.Movie;
import com.btlon.movie.store.entity.MovieGenre;
import com.btlon.movie.store.dto.MovieRequest;
import com.btlon.movie.store.repository.MovieGenreRepository;
import com.btlon.movie.store.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MovieService {
    private final MovieGenreRepository genreRepository;
    private final MovieRepository movieRepository;

    public MovieService(MovieGenreRepository genreRepository, MovieRepository movieRepository) {
        this.genreRepository = genreRepository;
        this.movieRepository = movieRepository;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAllByOrderByTitleAsc();
    }

    public Optional<Movie> getMovieById(Long movieId) {
        return movieRepository.findById(movieId);
    }

    @Transactional
    public Optional<Movie> createMovie(MovieRequest request) {
        if (request.getGenreId() == null) {
            return Optional.empty();
        }
        Optional<MovieGenre> genre = genreRepository.findById(request.getGenreId());
        if (genre.isEmpty()) return Optional.empty();

        Movie movie = new Movie(null, request.getTitle(), request.getDuration(),
                request.getReleaseYear(), genre.get());
        return Optional.of(movieRepository.save(movie));
    }

    @Transactional
    public Optional<Movie> updateMovie(Long id, MovieRequest request) {
        Optional<Movie> existing = movieRepository.findById(id);
        if (existing.isEmpty() || request.getGenreId() == null) {
            return Optional.empty();
        }
        Optional<MovieGenre> genre = genreRepository.findById(request.getGenreId());
        if (genre.isEmpty()) return Optional.empty();

        Movie movie = existing.get();
        movie.setTitle(request.getTitle());
        movie.setDuration(request.getDuration());
        movie.setReleaseYear(request.getReleaseYear());
        movie.setGenre(genre.get());
        return Optional.of(movieRepository.save(movie));
    }

    @Transactional
    public boolean deleteMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) return false;
        movieRepository.deleteById(movieId);
        return true;
    }
}
