package com.btlon.movie.store.repository;

import com.btlon.movie.store.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    @EntityGraph(attributePaths = "genre")
    List<Movie> findAllByOrderByTitleAscIdAsc();

    long countByFeaturedTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Movie> findByFeaturedTrue();

    List<Movie> findByFeaturedIsNull();

    List<Movie> findByPosterUrl(String posterUrl);

    List<Movie> findByGenre_Id(Long genreId);
}
