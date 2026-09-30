package com.btlon.movie.store.service;

import com.btlon.movie.store.model.Movie;
import com.btlon.movie.store.model.MovieGenre;
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

    public List<MovieGenre> getAllGenres() {
        return genreRepository.findAllByOrderByNameAsc();
    }

    public Optional<MovieGenre> getGenreWithMovies(Long genreId) {
        return genreRepository.findById(genreId);
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAllByOrderByTitleAsc();
    }

    public Optional<Movie> getMovieById(Long movieId) {
        return movieRepository.findById(movieId);
    }

    @Transactional
    public Optional<Movie> createMovie(Movie movie) {
        if (movie.getGenreId() == null || !genreRepository.existsById(movie.getGenreId())) {
            return Optional.empty();
        }
        movie.setId(null);
        return Optional.of(movieRepository.save(movie));
    }

    @Transactional
    public Optional<Movie> updateMovie(Long id, Movie updatedMovie) {
        if (!movieRepository.existsById(id)
                || updatedMovie.getGenreId() == null
                || !genreRepository.existsById(updatedMovie.getGenreId())) {
            return Optional.empty();
        }
        updatedMovie.setId(id);
        return Optional.of(movieRepository.save(updatedMovie));
    }

    @Transactional
    public boolean deleteMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) return false;
        movieRepository.deleteById(movieId);
        return true;
    }
}
