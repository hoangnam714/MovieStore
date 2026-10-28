package com.btlon.movie.store.dto;

import com.btlon.movie.store.entity.Movie;
import java.time.LocalDate;

public record MovieResponse(Long id, String title, Integer duration, LocalDate releaseDate,
        Integer releaseYear, Long genreId, String genreName, String posterUrl,
        String subtitle, String description, boolean featured, Integer bannerPosition) {
    public static MovieResponse from(Movie movie) {
        return new MovieResponse(movie.getId(), movie.getTitle(), movie.getDuration(), movie.getReleaseDate(),
                movie.getReleaseYear(), movie.getGenreId(), movie.getGenre().getName(), movie.getPosterUrl(),
                movie.getSubtitle(), movie.getDescription(), movie.isFeatured(), movie.getBannerPosition());
    }
}
