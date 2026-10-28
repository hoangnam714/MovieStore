package com.btlon.movie.store.config;

import com.btlon.movie.store.entity.*;
import com.btlon.movie.store.repository.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Additive local demo catalog. Existing movies, tickets and room schedules are
 * preserved.
 */
@Component
@Profile("!test")
@Order(10)
public class CinemaCatalogSeeder implements CommandLineRunner {
    private final MovieRepository movies;
    private final MovieGenreRepository genres;
    private final RoomRepository rooms;
    private final ShowtimeRepository showtimes;
    private final CinemaRepository cinemas;

    public CinemaCatalogSeeder(MovieRepository movies, MovieGenreRepository genres,
                               RoomRepository rooms, ShowtimeRepository showtimes, CinemaRepository cinemas) {
        this.movies = movies;
        this.genres = genres;
        this.rooms = rooms;
        this.showtimes = showtimes;
        this.cinemas = cinemas;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Cinema downtown = cinemas.findAll().stream().filter(c -> c.getName().equals("Movie Store Quận 1"))
                .findFirst()
                .orElseGet(() -> cinemas.save(new Cinema("Movie Store Quận 1", "12 Nguyễn Huệ, Quận 1, TP.HCM")));
        Cinema riverside = cinemas.findAll().stream().filter(c -> c.getName().equals("Movie Store Thủ Đức"))
                .findFirst().orElseGet(
                        () -> cinemas.save(new Cinema("Movie Store Thủ Đức", "25 Võ Văn Ngân, TP. Thủ Đức, TP.HCM")));
        rooms.findAll().stream().filter(r -> r.getCinema() == null).forEach(r -> {
            r.setCinema(r.getName().startsWith("Nova") ? riverside : downtown);
            rooms.save(r);
        });
        Map<String, MovieGenre> genreMap = new HashMap<>();
        genres.findAll().forEach(g -> genreMap.put(g.getName(), g));
        String[][] catalog = {
                {"Dune: Part Two", "166", "2024", "Khoa học viễn tưởng"},
                {"Inside Out 2", "96", "2024", "Hoạt hình"},
                {"Mai", "131", "2024", "Tình cảm"},
                {"Interstellar", "169", "2014", "Khoa học viễn tưởng"},
                {"Spirited Away", "125", "2001", "Hoạt hình"},
                {"Avengers: Endgame", "181", "2019", "Hành động"},
                {"The Grand Budapest Hotel", "100", "2014", "Hài"},
                {"Your Name", "106", "2016", "Hoạt hình"}
        };
        Map<String, Movie> movieMap = new HashMap<>();
        movies.findAll().forEach(m -> movieMap.put(m.getTitle(), m));
        List<Movie> featured = new ArrayList<>();
        int bannerCount = movies.findByFeaturedTrue().size();
        for (String[] entry : catalog) {
            MovieGenre genre = genreMap.computeIfAbsent(entry[3], name -> genres.save(new MovieGenre(null, name)));
            Movie movie = movieMap.get(entry[0]);
            if (movie == null) {
                movie = new Movie(null, entry[0], Integer.parseInt(entry[1]), Integer.parseInt(entry[2]), genre);
                MovieMediaMigration.applyCatalog(movie, bannerCount < 5);
                if (movie.isFeatured()) bannerCount++;
                movie = movies.save(movie);
            }
            featured.add(movie);
        }
        List<Room> seedRooms = new ArrayList<>();
        List<Room> existingRooms = rooms.findAll();
        for (String name : List.of("Aurora · 2D", "Nova · Dolby", "Luna · Premium")) {
            Room room = existingRooms.stream().filter(r -> r.getName().equals(name)).findFirst()
                    .orElseGet(() -> {
                        Room created = new Room(name, 60);
                        created.setCinema(name.startsWith("Nova") ? riverside : downtown);
                        return rooms.save(created);
                    });
            seedRooms.add(room);
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Bangkok"));
        int added = 0;
        for (int day = 0; day < 7; day++) {
            for (int roomIndex = 0; roomIndex < seedRooms.size(); roomIndex++) {
                Room room = seedRooms.get(roomIndex);
                List<Showtime> scheduled = new ArrayList<>(showtimes.findByRoom_Id(room.getId()));
                for (int slot = 0; slot < 4; slot++) {
                    LocalDateTime start = now.toLocalDate().plusDays(day).atTime(9 + slot * 4, roomIndex * 10);
                    if (!start.isAfter(now)) continue;
                    Movie movie = featured.get((day + roomIndex * 2 + slot) % featured.size());
                    boolean overlaps = scheduled.stream()
                            .anyMatch(s -> start.isBefore(s.getStartTime().plusMinutes(s.getMovie().getDuration() + 15))
                                    && s.getStartTime().isBefore(start.plusMinutes(movie.getDuration() + 15)));
                    if (overlaps) continue;
                    Showtime showtime = new Showtime();
                    showtime.setMovie(movie);
                    showtime.setRoom(room);
                    showtime.setStartTime(start);
                    showtime.setTicketPrice(BigDecimal.valueOf(85000L + roomIndex * 20000L + (slot >= 2 ? 10000 : 0)));
                    scheduled.add(showtimes.save(showtime));
                    added++;
                }
            }
        }
        System.out.println("MovieStore demo catalog ready: " + featured.size() + " movies, " + seedRooms.size()
                + " demo rooms, " + added + " new showtimes.");
    }
}
