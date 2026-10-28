package com.btlon.movie.store;

import com.btlon.movie.store.entity.Movie;
import com.btlon.movie.store.config.MovieGenreMigration;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import com.btlon.movie.store.entity.MovieGenre;
import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.entity.UserRole;
import com.btlon.movie.store.repository.AuthTokenRepository;
import com.btlon.movie.store.repository.MovieGenreRepository;
import com.btlon.movie.store.repository.MovieRepository;
import com.btlon.movie.store.repository.RoomRepository;
import com.btlon.movie.store.repository.ShowtimeRepository;
import com.btlon.movie.store.repository.TicketRepository;
import com.btlon.movie.store.repository.UserRepository;
import com.btlon.movie.store.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MovieRelationshipTests {
    @LocalServerPort
    private int port;
    @Autowired
    private MovieGenreRepository genres;
    @Autowired
    private MovieRepository movies;
    @Autowired
    private TicketRepository tickets;
    @Autowired
    private ShowtimeRepository showtimes;
    @Autowired
    private UserRepository users;
    @Autowired
    private RoomRepository rooms;
    @Autowired
    private TransactionTemplate transactions;
    @Autowired
    private AuthTokenRepository authTokens;
    @Autowired
    private AuthService authService;
    @Autowired
    private JdbcTemplate jdbc;
    private String adminToken;
    @Autowired
    private ObjectMapper mapper;

    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void cleanDatabase() {
        tickets.deleteAll();
        authTokens.deleteAll();
        showtimes.deleteAll();
        movies.deleteAll();
        genres.deleteAll();
        users.deleteAll();
        rooms.deleteAll();
        User admin = new User();
        admin.setFullName("Admin");
        admin.setEmail("relationship-admin@example.test");
        admin.setPasswordHash("hash");
        admin.setRole(UserRole.ADMIN);
        adminToken = authService.issue(users.save(admin));
    }

    @Test
    void movieCanHaveSeveralGenresAndReplacingGenresKeepsOtherMovies() throws Exception {
        MovieGenre action = genres.save(new MovieGenre(null, "Action"));
        MovieGenre drama = genres.save(new MovieGenre(null, "Drama"));
        long otherId = mapper.readTree(request("POST", "/api/movies", movieBody(action.getId())).body())
                .get("id").asLong();
        var created = request("POST", "/api/movies", movieBody(action.getId(), drama.getId(), action.getId()));
        assertEquals(201, created.statusCode(), created.body());
        JsonNode movie = mapper.readTree(created.body());
        long id = movie.get("id").asLong();
        assertEquals(Set.of(action.getId(), drama.getId()), genreIds(movie));
        assertEquals(2, movie.get("genres").size());
        assertEquals(Set.of(action.getId(), drama.getId()), genreIds(mapper.readTree(
                request("GET", "/api/movies/" + id, null).body())));

        for (Long genreId : List.of(action.getId(), drama.getId())) {
            var response = request("GET", "/api/genres/" + genreId + "/movies", null);
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("Demo"));
            assertFalse(mapper.readTree(response.body()).get("movies").get(0).has("genres"));
            var filtered = mapper.readTree(request("GET", "/api/movies?genreId=" + genreId, null).body());
            List<Long> ids = new ArrayList<>();
            filtered.get("movies").forEach(item -> ids.add(item.get("id").asLong()));
            assertTrue(ids.contains(id));
            assertEquals(ids.size(), Set.copyOf(ids).size());
            assertEquals(409, request("DELETE", "/api/genres/" + genreId, null).statusCode());
        }

        var updated = request("PUT", "/api/movies/" + id, movieBody(drama.getId()));
        assertEquals(200, updated.statusCode());
        assertEquals(Set.of(drama.getId()), genreIds(mapper.readTree(updated.body())));
        assertEquals(1, movies.findByGenres_Id(action.getId()).size());
        assertEquals(Set.of(action.getId()), genreIds(mapper.readTree(
                request("GET", "/api/movies/" + otherId, null).body())));

        assertEquals(204, request("DELETE", "/api/movies/" + id, null).statusCode());
        assertEquals(404, request("GET", "/api/movies/" + id, null).statusCode());
        assertTrue(genres.existsById(drama.getId()));
        assertTrue(genres.existsById(action.getId()));
        assertEquals(204, request("DELETE", "/api/genres/" + drama.getId(), null).statusCode());
        assertTrue(movies.existsById(otherId));
    }

    @Test
    void rejectsMissingEmptyOrInvalidGenresWithoutSavingMovie() throws Exception {
        MovieGenre genre = genres.save(new MovieGenre(null, "Action"));
        assertEquals(400, request("POST", "/api/movies", movieBody(999999L)).statusCode());
        assertEquals(400, request("POST", "/api/movies", movieBody(genre.getId(), 999999L)).statusCode());
        assertEquals(400, request("POST", "/api/movies", movieBody()).statusCode());
        assertEquals(400, request("POST", "/api/movies", movieBody(0L)).statusCode());
        assertEquals(400, request("POST", "/api/movies", movieBody(-1L)).statusCode());
        assertEquals(400, request("POST", "/api/movies", movieBody(genre.getId()).replace(
                "[" + genre.getId() + "]", "[null]")).statusCode());
        assertEquals(400, request("POST", "/api/movies",
                "{\"title\":\"Demo\",\"duration\":120,\"releaseDate\":\"2026-10-28\"}").statusCode());
        assertEquals(0, movies.count());
    }

    @Test
    void invalidGenreUpdateDoesNotChangeExistingMovie() throws Exception {
        MovieGenre genre = genres.save(new MovieGenre(null, "Action"));
        long id = mapper.readTree(request("POST", "/api/movies", movieBody(genre.getId())).body())
                .get("id").asLong();
        assertEquals(400, request("PUT", "/api/movies/" + id, movieBody(genre.getId(), 999999L)
                .replace("Demo", "Invalid update")).statusCode());
        var movie = mapper.readTree(request("GET", "/api/movies/" + id, null).body());
        assertEquals("Demo", movie.get("title").asText());
        assertEquals(Set.of(genre.getId()), genreIds(movie));
    }

    @Test
    void createsGenreWithoutJsonRecursion() throws Exception {
        var response = request("POST", "/api/genres", "{\"name\":\"Comedy\"}");
        assertEquals(201, response.statusCode());
        long genreId = mapper.readTree(response.body()).get("id").asLong();
        assertEquals(201, request("POST", "/api/movies", movieBody(genreId)).statusCode());
        assertEquals(1, movies.findByGenres_Id(genreId).size());
        assertEquals(200, request("GET", "/api/genres/" + genreId, null).statusCode());
        assertEquals(200, request("GET", "/api/genres", null).statusCode());
    }

    @Test
    void migratesLegacyGenreOnceAndAllowsNewMoviesWithoutOldColumn() throws Exception {
        MovieGenre action = genres.save(new MovieGenre(null, "Action"));
        MovieGenre drama = genres.save(new MovieGenre(null, "Drama"));
        Movie old = movies.save(new Movie(null, "Legacy", 90, 2026, null));
        jdbc.execute("ALTER TABLE movies ADD COLUMN genre_id BIGINT");
        jdbc.update("UPDATE movies SET genre_id = ? WHERE id = ?", action.getId(), old.getId());
        jdbc.execute("ALTER TABLE movies ALTER COLUMN genre_id SET NOT NULL");
        jdbc.execute("ALTER TABLE movies ADD CONSTRAINT fk_test_old_genre FOREIGN KEY (genre_id) REFERENCES movie_genres(id)");
        var migration = new MovieGenreMigration(jdbc);
        migration.run();
        assertEquals(Set.of(action.getId()), genreIds(mapper.readTree(
                request("GET", "/api/movies/" + old.getId(), null).body())));
        assertEquals(200, request("PUT", "/api/movies/" + old.getId(), movieBody(drama.getId())).statusCode());
        migration.run();
        assertEquals(Set.of(drama.getId()), genreIds(mapper.readTree(
                request("GET", "/api/movies/" + old.getId(), null).body())));
        assertEquals(201, request("POST", "/api/movies", movieBody(action.getId(), drama.getId())).statusCode());
    }

    private Set<Long> genreIds(JsonNode movie) {
        Set<Long> ids = new java.util.HashSet<>();
        movie.get("genres").forEach(genre -> ids.add(genre.get("id").asLong()));
        return ids;
    }

    private String movieBody(Long... genreIds) {
        return "{\"title\":\"Demo\",\"duration\":120,\"releaseDate\":\"2026-10-28\",\"genreIds\":"
                + java.util.Arrays.toString(genreIds) + "}";
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .method(method, body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
