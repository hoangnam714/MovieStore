package com.btlon.movie.store.service;

import com.btlon.movie.store.dto.GenreRequest;
import com.btlon.movie.store.entity.MovieGenre;
import com.btlon.movie.store.repository.MovieGenreRepository;
import com.btlon.movie.store.repository.MovieRepository;
import com.btlon.movie.store.web.BadRequestException;
import com.btlon.movie.store.web.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MovieGenreService {
    private final MovieGenreRepository movieGenreRepository;
    private final MovieRepository movieRepository;

    public MovieGenreService(MovieGenreRepository movieGenreRepository, MovieRepository movieRepository) {
        this.movieGenreRepository = movieGenreRepository;
        this.movieRepository = movieRepository;
    }

    public List<MovieGenre> getAllGenres() {
        return movieGenreRepository.findAllByOrderByNameAsc();
    }

    public Optional<MovieGenre> getGenreWithMovies(Long genreId) {
        return movieGenreRepository.findById(genreId);
    }

    @Transactional
    public Optional<MovieGenre> createGenre(GenreRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Genre name is required");
        }
        String name = request.getName().trim();
        if (movieGenreRepository.existsByName(name)) {
            throw new ConflictException("Genre name already exists");
        }
        return Optional.of(movieGenreRepository.save(new MovieGenre(null, name)));
    }

    @Transactional
    public Optional<MovieGenre> updateGenre(Long id, GenreRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Genre name is required");
        }
        Optional<MovieGenre> existing = movieGenreRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        String name = request.getName().trim();
        if (movieGenreRepository.existsByName(name) && !existing.get().getName().equals(name)) {
            throw new ConflictException("Genre name already exists");
        }
        existing.get().setName(name);
        return Optional.of(movieGenreRepository.save(existing.get()));
    }

    @Transactional
    public boolean deleteGenre(Long id) {
        if (!movieGenreRepository.existsById(id)) {
            return false;
        }
        if (!movieRepository.findByGenres_Id(id).isEmpty()) {
            throw new ConflictException("Cannot delete genre with movies");
        }
        movieGenreRepository.deleteById(id);
        return true;
    }
}
