package com.btlon.movie.store.controller;

import com.btlon.movie.store.model.MovieGenre;
import com.btlon.movie.store.service.MovieService;
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
    private final MovieService service;

    public MovieGenreController(MovieService service) {
        this.service = service;
    }

    @GetMapping
    public List<MovieGenre> getAllGenres() {
        return service.getAllGenres();
    }

    @GetMapping("/{id}/movies")
    public ResponseEntity<MovieGenre> getGenreMovies(@PathVariable Long id) {
        return service.getGenreWithMovies(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
