package com.btlon.movie.store;

import com.btlon.movie.store.dto.TicketBookRequest;
import com.btlon.movie.store.entity.*;
import com.btlon.movie.store.repository.*;
import com.btlon.movie.store.service.TicketService;
import com.btlon.movie.store.web.BadRequestException;
import com.btlon.movie.store.web.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BookingFlowTests {
    @Autowired RoomRepository rooms;
    @Autowired MovieGenreRepository genres;
    @Autowired MovieRepository movies;
    @Autowired ShowtimeRepository showtimes;
    @Autowired UserRepository users;
    @Autowired DiscountCodeRepository discountCodes;
    @Autowired TicketService tickets;

    @Test
    void booksOnlyFreeSeatsAndReopensSeatAfterCancellation() {
        Room room = new Room("Test Room", 20);
        room = rooms.save(room);
        MovieGenre genre = genres.save(new MovieGenre(null, "Test Genre"));
        Movie movie = movies.save(new Movie(null, "Test Movie", 110, 2026, genre));
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(LocalDateTime.now().plusDays(1));
        showtime.setTicketPrice(BigDecimal.valueOf(90000));
        showtime = showtimes.save(showtime);
        User user = new User();
        user.setFullName("Test Customer");
        user.setEmail("booking-flow@example.test");
        user.setPasswordHash(new BCryptPasswordEncoder().encode("password"));
        user.setRole(UserRole.CUSTOMER);
        User member = users.save(user);

        TicketBookRequest request = new TicketBookRequest();
        request.setShowtimeId(showtime.getId());
        request.setSeatNumbers(List.of(1, 3));
        assertThrows(BadRequestException.class, () -> tickets.bookTickets(request, member));
        request.setSeatNumbers(List.of(10, 11));
        assertThrows(BadRequestException.class, () -> tickets.bookTickets(request, member));
        request.setSeatNumbers(List.of(1, 12));
        assertThrows(BadRequestException.class, () -> tickets.bookTickets(request, member));
        request.setSeatNumbers(List.of(1, 11, 12));
        var booked = tickets.bookTickets(request, member).getTickets();
        assertEquals(3, booked.size());
        assertEquals(room.getName(), booked.get(0).getRoomName());
        assertEquals(List.of(1, 11, 12), tickets.getBookedSeats(showtime.getId()));
        assertThrows(ConflictException.class, () -> tickets.bookTickets(request, member));

        assertTrue(tickets.cancelTicket(booked.get(0).getId()));
        assertEquals(List.of(11, 12), tickets.getBookedSeats(showtime.getId()));
        request.setSeatNumbers(List.of(1));
        assertEquals(1, tickets.bookTickets(request, member).getTickets().size());
        request.setSeatNumbers(List.of(1, 1));
        assertThrows(BadRequestException.class, () -> tickets.bookTickets(request, member));
    }

    private Showtime createShowtime(String suffix, BigDecimal price) {
        Room room = new Room("Pricing Room " + suffix, 20);
        room = rooms.save(room);
        MovieGenre genre = genres.save(new MovieGenre(null, "Pricing Genre " + suffix));
        Movie movie = movies.save(new Movie(null, "Pricing Movie " + suffix, 100, 2026, genre));
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(LocalDateTime.now().plusDays(1));
        showtime.setTicketPrice(price);
        return showtimes.save(showtime);
    }

    @Test
    void guestMustGivePhoneAndPaysVatWithoutMemberDiscount() {
        Showtime showtime = createShowtime("guest", BigDecimal.valueOf(100000));
        TicketBookRequest request = new TicketBookRequest();
        request.setShowtimeId(showtime.getId());
        request.setSeatNumbers(List.of(1, 2));
        assertThrows(BadRequestException.class, () -> tickets.bookTickets(request, null));
        request.setCustomerPhone("12345");
        assertThrows(BadRequestException.class, () -> tickets.bookTickets(request, null));

        request.setCustomerPhone("+84 912 345 678");
        var booking = tickets.bookTickets(request, null);
        assertEquals(0, booking.getPricing().getMemberDiscountPercent());
        assertEquals(0, new BigDecimal("220000").compareTo(booking.getPricing().getTotal()));
        var ticket = booking.getTickets().get(0);
        assertTrue(ticket.isGuest());
        assertEquals("0912345678", ticket.getCustomerPhone());
        assertEquals(0, new BigDecimal("10000").compareTo(ticket.getVatAmount()));
        assertEquals(0, new BigDecimal("110000").compareTo(ticket.getTotalAmount()));
    }

    @Test
    void memberGetsFivePercentPlusDiscountCodeAndCodeUsageIsLimited() {
        Showtime showtime = createShowtime("member", BigDecimal.valueOf(100000));
        User user = new User();
        user.setFullName("Member");
        user.setEmail("member-pricing@example.test");
        user.setPasswordHash("hash");
        User member = users.save(user);
        DiscountCode code = new DiscountCode();
        code.setCode("SAVE10");
        code.setDiscountPercent(10);
        code.setUsageLimit(1);
        code.setEndDate(LocalDate.now().plusDays(1));
        discountCodes.save(code);

        TicketBookRequest request = new TicketBookRequest();
        request.setShowtimeId(showtime.getId());
        request.setSeatNumbers(List.of(5));
        request.setDiscountCode("save10");
        var quote = tickets.quote(request, member);
        assertEquals(5, quote.getMemberDiscountPercent());
        assertEquals(10, quote.getCodeDiscountPercent());
        // 100,000 - 15% = 85,000; VAT 10% = 8,500; total 93,500
        assertEquals(0, new BigDecimal("93500").compareTo(quote.getTotal()));

        var ticket = tickets.bookTickets(request, member).getTickets().get(0);
        assertEquals("SAVE10", ticket.getDiscountCode());
        assertEquals(0, new BigDecimal("15000").compareTo(ticket.getDiscountAmount()));
        assertEquals(0, new BigDecimal("93500").compareTo(ticket.getTotalAmount()));
        assertEquals(1, discountCodes.findByCodeIgnoreCase("SAVE10").orElseThrow().getUsedCount());

        request.setSeatNumbers(List.of(6));
        assertThrows(BadRequestException.class, () -> tickets.bookTickets(request, member));
        request.setDiscountCode(null);
        var memberOnly = tickets.bookTickets(request, member).getTickets().get(0);
        // 100,000 - 5% = 95,000; VAT 9,500; total 104,500
        assertEquals(0, new BigDecimal("104500").compareTo(memberOnly.getTotalAmount()));
    }
}
