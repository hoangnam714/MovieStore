package com.btlon.movie.store;

import com.btlon.movie.store.entity.Movie;
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
    void movieCrudKeepsGenreIdAndAllowsChangingGenre() throws Exception {
        MovieGenre action = genres.save(new MovieGenre(null, "Action"));
        MovieGenre drama = genres.save(new MovieGenre(null, "Drama"));
        HttpResponse<String> created = request("POST", "/api/movies", movieBody(action.getId()));
        assertEquals(201, created.statusCode());
        JsonNode movie = mapper.readTree(created.body());
        long id = movie.get("id").asLong();
        assertEquals(action.getId().longValue(), movie.get("genreId").asLong());
        assertFalse(movie.has("genre"));

        HttpResponse<String> genreResponse = request("GET", "/api/genres/" + action.getId() + "/movies", null);
        assertEquals(200, genreResponse.statusCode());
        JsonNode genre = mapper.readTree(genreResponse.body());
        assertEquals(id, genre.get("movies").get(0).get("id").asLong());
        assertEquals(200, request("GET", "/api/genres", null).statusCode());
        assertEquals(200, request("GET", "/api/movies", null).statusCode());
        assertEquals(action.getId().longValue(), mapper.readTree(
                request("GET", "/api/movies/" + id, null).body()).get("genreId").asLong());

        HttpResponse<String> updated = request("PUT", "/api/movies/" + id, movieBody(drama.getId()));
        assertEquals(200, updated.statusCode());
        assertEquals(drama.getId().longValue(), mapper.readTree(updated.body()).get("genreId").asLong());
        assertTrue(movies.findByGenre_Id(action.getId()).isEmpty());
        assertEquals(1, movies.findByGenre_Id(drama.getId()).size());

        assertEquals(204, request("DELETE", "/api/movies/" + id, null).statusCode());
        assertEquals(404, request("GET", "/api/movies/" + id, null).statusCode());
        assertTrue(genres.existsById(drama.getId()));
    }

    @Test
    void rejectsMissingOrUnknownGenreWithoutSavingMovie() throws Exception {
        assertEquals(400, request("POST", "/api/movies", movieBody(999999L)).statusCode());
        assertEquals(400, request("POST", "/api/movies",
                "{\"title\":\"Demo\",\"duration\":120,\"releaseYear\":2026}").statusCode());
        assertEquals(0, movies.count());
    }

    @Test
    void createsGenreWithMoviesWithoutJsonRecursion() throws Exception {
        HttpResponse<String> response = request("POST", "/api/genres", """
                {"name":"Comedy"}
                """);
        assertEquals(201, response.statusCode());
        JsonNode genre = mapper.readTree(response.body());
        long genreId = genre.get("id").asLong();
        assertEquals(201, request("POST", "/api/movies",
                "{\"title\":\"Demo\",\"duration\":100,\"releaseYear\":2026,\"genreId\":" + genreId + "}")
                .statusCode());
        assertEquals(1, movies.findByGenre_Id(genreId).size());
        assertEquals(200, request("GET", "/api/genres/" + genreId, null).statusCode());
    }

    @Test
    void cascadesPersistenceWithoutDeletingMoviesWhenGenreIsRemoved() {
        MovieGenre genre = new MovieGenre(null, "Adventure");
        genre.addMovie(new Movie(null, "First", 90, 2026, null));
        genre.addMovie(new Movie(null, "Second", 100, 2026, null));
        Long genreId = genres.saveAndFlush(genre).getId();
        assertEquals(2, movies.findByGenre_Id(genreId).size());

        transactions.executeWithoutResult(status -> {
            MovieGenre managed = genres.findById(genreId).orElseThrow();
            managed.removeMovie(managed.getMovies().get(0));
        });
        assertEquals(2, movies.count());
        assertThrows(Exception.class, () -> genres.deleteById(genreId));
        assertEquals(2, movies.count());
    }

    private String movieBody(Long genreId) {
        return "{\"title\":\"Demo\",\"duration\":120,\"releaseYear\":2026,\"genreId\":" + genreId + "}";
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
