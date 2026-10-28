package com.btlon.movie.store.service;

import com.btlon.movie.store.dto.BookingQuote;
import com.btlon.movie.store.dto.BookingResponse;
import com.btlon.movie.store.dto.TicketBookRequest;
import com.btlon.movie.store.dto.TicketResponse;
import com.btlon.movie.store.entity.DiscountCode;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class TicketService {
    private static final int SEATS_PER_ROW = 10;
    private static final int MAX_SEATS_PER_BOOKING = 8;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final DiscountCodeService discountCodeService;
    private final TicketPricing ticketPricing;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository,
                         ShowtimeRepository showtimeRepository, DiscountCodeService discountCodeService,
                         TicketPricing ticketPricing) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.showtimeRepository = showtimeRepository;
        this.discountCodeService = discountCodeService;
        this.ticketPricing = ticketPricing;
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

    /**
     * Price preview for the booking page; seat availability is checked only when
     * booking.
     */
    public BookingQuote quote(TicketBookRequest request, User user) {
        if (request == null || request.getShowtimeId() == null
                || request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
            throw new BadRequestException("showtimeId and seatNumbers are required");
        }
        if (request.getSeatNumbers().size() > MAX_SEATS_PER_BOOKING) {
            throw new BadRequestException("Maximum 8 seats per booking");
        }
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new BadRequestException("Unknown showtime"));
        return ticketPricing.quote(showtime.getTicketPrice(), request.getSeatNumbers().size(), user != null,
                discountCodeService.findUsable(request.getDiscountCode()));
    }

    @Transactional
    /** @param user authenticated customer, or null for a guest booking */
    public BookingResponse bookTickets(TicketBookRequest request, User user) {
        if (request == null || request.getShowtimeId() == null
                || request.getSeatNumbers() == null || request.getSeatNumbers().isEmpty()) {
            throw new BadRequestException("showtimeId and seatNumbers are required");
        }
        String phone = PhoneNumbers.normalize(request.getCustomerPhone());
        if (phone == null && user != null) phone = user.getPhone();
        if (user == null && phone == null) {
            throw new BadRequestException("Phone number is required when booking without an account");
        }
        DiscountCode discountCode = discountCodeService.findUsable(request.getDiscountCode());
        Showtime showtime = showtimeRepository.findByIdForUpdate(request.getShowtimeId())
                .orElseThrow(() -> new BadRequestException("Unknown showtime"));

        if (!showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Cannot book tickets after showtime has started");
        }

        int seatCount = showtime.getRoom().getSeatCount();
        Set<Integer> uniqueSeats = new LinkedHashSet<>();
        if (request.getSeatNumbers().size() > MAX_SEATS_PER_BOOKING)
            throw new BadRequestException("Maximum 8 seats per booking");
        for (Integer seat : request.getSeatNumbers()) {
            if (seat == null || seat < 1 || seat > seatCount) {
                throw new BadRequestException("Seat must be between 1 and " + seatCount);
            }
            if (!uniqueSeats.add(seat)) {
                throw new BadRequestException("Duplicate seat in request");
            }
        }
        if (!seatsAreConnected(uniqueSeats, seatCount)) {
            throw new BadRequestException("Seats in one booking must connect horizontally or vertically");
        }
        Set<Integer> booked = new HashSet<>(getBookedSeats(showtime.getId()));
        if (uniqueSeats.stream().anyMatch(booked::contains))
            throw new ConflictException("One or more seats are already booked for this showtime");

        if (discountCode != null) discountCodeService.consume(discountCode);
        BookingQuote pricing = ticketPricing.quote(showtime.getTicketPrice(), uniqueSeats.size(), user != null,
                discountCode);

        List<Ticket> tickets = new ArrayList<>();
        for (Integer seat : uniqueSeats) {
            Ticket ticket = new Ticket();
            ticket.setUser(user);
            ticket.setCustomerPhone(phone);
            ticket.setShowtime(showtime);
            ticket.setSeatNumber(seat);
            ticket.setPrice(showtime.getTicketPrice());
            ticket.setDiscountCode(discountCode);
            ticket.setDiscountAmount(pricing.getTicketDiscount());
            ticket.setVatAmount(pricing.getTicketVat());
            ticket.setTotalAmount(pricing.getTicketTotal());
            tickets.add(ticket);
        }
        try {
            List<TicketResponse> saved = ticketRepository.saveAllAndFlush(tickets).stream()
                    .map(TicketResponse::from)
                    .toList();
            return new BookingResponse(pricing, saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("One or more seats are already booked for this showtime");
        }
    }

    private static boolean seatsAreConnected(Set<Integer> seats, int seatCount) {
        if (seats.size() <= 1) return true;
        Set<Integer> visited = new HashSet<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        Integer first = seats.iterator().next();
        visited.add(first);
        queue.add(first);
        while (!queue.isEmpty()) {
            int seat = queue.removeFirst();
            for (int neighbor : new int[] { seat - 1, seat + 1, seat - SEATS_PER_ROW, seat + SEATS_PER_ROW }) {
                if (neighbor < 1 || neighbor > seatCount) continue;
                if (Math.abs(neighbor - seat) == 1
                        && (neighbor - 1) / SEATS_PER_ROW != (seat - 1) / SEATS_PER_ROW)
                    continue;
                if (seats.contains(neighbor) && visited.add(neighbor)) queue.addLast(neighbor);
            }
        }
        return visited.size() == seats.size();
    }

    @Transactional
    public boolean cancelTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket == null) return false;
        showtimeRepository.findByIdForUpdate(ticket.getShowtimeId());
        if (!ticket.getShowtime().getStartTime().isAfter(LocalDateTime.now()))
            throw new ConflictException("Cannot cancel a ticket after showtime has started");
        ticketRepository.delete(ticket);
        ticketRepository.flush();
        return true;
    }
}
