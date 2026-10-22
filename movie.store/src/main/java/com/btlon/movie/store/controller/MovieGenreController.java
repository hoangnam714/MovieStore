package com.btlon.movie.store.controller;

import com.btlon.movie.store.dto.GenreRequest;
import com.btlon.movie.store.entity.MovieGenre;
import com.btlon.movie.store.service.MovieGenreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/{id}")
    public ResponseEntity<MovieGenre> getGenreById(@PathVariable Long id) {
        return movieGenreService.getGenreWithMovies(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MovieGenre> createGenre(@RequestBody GenreRequest request) {
        return movieGenreService.createGenre(request)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
                .orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieGenre> updateGenre(@PathVariable Long id, @RequestBody GenreRequest request) {
        return movieGenreService.updateGenre(id, request).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        return movieGenreService.deleteGenre(id) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
