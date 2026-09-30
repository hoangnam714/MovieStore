package com.btlon.movie.store.config;

import com.btlon.movie.store.model.Movie;
import com.btlon.movie.store.model.MovieGenre;
import com.btlon.movie.store.repository.MovieGenreRepository;
import com.btlon.movie.store.repository.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseInitializer {
    @Bean
    CommandLineRunner seedDatabase(MovieGenreRepository genreRepository, MovieRepository movieRepository) {
        return args -> {
            if (genreRepository.count() > 0) return;

            MovieGenre action = genreRepository.save(new MovieGenre(null, "Hành động"));
            MovieGenre animation = genreRepository.save(new MovieGenre(null, "Hoạt hình"));
            MovieGenre horror = genreRepository.save(new MovieGenre(null, "Kinh dị"));

            movieRepository.save(new Movie(null, "Avengers: Endgame", 181, 2019, action.getId()));
            movieRepository.save(new Movie(null, "John Wick: Chapter 4", 169, 2023, action.getId()));
            movieRepository.save(new Movie(null, "Inside Out 2", 96, 2024, animation.getId()));
            movieRepository.save(new Movie(null, "The Conjuring", 112, 2013, horror.getId()));
        };
    }
}
