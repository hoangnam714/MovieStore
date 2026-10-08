package com.btlon.movie.store.repository;

import com.btlon.movie.store.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    List<MovieGenre> findAllByOrderByNameAsc();
}
