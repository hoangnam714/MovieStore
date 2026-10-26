package com.btlon.movie.store.service;

import com.btlon.movie.store.dto.CinemaRequest;
import com.btlon.movie.store.entity.Cinema;
import com.btlon.movie.store.repository.CinemaRepository;
import com.btlon.movie.store.repository.RoomRepository;
import com.btlon.movie.store.web.BadRequestException;
import com.btlon.movie.store.web.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CinemaService {
    private final CinemaRepository cinemas;
    private final RoomRepository rooms;
    public CinemaService(CinemaRepository cinemas, RoomRepository rooms) {
        this.cinemas = cinemas; this.rooms = rooms;
    }
    public List<Cinema> list() { return cinemas.findAll(); }
    @Transactional
    public Cinema create(CinemaRequest request) {
        validate(request);
        return cinemas.save(new Cinema(request.getName().trim(), request.getAddress().trim()));
    }
    @Transactional
    public Optional<Cinema> update(Long id, CinemaRequest request) {
        validate(request);
        return cinemas.findById(id).map(cinema -> {
            cinema.setName(request.getName().trim());
            cinema.setAddress(request.getAddress().trim());
            return cinemas.save(cinema);
        });
    }
    @Transactional
    public boolean delete(Long id) {
        if (!cinemas.existsById(id)) return false;
        if (rooms.existsByCinema_Id(id)) throw new ConflictException("Cannot delete cinema with rooms");
        cinemas.deleteById(id);
        return true;
    }
    private static void validate(CinemaRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()
                || request.getAddress() == null || request.getAddress().isBlank())
            throw new BadRequestException("Cinema name and address are required");
    }
}
