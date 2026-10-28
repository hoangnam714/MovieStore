package com.btlon.movie.store.repository;

import com.btlon.movie.store.entity.Showtime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    boolean existsByMovie_Id(Long movieId);

    boolean existsByRoom_Id(Long roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Showtime s WHERE s.id = :id")
    Optional<Showtime> findByIdForUpdate(Long id);

    @EntityGraph(attributePaths = "movie")
    List<Showtime> findByRoom_Id(Long roomId);

    @EntityGraph(attributePaths = { "movie", "room" })
    List<Showtime> findByMovie_Id(Long movieId);

    @EntityGraph(attributePaths = { "movie", "room" })
    List<Showtime> findAllByOrderByStartTimeAsc();

    @EntityGraph(attributePaths = { "movie", "room" })
    List<Showtime> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime after);

    @Override
    @EntityGraph(attributePaths = { "movie", "room" })
    Optional<Showtime> findById(Long id);
}
