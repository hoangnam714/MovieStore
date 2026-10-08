package com.btlon.movie.store.controller;

import com.btlon.movie.store.entity.MovieGenre;
import com.btlon.movie.store.service.MovieGenreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@CrossOrigin(origins = "*")
public class MovieGenreController {
    private final MovieGenreService movieGenreService;

    public MovieGenreController(MovieGenreService movieGenreService) {
        this.movieGenreService = movieGenreService;
    }

    @GetMapping
    public List<MovieGenre> getAllGenres() {
        return movieGenreService.getAllGenres();
    }

    @GetMapping("/{id}/movies")
    public ResponseEntity<MovieGenre> getGenreMovies(@PathVariable Long id) {
        return movieGenreService.getGenreWithMovies(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
