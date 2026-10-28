package com.btlon.movie.store.repository;

import com.btlon.movie.store.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    // All banner changes take the same database lock, including when no banner exists yet.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM MovieGenre g ORDER BY g.id")
    List<MovieGenre> lockBannerChanges();

    boolean existsByName(String name);

    @EntityGraph(attributePaths = "movies")
    List<MovieGenre> findAllByOrderByNameAsc();

    // Load movies before the transaction ends (open-in-view is disabled).
    @Override
    @EntityGraph(attributePaths = "movies")
    Optional<MovieGenre> findById(Long id);
}
