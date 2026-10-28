package com.btlon.movie.store.controller;

import com.btlon.movie.store.dto.MovieResponse;
import com.btlon.movie.store.dto.MovieListResponse;
import com.btlon.movie.store.dto.MovieRequest;
import com.btlon.movie.store.service.MovieService;
import com.btlon.movie.store.security.AdminOnly;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public MovieListResponse getAllMovies(@RequestParam(required = false) String title,
                                                          @RequestParam(required = false) Long genreId,
                                                          @RequestParam(required = false) Boolean featured) {
        List<MovieResponse> movies = movieService.getAllMovies(title, genreId, featured);
        return new MovieListResponse(movies, movieService.getFeaturedCount());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return movieService.getMovieById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @AdminOnly
    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(@RequestBody MovieRequest movie) {
        return movieService.createMovie(movie)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
                .orElse(ResponseEntity.badRequest().build());
    }

    @AdminOnly
    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id, @RequestBody MovieRequest movie) {
        return movieService.updateMovie(id, movie).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        return movieService.deleteMovie(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/banners")
    public List<MovieResponse> getBanners() {
        return movieService.getBanners();
    }
}
