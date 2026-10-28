package com.btlon.movie.store.repository;

import com.btlon.movie.store.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @EntityGraph(attributePaths = "cinema")
    List<Room> findAllByOrderByNameAscIdAsc();

    boolean existsByCinema_Id(Long cinemaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdForUpdate(Long id);
}
