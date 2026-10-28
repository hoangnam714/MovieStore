package com.btlon.movie.store.repository;

import com.btlon.movie.store.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {
}
