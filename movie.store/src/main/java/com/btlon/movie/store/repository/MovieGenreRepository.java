package com.btlon.movie.store.repository;

import com.btlon.movie.store.model.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    List<MovieGenre> findAllByOrderByNameAsc();
}
