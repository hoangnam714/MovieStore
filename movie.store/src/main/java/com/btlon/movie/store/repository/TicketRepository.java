package com.btlon.movie.store.repository;

import com.btlon.movie.store.entity.Ticket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    boolean existsByShowtime_Id(Long showtimeId);
    boolean existsByUser_Id(Long userId);
    boolean existsByDiscountCode_Id(Long discountCodeId);

    @EntityGraph(attributePaths = {"showtime", "showtime.movie", "showtime.room", "user", "discountCode"})
    List<Ticket> findByUser_IdOrderByBookedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"showtime", "showtime.movie", "showtime.room", "user", "discountCode"})
    List<Ticket> findAllByOrderByBookedAtDesc();

    List<Ticket> findByShowtime_Id(Long showtimeId);
}
