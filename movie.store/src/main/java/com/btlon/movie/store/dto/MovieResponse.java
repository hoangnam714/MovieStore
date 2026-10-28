package com.btlon.movie.store.dto;

import com.btlon.movie.store.entity.Movie;
import java.time.LocalDate;
import java.util.List;

public record MovieResponse(Long id, String title, Integer duration, LocalDate releaseDate,
        Integer releaseYear, List<GenreResponse> genres, String posterUrl,
        String subtitle, String description, boolean featured, Integer bannerPosition) {
    public static MovieResponse from(Movie movie) {
        List<GenreResponse> genres = movie.getGenres().stream()
                .map(genre -> new GenreResponse(genre.getId(), genre.getName()))
                .toList();
        return new MovieResponse(movie.getId(), movie.getTitle(), movie.getDuration(), movie.getReleaseDate(),
                movie.getReleaseYear(), genres, movie.getPosterUrl(),
                movie.getSubtitle(), movie.getDescription(), movie.isFeatured(), movie.getBannerPosition());
    }
}
