package com.btlon.movie.store.repository;

import com.btlon.movie.store.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {
    List<Cinema> findAllByOrderByNameAscIdAsc();
}
