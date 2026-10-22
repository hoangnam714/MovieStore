package com.btlon.movie.store.controller;

import com.btlon.movie.store.dto.TicketBookRequest;
import com.btlon.movie.store.dto.TicketResponse;
import com.btlon.movie.store.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
    public List<TicketResponse> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/user/{userId}")
    public List<TicketResponse> getTicketsForUser(@PathVariable Long userId) {
        return ticketService.getTicketsForUser(userId);
    }

    @GetMapping("/showtime/{showtimeId}/seats")
    public List<Integer> getBookedSeats(@PathVariable Long showtimeId) {
        return ticketService.getBookedSeats(showtimeId);
    }

    @PostMapping("/book")
    public ResponseEntity<List<TicketResponse>> book(@RequestBody TicketBookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.bookTickets(request));
    }
}
