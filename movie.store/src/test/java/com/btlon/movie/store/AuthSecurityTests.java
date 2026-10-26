package com.btlon.movie.store;

import com.btlon.movie.store.entity.*;
import com.btlon.movie.store.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthSecurityTests {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired CinemaRepository cinemas;
    @Autowired RoomRepository rooms;
    @Autowired MovieGenreRepository genres;
    @Autowired MovieRepository movies;
    @Autowired ShowtimeRepository showtimes;
    @Autowired ObjectMapper mapper;
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void adminEndpointsRejectGuestsAndCustomers() throws Exception {
        createUser("auth-admin@example.test", UserRole.ADMIN);
        createUser("auth-customer@example.test", UserRole.CUSTOMER);
        String admin = login("auth-admin@example.test");
        String customer = login("auth-customer@example.test");
        String code = "{\"code\":\"AUTH15\",\"discountPercent\":15}";

        assertEquals(401, request("POST", "/api/discount-codes", code, null).statusCode());
        assertEquals(403, request("POST", "/api/discount-codes", code, customer).statusCode());
        assertEquals(201, request("POST", "/api/discount-codes", code, admin).statusCode());
        assertEquals(403, request("GET", "/api/discount-codes", null, customer).statusCode());
        assertEquals(401, request("GET", "/api/tickets", null, null).statusCode());
        assertEquals(403, request("GET", "/api/tickets", null, customer).statusCode());
        assertEquals(200, request("GET", "/api/tickets", null, admin).statusCode());
        assertEquals(401, request("DELETE", "/api/tickets/1", null, null).statusCode());
        assertEquals(403, request("DELETE", "/api/tickets/1", null, customer).statusCode());
        assertEquals(403, request("POST", "/api/genres", "{\"name\":\"Hack\"}", customer).statusCode());
        assertEquals(200, request("GET", "/api/movies", null, null).statusCode());
    }

    @Test
    void bookingUsesTokenUserAndIgnoresUserIdInBody() throws Exception {
        User owner = createUser("auth-owner@example.test", UserRole.CUSTOMER);
        User victim = createUser("auth-victim@example.test", UserRole.CUSTOMER);
        String token = login("auth-owner@example.test");
        Long showtimeId = createShowtime().getId();

        // A guest cannot claim the member discount by sending someone's userId.
        String spoofed = "{\"userId\":" + victim.getId() + ",\"showtimeId\":" + showtimeId + ",\"seatNumbers\":[1]}";
        HttpResponse<String> guestQuote = request("POST", "/api/tickets/quote", spoofed, null);
        assertEquals(0, mapper.readTree(guestQuote.body()).get("memberDiscountPercent").asInt());
        assertEquals(400, request("POST", "/api/tickets/book", spoofed, null).statusCode());

        HttpResponse<String> booked = request("POST", "/api/tickets/book", spoofed, token);
        assertEquals(201, booked.statusCode());
        JsonNode ticket = mapper.readTree(booked.body()).get("tickets").get(0);
        assertEquals(owner.getId().longValue(), ticket.get("userId").asLong());
        assertEquals(5, mapper.readTree(booked.body()).get("pricing").get("memberDiscountPercent").asInt());

        assertEquals(401, request("GET", "/api/tickets/me", null, null).statusCode());
        JsonNode mine = mapper.readTree(request("GET", "/api/tickets/me", null, token).body());
        assertEquals(1, mine.size());
        assertEquals(401, request("POST", "/api/tickets/quote", spoofed, "not-a-real-token").statusCode());
        assertEquals(403, request("GET", "/api/users/" + victim.getId(), null, token).statusCode());

        assertEquals(204, request("POST", "/api/users/logout", null, token).statusCode());
        assertEquals(401, request("GET", "/api/users/me", null, token).statusCode());
    }

    @Test
    void registerRequiresValidPhoneAndMatchingPasswordAndMemberTicketsKeepPhone() throws Exception {
        String base = "\"fullName\":\"Reg\",\"email\":\"auth-register@example.test\",\"password\":\"secret1\"";
        assertEquals(400, request("POST", "/api/users/register",
                "{" + base + ",\"confirmPassword\":\"secret1\"}", null).statusCode());
        assertEquals(400, request("POST", "/api/users/register",
                "{" + base + ",\"phone\":\"0912345678\",\"confirmPassword\":\"other\"}", null).statusCode());
        assertEquals(400, request("POST", "/api/users/register",
                "{" + base + ",\"phone\":\"123\",\"confirmPassword\":\"secret1\"}", null).statusCode());

        HttpResponse<String> created = request("POST", "/api/users/register",
                "{" + base + ",\"phone\":\"+84 912 000 111\",\"confirmPassword\":\"secret1\"}", null);
        assertEquals(201, created.statusCode());
        JsonNode user = mapper.readTree(created.body());
        assertEquals("0912000111", user.get("phone").asString());

        Long showtimeId = createShowtime().getId();
        HttpResponse<String> booked = request("POST", "/api/tickets/book",
                "{\"showtimeId\":" + showtimeId + ",\"seatNumbers\":[9]}", user.get("token").asString());
        assertEquals(201, booked.statusCode());
        assertEquals("0912000111", mapper.readTree(booked.body()).get("tickets").get(0).get("customerPhone").asString());
    }

    private User createUser(String email, UserRole role) {
        User user = new User();
        user.setFullName(email);
        user.setEmail(email);
        user.setPasswordHash(new BCryptPasswordEncoder().encode("secret"));
        user.setRole(role);
        return users.save(user);
    }

    private String login(String email) throws Exception {
        HttpResponse<String> response = request("POST", "/api/users/login",
                "{\"email\":\"" + email + "\",\"password\":\"secret\"}", null);
        assertEquals(200, response.statusCode());
        return mapper.readTree(response.body()).get("token").asString();
    }

    private Showtime createShowtime() {
        String suffix = String.valueOf(System.nanoTime());
        Cinema cinema = cinemas.save(new Cinema("Auth Cinema " + suffix, "Address"));
        Room room = new Room("Auth Room " + suffix.substring(suffix.length() - 8), 20);
        room.setCinema(cinema);
        room = rooms.save(room);
        MovieGenre genre = genres.save(new MovieGenre(null, "Auth Genre " + suffix));
        Movie movie = movies.save(new Movie(null, "Auth Movie", 100, 2026, genre));
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(LocalDateTime.now().plusDays(1));
        showtime.setTicketPrice(BigDecimal.valueOf(100000));
        return showtimes.save(showtime);
    }

    private HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
