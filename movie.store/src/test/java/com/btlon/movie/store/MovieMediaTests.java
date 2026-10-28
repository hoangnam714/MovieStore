package com.btlon.movie.store;

import com.btlon.movie.store.config.MovieMediaMigration;
import com.btlon.movie.store.entity.*;
import com.btlon.movie.store.repository.*;
import com.btlon.movie.store.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.servlet.multipart.max-file-size=5MB",
        "spring.servlet.multipart.max-request-size=6MB",
        "server.tomcat.max-swallow-size=6MB"
})
class MovieMediaTests {
    @LocalServerPort
    private int port;
    @Autowired
    private MovieRepository movies;
    @Autowired
    private MovieGenreRepository genres;
    @Autowired
    private ShowtimeRepository showtimes;
    @Autowired
    private RoomRepository rooms;
    @Autowired
    private TicketRepository tickets;
    @Autowired
    private UserRepository users;
    @Autowired
    private AuthTokenRepository tokens;
    @Autowired
    private AuthService auth;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private TransactionTemplate transactions;
    private final HttpClient client = HttpClient.newHttpClient();
    private static final Path UPLOADS = Path.of("target", "movie-media-test-uploads", UUID.randomUUID().toString())
            .toAbsolutePath();
    private MovieGenre genre;
    private Room room;
    private String adminToken;
    private String customerToken;

    @DynamicPropertySource
    static void uploadDirectory(DynamicPropertyRegistry registry) {
        registry.add("moviestore.upload.directory", UPLOADS::toString);
    }

    @BeforeEach
    void setup() {
        tickets.deleteAll();
        tokens.deleteAll();
        showtimes.deleteAll();
        movies.deleteAll();
        genres.deleteAll();
        users.deleteAll();
        rooms.deleteAll();
        genre = genres.save(new MovieGenre(null, "Media test"));
        room = rooms.save(new Room("Media room", 50));
        adminToken = token("admin", UserRole.ADMIN);
        customerToken = token("customer", UserRole.CUSTOMER);
    }

    @Test
    void movieListFiltersByTitleGenreAndBannerWithGlobalFeaturedCount() throws Exception {
        Movie action = movie("Alpha Action", LocalDate.now(), null, true, true, 0);
        Movie disabled = movie("Beta Action", LocalDate.now(), null, false, true, 0);
        Movie dramaMovie = movie("Alpha Drama", LocalDate.now(), null, true, true, 0);
        MovieGenre drama = genres.save(new MovieGenre(null, "Drama"));
        dramaMovie.setGenre(drama);
        movies.save(dramaMovie);

        assertEquals(List.of(action.getId(), dramaMovie.getId(), disabled.getId()), movieIds("/api/movies"));
        assertEquals(List.of(action.getId(), dramaMovie.getId()), movieIds("/api/movies?title=%20aLpHa%20"));
        assertEquals(List.of(dramaMovie.getId()), movieIds("/api/movies?genreId=" + drama.getId()));
        assertEquals(List.of(action.getId(), dramaMovie.getId()), movieIds("/api/movies?featured=true"));
        assertEquals(List.of(disabled.getId()), movieIds("/api/movies?featured=false"));
        String combined = "/api/movies?title=alpha&genreId=" + genre.getId() + "&featured=true";
        assertEquals(List.of(action.getId()), movieIds(combined));
        assertTrue(movieIds("/api/movies?title=alpha&genreId=" + drama.getId() + "&featured=false").isEmpty());
        assertTrue(movieIds("/api/movies?title=missing").isEmpty());
        assertTrue(movieIds("/api/movies?genreId=999999").isEmpty());
        var response = request("GET", combined, null, null);
        var body = mapper.readTree(response.body());
        assertEquals(2, body.get("featuredCount").asInt());
        assertEquals(1, body.get("movies").size());
        assertEquals(2, mapper.readTree(request("GET", "/api/movies?title=missing", null, null).body())
                .get("featuredCount").asInt());

        // Legacy NULL means not featured, just like Movie.isFeatured().
        jdbc.update("UPDATE movies SET featured = NULL WHERE id = ?", disabled.getId());
        assertEquals(List.of(disabled.getId()), movieIds("/api/movies?featured=false"));
    }

    @Test
    void movieTitleSearchTreatsWildcardsAsLiteralText() throws Exception {
        Movie percent = movie("100% Real", LocalDate.now(), null, false, true, 0);
        movie("1000 Real", LocalDate.now(), null, false, true, 0);
        Movie underscore = movie("Under_score", LocalDate.now(), null, false, true, 0);
        movie("UnderXscore", LocalDate.now(), null, false, true, 0);
        Movie exclamation = movie("Wow! Film", LocalDate.now(), null, false, true, 0);
        assertEquals(List.of(percent.getId()), movieIds("/api/movies?title=100%25"));
        assertEquals(List.of(underscore.getId()), movieIds("/api/movies?title=Under_score"));
        assertEquals(List.of(exclamation.getId()), movieIds("/api/movies?title=Wow%21"));
        assertEquals(5, movieIds("/api/movies?title=%20%20").size());
    }

    @Test
    void movieListRejectsInvalidFilterValues() throws Exception {
        assertEquals(400, request("GET", "/api/movies?genreId=0", null, null).statusCode());
        assertEquals(400, request("GET", "/api/movies?genreId=-1", null, null).statusCode());
        assertEquals(400, request("GET", "/api/movies?genreId=invalid", null, null).statusCode());
        assertEquals(400, request("GET", "/api/movies?featured=invalid", null, null).statusCode());
    }

    @Test
    void movieCrudPersistsMediaAndRequiresRealReleaseDate() throws Exception {
        var created = request("POST", "/api/movies", body("New movie", null, false), adminToken);
        assertEquals(201, created.statusCode(), created.body());
        JsonNode movie = mapper.readTree(created.body());
        long id = movie.get("id").asLong();
        assertEquals("2026-10-28", movie.get("releaseDate").asText());
        assertEquals("Media test", movie.get("genreName").asText());
        assertTrue(movie.get("bannerPosition").isNull());
        assertFalse(movie.has("genre"));
        var updated = request("PUT", "/api/movies/" + id, body("Renamed movie", 20, true), adminToken);
        assertEquals(200, updated.statusCode(), updated.body());
        movie = mapper.readTree(updated.body());
        assertEquals(20, movie.get("bannerPosition").asInt());
        assertTrue(movie.get("featured").asBoolean());
        assertEquals("Subtitle", movie.get("subtitle").asText());
        assertEquals("Description", movie.get("description").asText());
        String atLimit = body("Boundary length", null, false)
                .replace("\"Subtitle\"", "\"" + "A".repeat(100) + "\"")
                .replace("\"Description\"", "\"" + "B".repeat(500) + "\"");
        assertEquals(200, request("PUT", "/api/movies/" + id, atLimit, adminToken).statusCode());
        assertEquals(400, request("PUT", "/api/movies/" + id,
                body("Long subtitle", null, false).replace("\"Subtitle\"", "\"" + "A".repeat(101) + "\""),
                adminToken).statusCode());
        assertEquals(400, request("PUT", "/api/movies/" + id,
                body("Long description", null, false).replace("\"Description\"", "\"" + "B".repeat(501) + "\""),
                adminToken).statusCode());
        assertEquals(400, request("PUT", "/api/movies/" + id, body("Bad position", -1, true), adminToken).statusCode());
        assertEquals(400, request("PUT", "/api/movies/" + id, body("Position too high", 100, true), adminToken).statusCode());
        assertEquals(200, request("PUT", "/api/movies/" + id, body("Lowest position", 0, true), adminToken).statusCode());
        assertEquals(200, request("PUT", "/api/movies/" + id, body("Highest position", 99, true), adminToken).statusCode());
        String noDate = body("No date", null, false).replace("\"releaseDate\":\"2026-10-28\"", "\"releaseYear\":2026");
        assertEquals(400, request("POST", "/api/movies", noDate, adminToken).statusCode());
        assertEquals(400, request("POST", "/api/movies", body("No poster", null, true)
                .replace("https://example.test/poster.jpg", ""), adminToken).statusCode());
        assertEquals(400, request("POST", "/api/movies", body("Bad date", null, false)
                .replace("2026-10-28", "2026-02-30"), adminToken).statusCode());
        assertEquals(204, request("DELETE", "/api/movies/" + id, null, adminToken).statusCode());
    }

    @Test
    void bannersArePublicFilteredOrderedAndLimitedByBackend() throws Exception {
        Movie first = movie("Priority 20", LocalDate.of(2020, 1, 1), 20, true, true, 1);
        Movie second = movie("Priority 10 newest", LocalDate.of(2026, 1, 1), 10, true, true, 1);
        Movie third = movie("Priority 10 older", LocalDate.of(2024, 1, 1), 10, true, true, 1);
        Movie fourth = movie("No priority newest", LocalDate.of(2028, 1, 1), null, true, true, 1);
        Movie fifth = movie("No priority tied newer id", LocalDate.of(2027, 1, 1), null, true, true, 1);
        // Create the tie after fifth, so ID breaks the tie ahead of fifth.
        Movie tie = movie("No priority tied", LocalDate.of(2027, 1, 1), null, true, true, 1);
        movie("Legacy unknown date", null, null, true, true, 1);
        movie("Disabled", LocalDate.now(), 100, false, true, 1);
        movie("No image", LocalDate.now(), 100, true, false, 1);
        movie("Past only", LocalDate.now(), 100, true, true, -1);
        Movie unscheduled = movie("No showtimes", LocalDate.now(), 100, true, true, 0);
        // A second future showtime must not duplicate the first banner.
        addShowtime(first, 3);
        var response = request("GET", "/api/movies/banners", null, null);
        assertEquals(200, response.statusCode(), response.body());
        List<Long> ids = new ArrayList<>();
        mapper.readTree(response.body()).forEach(item -> ids.add(item.get("id").asLong()));
        assertEquals(List.of(first.getId(), second.getId(), third.getId(), fourth.getId(), tie.getId()), ids);
        assertFalse(ids.contains(unscheduled.getId()));
        assertFalse(ids.contains(fifth.getId()));
    }

    @Test
    void limitAllowsEditingAndDisablingButRejectsSixthBanner() throws Exception {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            var response = request("POST", "/api/movies", body("Banner " + i, null, true), adminToken);
            assertEquals(201, response.statusCode(), response.body());
            ids.add(mapper.readTree(response.body()).get("id").asLong());
        }
        assertEquals(409, request("POST", "/api/movies", body("Sixth", null, true), adminToken).statusCode());
        assertEquals(200,
                request("PUT", "/api/movies/" + ids.get(0), body("Edited", 99, true), adminToken).statusCode());
        assertEquals(200,
                request("PUT", "/api/movies/" + ids.get(0), body("Disabled", 99, false), adminToken).statusCode());
        assertEquals(201, request("POST", "/api/movies", body("Replacement", null, true), adminToken).statusCode());
    }

    @Test
    void concurrentPromotionsCannotExceedFive() throws Exception {
        for (int i = 0; i < 4; i++)
            assertEquals(201,
                    request("POST", "/api/movies", body("Existing " + i, null, true), adminToken).statusCode());
        var one = client.sendAsync(httpRequest("POST", "/api/movies", body("Concurrent one", 1, true), adminToken),
                HttpResponse.BodyHandlers.ofString());
        var two = client.sendAsync(httpRequest("POST", "/api/movies", body("Concurrent two", 2, true), adminToken),
                HttpResponse.BodyHandlers.ofString());
        CompletableFuture.allOf(one, two).join();
        assertEquals(List.of(201, 409),
                List.of(one.join().statusCode(), two.join().statusCode()).stream().sorted().toList());
        assertEquals(5, movies.findAll().stream().filter(Movie::isFeatured).count());
    }

    @Test
    void imageUploadIsAdminOnlyAndUploadedFileIsPublic() throws Exception {
        byte[] png = png();
        assertEquals(401, upload(png, "image/png", null).statusCode());
        assertEquals(403, upload(png, "image/png", customerToken).statusCode());
        var response = upload(png, "image/png", adminToken);
        assertEquals(201, response.statusCode(), response.body());
        String url = mapper.readTree(response.body()).get("url").asText();
        assertTrue(url.matches("/api/uploads/images/[a-f0-9-]{36}\\.png"));
        var read = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + url)).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, read.statusCode());
        assertEquals("image/png", read.headers().firstValue("Content-Type").orElseThrow());
        assertArrayEquals(png, read.body());
        ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "jpg", jpeg);
        var jpegUpload = upload(jpeg.toByteArray(), "image/jpeg", adminToken);
        assertEquals(201, jpegUpload.statusCode(), jpegUpload.body());
        assertTrue(mapper.readTree(jpegUpload.body()).get("url").asText().endsWith(".jpg"));
        String jpegUrl = mapper.readTree(jpegUpload.body()).get("url").asText();
        assertEquals("image/jpeg", request("GET", jpegUrl, null, null).headers()
                .firstValue("Content-Type").orElseThrow());
        byte[] webp = Base64.getDecoder().decode("UklGRiIAAABXRUJQVlA4IBYAAAAwAQCdASoBAAEADsD+JaQAA3AAAAAA");
        var webpUpload = upload(webp, "image/webp", adminToken);
        assertEquals(201, webpUpload.statusCode(), webpUpload.body());
        assertTrue(mapper.readTree(webpUpload.body()).get("url").asText().endsWith(".webp"));
        String webpUrl = mapper.readTree(webpUpload.body()).get("url").asText();
        assertEquals("image/webp", request("GET", webpUrl, null, null).headers()
                .firstValue("Content-Type").orElseThrow());
        assertEquals(404, request("GET", "/api/uploads/images/missing.png", null, null).statusCode());
        assertEquals(400,
                upload("<svg>script</svg>".getBytes(StandardCharsets.UTF_8), "image/svg+xml", adminToken).statusCode());
        assertEquals(400, upload(png, "application/pdf", adminToken).statusCode());
        assertEquals(400, upload(new byte[0], "image/png", adminToken).statusCode());
        assertEquals(413, upload(new byte[5 * 1024 * 1024 + 1], "image/png", adminToken).statusCode());
    }

    @Test
    void publicImageUrlCannotReadOutsideTheUploadDirectory() throws Exception {
        Files.createDirectories(UPLOADS);
        Files.writeString(UPLOADS.getParent().resolve("outside-upload.txt"), "outside-upload-secret");
        var response = request("GET", "/api/uploads/images/%2e%2e/outside-upload.txt", null, null);
        assertTrue(List.of(400, 403, 404).contains(response.statusCode()), response.body());
        assertFalse(response.body().contains("outside-upload-secret"));
    }

    @Test
    void legacyMigrationDoesNotInventDatesOrOverwriteCmsChangesOnRestart() {
        Movie legacy = movies.save(new Movie(null, "Dune: Part Two", 166, 2024, genre));
        jdbc.update("UPDATE movies SET featured = NULL WHERE id = ?", legacy.getId());
        transactions.executeWithoutResult(status -> new MovieMediaMigration(movies).run());
        Movie migrated = movies.findById(legacy.getId()).orElseThrow();
        assertTrue(migrated.isFeatured());
        assertNotNull(migrated.getPosterUrl());
        assertEquals("/api/uploads/images/10000000-0000-4000-8000-000000000001.jpg", migrated.getPosterUrl());
        assertNull(migrated.getReleaseDate());
        assertEquals(2024, migrated.getReleaseYear());
        migrated.setFeatured(false);
        migrated.setPosterUrl("https://example.test/changed.jpg");
        movies.save(migrated);
        transactions.executeWithoutResult(status -> new MovieMediaMigration(movies).run());
        migrated = movies.findById(legacy.getId()).orElseThrow();
        assertFalse(migrated.isFeatured());
        assertEquals("https://example.test/changed.jpg", migrated.getPosterUrl());
    }

    @Test
    void existingExternalSamplePosterBecomesLocalWithoutOverwritingCmsMetadata() {
        Movie movie = new Movie(null, "Renamed movie", 166, 2024, genre);
        movie.setPosterUrl("https://image.tmdb.org/t/p/w500/6izwz7rsy95ARzTR3poZ8H6c5pp.jpg");
        movie.setDescription("Description edited in CMS");
        movie.setBannerPosition(77);
        movie.setFeatured(false);
        Long id = movies.save(movie).getId();
        transactions.executeWithoutResult(status -> new MovieMediaMigration(movies).run());
        Movie migrated = movies.findById(id).orElseThrow();
        assertEquals("/api/uploads/images/10000000-0000-4000-8000-000000000001.jpg", migrated.getPosterUrl());
        assertEquals("Renamed movie", migrated.getTitle());
        assertEquals("Description edited in CMS", migrated.getDescription());
        assertEquals(77, migrated.getBannerPosition());
        assertFalse(migrated.isFeatured());
        assertNull(migrated.getReleaseDate());
    }

    private String token(String name, UserRole role) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(name + "@media.test");
        user.setPasswordHash("hash");
        user.setRole(role);
        return auth.issue(users.save(user));
    }

    private Movie movie(String title, LocalDate date, Integer position, boolean featured, boolean image, int day) {
        Movie movie = new Movie(null, title, 100, 2024, genre);
        movie.setReleaseDate(date);
        movie.setBannerPosition(position);
        movie.setFeatured(featured);
        movie.setPosterUrl(image ? "https://example.test/poster.jpg" : null);
        movie = movies.save(movie);
        if (day != 0) addShowtime(movie, day);
        return movie;
    }

    private void addShowtime(Movie movie, int day) {
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(day));
        showtime.setTicketPrice(BigDecimal.TEN);
        showtimes.save(showtime);
    }

    private String body(String title, Integer position, boolean featured) {
        return """
                {"title":"%s","duration":120,"releaseDate":"2026-10-28","genreId":%d,
                 "posterUrl":"https://example.test/poster.jpg","subtitle":"Subtitle","description":"Description",
                 "featured":%s,"bannerPosition":%s}
                """.formatted(title, genre.getId(), featured, position);
    }

    private List<Long> movieIds(String path) throws Exception {
        var response = request("GET", path, null, null);
        assertEquals(200, response.statusCode(), response.body());
        List<Long> ids = new ArrayList<>();
        mapper.readTree(response.body()).get("movies").forEach(movie -> ids.add(movie.get("id").asLong()));
        return ids;
    }

    private HttpRequest httpRequest(String method, String path, String body, String token) {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return builder
                .method(method,
                        body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        return client.send(httpRequest(method, path, body, token), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> upload(byte[] file, String type, String token) throws Exception {
        String boundary = "MovieStoreBoundary";
        byte[] prefix = ("--" + boundary
                + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"poster.png\"\r\nContent-Type: " + type
                + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] suffix = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/uploads/images"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary);
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return client.send(builder.POST(HttpRequest.BodyPublishers.concat(
                HttpRequest.BodyPublishers.ofByteArray(prefix), HttpRequest.BodyPublishers.ofByteArray(file),
                HttpRequest.BodyPublishers.ofByteArray(suffix))).build(), HttpResponse.BodyHandlers.ofString());
    }

    private byte[] png() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", bytes);
        return bytes.toByteArray();
    }
}
