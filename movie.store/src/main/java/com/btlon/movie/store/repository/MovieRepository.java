package com.btlon.movie.store.repository;

import com.btlon.movie.store.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    @EntityGraph(attributePaths = "genre")
    List<Movie> findAllByOrderByTitleAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Movie m WHERE m.featured = true")
    List<Movie> findFeaturedForUpdate();

    List<Movie> findByFeaturedIsNull();

    List<Movie> findByPosterUrl(String posterUrl);

    @EntityGraph(attributePaths = "genre")
    @Query("""
            SELECT m FROM Movie m
            WHERE m.featured = true AND m.posterUrl IS NOT NULL AND m.posterUrl <> ''
              AND EXISTS (SELECT s.id FROM Showtime s WHERE s.movie = m AND s.startTime > :now)
            ORDER BY CASE WHEN m.bannerPosition IS NULL THEN 1 ELSE 0 END,
                     m.bannerPosition DESC,
                     CASE WHEN m.releaseDate IS NULL THEN 1 ELSE 0 END,
                     m.releaseDate DESC, m.id DESC
            """)
    List<Movie> findBanners(LocalDateTime now, Pageable pageable);

    List<Movie> findByGenre_Id(Long genreId);
}
