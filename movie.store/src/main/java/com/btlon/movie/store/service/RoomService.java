package com.btlon.movie.store.service;

import com.btlon.movie.store.dto.RoomRequest;
import com.btlon.movie.store.entity.Room;
import com.btlon.movie.store.repository.RoomRepository;
import com.btlon.movie.store.repository.ShowtimeRepository;
import com.btlon.movie.store.repository.TicketRepository;
import com.btlon.movie.store.web.BadRequestException;
import com.btlon.movie.store.web.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RoomService {
    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;

    public RoomService(RoomRepository roomRepository, ShowtimeRepository showtimeRepository,
                       TicketRepository ticketRepository) {
        this.roomRepository = roomRepository;
        this.showtimeRepository = showtimeRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    @Transactional
    public Room createRoom(RoomRequest request) {
        validate(request);
        Room room = new Room(request.getName().trim(), request.getSeatCount());
        return roomRepository.save(room);
    }

    @Transactional
    public Optional<Room> updateRoom(Long id, RoomRequest request) {
        validate(request);
        Optional<Room> existing = roomRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        Room room = existing.get();
        if (request.getSeatCount() < room.getSeatCount()) {
            ensureSeatCountCanShrink(room.getId(), request.getSeatCount());
        }
        room.setName(request.getName().trim());
        room.setSeatCount(request.getSeatCount());
        return Optional.of(roomRepository.save(room));
    }

    private void ensureSeatCountCanShrink(Long roomId, int newSeatCount) {
        for (var showtime : showtimeRepository.findByRoom_Id(roomId)) {
            int maxSeatSold = ticketRepository.findByShowtime_Id(showtime.getId()).stream()
                    .mapToInt(t -> t.getSeatNumber())
                    .max()
                    .orElse(0);
            if (newSeatCount < maxSeatSold) {
                throw new ConflictException("Cannot reduce seat count below sold seats for a showtime in this room");
            }
        }
    }

    @Transactional
    public boolean deleteRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            return false;
        }
        if (showtimeRepository.existsByRoom_Id(roomId)) {
            throw new ConflictException("Cannot delete room with showtimes");
        }
        roomRepository.deleteById(roomId);
        return true;
    }

    private static void validate(RoomRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Room name is required");
        }
        if (request.getSeatCount() == null || request.getSeatCount() <= 0) {
            throw new BadRequestException("seatCount must be greater than 0");
        }
    }
}
