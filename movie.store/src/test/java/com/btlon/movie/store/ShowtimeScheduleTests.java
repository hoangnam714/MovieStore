package com.btlon.movie.store;

import com.btlon.movie.store.entity.*;
import com.btlon.movie.store.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShowtimeScheduleTests {
    @LocalServerPort private int port;
    @Autowired private CinemaRepository cinemas;
    @Autowired private RoomRepository rooms;
    @Autowired private MovieRepository movies;
    @Autowired private MovieGenreRepository genres;
    @Autowired private ShowtimeRepository showtimes;
    @Autowired private TicketRepository tickets;
    @Autowired private ObjectMapper mapper;
    private final HttpClient client = HttpClient.newHttpClient();
    private final LocalDate date = LocalDate.of(2026, 10, 30);
    private Cinema firstCinema;
    private Cinema secondCinema;
    private Room firstRoom;
    private Room emptyRoom;
    private Movie soul;
    private Showtime early;
    private Showtime late;
    private Showtime overnight;

    @BeforeEach
    void setUp() {
        tickets.deleteAll();
        showtimes.deleteAll();
        movies.deleteAll();
        genres.deleteAll();
        rooms.deleteAll();
        cinemas.deleteAll();
        firstCinema = cinemas.save(new Cinema("Rạp Quận 1", "Địa chỉ 1"));
        secondCinema = cinemas.save(new Cinema("Rạp Thủ Đức", "Địa chỉ 2"));
        cinemas.save(new Cinema("Rạp Trống", "Địa chỉ 3"));
        firstRoom = room("Phòng 1", firstCinema);
        emptyRoom = room("Phòng 2", firstCinema);
        Room secondRoom = room("Phòng 3", secondCinema);
        MovieGenre genre = genres.save(new MovieGenre(null, "Hành động"));
        soul = movies.save(new Movie(null, "Soul", 100, 2026, genre));
        Movie dune = movies.save(new Movie(null, "Dune", 166, 2026, genre));
        // Save out of order to verify chronological ordering in the API.
        late = showtime(dune, firstRoom, date.atTime(13, 0));
        early = showtime(soul, firstRoom, date.atTime(9, 0));
        showtime(dune, secondRoom, date.atTime(10, 0));
        overnight = showtime(soul, secondRoom, date.atTime(23, 30));
        showtime(dune, firstRoom, date.minusDays(1).atTime(23, 30));
        showtime(dune, firstRoom, date.plusDays(1).atStartOfDay());
    }

    @Test
    void groupsByCinemaAndRoomKeepingEmptyRoomsAndCinema() throws Exception {
        JsonNode body = schedule("");
        assertEquals(date.toString(), body.get("date").asText());
        assertEquals(4, body.get("totalShowtimes").asInt());
        assertEquals(3, body.get("cinemas").size());
        JsonNode cinema = body.get("cinemas").get(0);
        assertEquals(firstCinema.getId().longValue(), cinema.get("id").asLong());
        assertEquals(2, cinema.get("totalShowtimes").asInt());
        assertEquals(2, cinema.get("rooms").size());
        assertEquals(List.of(early.getId(), late.getId()), ids(cinema.get("rooms").get(0).get("showtimes")));
        assertEquals(emptyRoom.getId().longValue(), cinema.get("rooms").get(1).get("id").asLong());
        assertTrue(cinema.get("rooms").get(1).get("showtimes").isEmpty());
        assertTrue(body.get("cinemas").get(2).get("rooms").isEmpty());
    }

    @Test
    void filtersOneOrSeveralCinemasWithoutDuplicatingGroups() throws Exception {
        JsonNode one = schedule("&cinemaIds=" + secondCinema.getId());
        assertEquals(1, one.get("cinemas").size());
        assertEquals(2, one.get("totalShowtimes").asInt());
        JsonNode two = schedule("&cinemaIds=" + firstCinema.getId() + "," + secondCinema.getId());
        assertEquals(2, two.get("cinemas").size());
        assertEquals(4, two.get("totalShowtimes").asInt());
        assertEquals(1, schedule("&cinemaIds=" + firstCinema.getId() + "," + firstCinema.getId())
                .get("cinemas").size());
        assertTrue(schedule("&cinemaIds=999999").get("cinemas").isEmpty());
    }

    @Test
    void searchesMovieRoomAndCinemaIgnoringCaseAndVietnameseAccents() throws Exception {
        JsonNode movie = schedule("&keyword=SoUl");
        assertEquals(2, movie.get("totalShowtimes").asInt());
        assertEquals(2, movie.get("cinemas").size());
        assertEquals(1, movie.get("cinemas").get(0).get("rooms").size());
        JsonNode cinema = schedule("&keyword=" + URLEncoder.encode("  RAP QUAN 1  ", StandardCharsets.UTF_8));
        assertEquals(1, cinema.get("cinemas").size());
        assertEquals(2, cinema.get("totalShowtimes").asInt());
        assertEquals(2, cinema.get("cinemas").get(0).get("rooms").size());
        JsonNode room = schedule("&keyword=phong%202");
        assertEquals(0, room.get("totalShowtimes").asInt());
        assertEquals(emptyRoom.getId().longValue(), room.get("cinemas").get(0).get("rooms").get(0).get("id").asLong());
        JsonNode missing = schedule("&keyword=missing");
        assertTrue(missing.get("cinemas").isEmpty());
        assertEquals(0, missing.get("totalShowtimes").asInt());
    }

    @Test
    void calculatesEndTimeAcrossMidnightAndKeepsExistingEndpoints() throws Exception {
        JsonNode body = schedule("&cinemaIds=" + secondCinema.getId());
        JsonNode last = body.get("cinemas").get(0).get("rooms").get(0).get("showtimes").get(1);
        assertEquals(overnight.getId().longValue(), last.get("id").asLong());
        assertEquals("2026-10-31T01:10:00", last.get("endTime").asText());
        assertTrue(mapper.readTree(get("/api/showtimes").body()).isArray());
        JsonNode single = mapper.readTree(get("/api/showtimes/" + early.getId()).body());
        assertEquals("2026-10-30T10:40:00", single.get("endTime").asText());
    }

    @Test
    void includesUnassignedRoomsAndCanSelectThemWithZero() throws Exception {
        Room legacy = room("Phòng cũ", null);
        Showtime item = showtime(soul, legacy, date.atTime(11, 0));
        JsonNode body = schedule("&cinemaIds=0");
        assertEquals(1, body.get("totalShowtimes").asInt());
        assertEquals(1, body.get("cinemas").size());
        JsonNode cinema = body.get("cinemas").get(0);
        assertTrue(cinema.get("id").isNull());
        assertEquals(List.of(item.getId()), ids(cinema.get("rooms").get(0).get("showtimes")));
    }

    @Test
    void defaultsToTodayAndRejectsInvalidFilters() throws Exception {
        JsonNode body = mapper.readTree(get("/api/showtimes/schedule").body());
        assertEquals(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString(), body.get("date").asText());
        assertEquals(400, get("/api/showtimes/schedule?date=2026-02-30").statusCode());
        assertEquals(400, get("/api/showtimes/schedule?cinemaIds=-1").statusCode());
        assertEquals(400, get("/api/showtimes/schedule?cinemaIds=abc").statusCode());
    }

    private JsonNode schedule(String filters) throws Exception {
        var response = get("/api/showtimes/schedule?date=" + date + filters);
        assertEquals(200, response.statusCode(), response.body());
        return mapper.readTree(response.body());
    }

    private List<Long> ids(JsonNode showtimes) {
        List<Long> ids = new ArrayList<>();
        showtimes.forEach(showtime -> ids.add(showtime.get("id").asLong()));
        return ids;
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private Room room(String name, Cinema cinema) {
        Room room = new Room(name, 50);
        room.setCinema(cinema);
        return rooms.save(room);
    }

    private Showtime showtime(Movie movie, Room room, LocalDateTime start) {
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(start);
        showtime.setTicketPrice(new BigDecimal("80000"));
        return showtimes.save(showtime);
    }
}
