package com.btlon.movie.store.service;

import com.btlon.movie.store.model.Movie;
import com.btlon.movie.store.model.MovieGenre;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MovieService {
    private final List<MovieGenre> genres = new ArrayList<>();
    private final AtomicLong movieIdCounter = new AtomicLong(1);

    public MovieService() {
        genres.add(new MovieGenre(101L, "Hành động"));
        genres.add(new MovieGenre(102L, "Hoạt hình"));
        genres.add(new MovieGenre(103L, "Kinh dị"));

        createMovie(new Movie(null, "Avengers: Endgame", 181, 2019, 101L));
        createMovie(new Movie(null, "John Wick: Chapter 4", 169, 2023, 101L));
        createMovie(new Movie(null, "Inside Out 2", 96, 2024, 102L));
        createMovie(new Movie(null, "The Conjuring", 112, 2013, 103L));
    }

    private Optional<MovieGenre> findGenreById(Long genreId) {
        return genres.stream().filter(genre -> genre.getId().equals(genreId)).findFirst();
    }

    public List<MovieGenre> getAllGenres() {
        return new ArrayList<>(genres);
    }

    public Optional<MovieGenre> getGenreWithMovies(Long genreId) {
        return findGenreById(genreId);
    }

    public List<Movie> getAllMovies() {
        return genres.stream().flatMap(genre -> genre.getMovies().stream()).toList();
    }

    public Optional<Movie> getMovieById(Long movieId) {
        return getAllMovies().stream().filter(movie -> movie.getId().equals(movieId)).findFirst();
    }

    public Optional<Movie> createMovie(Movie movie) {
        Optional<MovieGenre> genre = findGenreById(movie.getGenreId());
        if (genre.isEmpty()) return Optional.empty();

        movie.setId(movieIdCounter.getAndIncrement());
        genre.get().getMovies().add(movie);
        return Optional.of(movie);
    }

    public Optional<Movie> updateMovie(Long id, Movie updatedMovie) {
        Optional<MovieGenre> targetGenre = findGenreById(updatedMovie.getGenreId());
        if (targetGenre.isEmpty()) return Optional.empty();

        for (MovieGenre genre : genres) {
            List<Movie> movies = genre.getMovies();
            for (int index = 0; index < movies.size(); index++) {
                Movie existingMovie = movies.get(index);
                if (existingMovie.getId().equals(id)) {
                    updatedMovie.setId(id);
                    movies.remove(index);
                    targetGenre.get().getMovies().add(updatedMovie);
                    return Optional.of(updatedMovie);
                }
            }
        }
        return Optional.empty();
    }

    public boolean deleteMovie(Long movieId) {
        return genres.stream()
                .anyMatch(genre -> genre.getMovies().removeIf(movie -> movie.getId().equals(movieId)));
    }
}
