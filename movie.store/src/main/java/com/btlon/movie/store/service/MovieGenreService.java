package com.btlon.movie.store.service;

import com.btlon.movie.store.entity.MovieGenre;
import com.btlon.movie.store.repository.MovieGenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MovieGenreService {
    private final MovieGenreRepository movieGenreRepository;

    public MovieGenreService(MovieGenreRepository movieGenreRepository) {
        this.movieGenreRepository = movieGenreRepository;
    }

    public List<MovieGenre> getAllGenres() {
        return movieGenreRepository.findAllByOrderByNameAsc();
    }

    public Optional<MovieGenre> getGenreWithMovies(Long genreId) {
        return movieGenreRepository.findById(genreId);
    }
}
