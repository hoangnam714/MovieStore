package com.btlon.movie.store.controller;

import com.btlon.movie.store.dto.BookingQuote;
import com.btlon.movie.store.dto.BookingResponse;
import com.btlon.movie.store.dto.TicketBookRequest;
import com.btlon.movie.store.dto.TicketResponse;
import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.security.AdminOnly;
import com.btlon.movie.store.security.CurrentUser;
import com.btlon.movie.store.security.LoginRequired;
import com.btlon.movie.store.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    @AdminOnly
    public List<TicketResponse> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/me")
    @LoginRequired
    public List<TicketResponse> getMyTickets(@CurrentUser User user) {
        return ticketService.getTicketsForUser(user.getId());
    }

    @GetMapping("/showtime/{showtimeId}/seats")
    public List<Integer> getBookedSeats(@PathVariable Long showtimeId) {
        return ticketService.getBookedSeats(showtimeId);
    }

    @PostMapping("/quote")
    public BookingQuote quote(@RequestBody TicketBookRequest request, @CurrentUser User user) {
        return ticketService.quote(request, user);
    }

    @PostMapping("/book")
    public ResponseEntity<BookingResponse> book(@RequestBody TicketBookRequest request, @CurrentUser User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.bookTickets(request, user));
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        return ticketService.cancelTicket(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
