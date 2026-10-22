package com.btlon.movie.store.service;

import com.btlon.movie.store.dto.TicketBookRequest;
import com.btlon.movie.store.dto.TicketResponse;
import com.btlon.movie.store.entity.Showtime;
import com.btlon.movie.store.entity.Ticket;
import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.repository.ShowtimeRepository;
import com.btlon.movie.store.repository.TicketRepository;
import com.btlon.movie.store.repository.UserRepository;
import com.btlon.movie.store.web.BadRequestException;
import com.btlon.movie.store.web.ConflictException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository,
                         ShowtimeRepository showtimeRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.showtimeRepository = showtimeRepository;
    }

    public List<TicketResponse> getTicketsForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            return List.of();
        }
        return ticketRepository.findByUser_IdOrderByBookedAtDesc(userId).stream()
                .map(TicketResponse::from)
                .toList();
    }

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAllByOrderByBookedAtDesc().stream()
                .map(TicketResponse::from)
                .toList();
    }

    public List<Integer> getBookedSeats(Long showtimeId) {
        return ticketRepository.findByShowtime_Id(showtimeId).stream()
                .map(Ticket::getSeatNumber)
                .sorted()
                .toList();
    }

    @Transactional
    public List<TicketResponse> bookTickets(TicketBookRequest request) {
        if (request.getUserId() == null || request.getShowtimeId() == null
                || request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
            throw new BadRequestException("userId, showtimeId and seatNumbers are required");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BadRequestException("Unknown user"));
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new BadRequestException("Unknown showtime"));

        if (!showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Cannot book tickets after showtime has started");
        }

        int seatCount = showtime.getRoom().getSeatCount();
        Set<Integer> uniqueSeats = new HashSet<>();
        for (Integer seat : request.getSeatNumbers()) {
            if (seat == null || seat < 1 || seat > seatCount) {
                throw new BadRequestException("Seat must be between 1 and " + seatCount);
            }
            if (!uniqueSeats.add(seat)) {
                throw new BadRequestException("Duplicate seat in request");
            }
        }

        List<Ticket> tickets = new ArrayList<>();
        for (Integer seat : uniqueSeats) {
            Ticket ticket = new Ticket();
            ticket.setUser(user);
            ticket.setShowtime(showtime);
            ticket.setSeatNumber(seat);
            ticket.setPrice(showtime.getTicketPrice());
            tickets.add(ticket);
        }
        try {
            return ticketRepository.saveAll(tickets).stream()
                    .map(TicketResponse::from)
                    .toList();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("One or more seats are already booked for this showtime");
        }
    }
}
