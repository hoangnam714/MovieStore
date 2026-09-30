package com.btlon.movie.store.repository;

import com.btlon.movie.store.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findAllByOrderByTitleAsc();
}
