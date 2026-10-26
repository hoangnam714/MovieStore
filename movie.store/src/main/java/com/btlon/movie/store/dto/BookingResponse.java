package com.btlon.movie.store.dto;

import java.util.List;

public class BookingResponse {
    private final BookingQuote pricing;
    private final List<TicketResponse> tickets;

    public BookingResponse(BookingQuote pricing, List<TicketResponse> tickets) {
        this.pricing = pricing;
        this.tickets = tickets;
    }

    public BookingQuote getPricing() { return pricing; }
    public List<TicketResponse> getTickets() { return tickets; }
}
