package com.btlon.movie.store.config;

import com.btlon.movie.store.entity.Movie;
import com.btlon.movie.store.entity.MovieGenre;
import com.btlon.movie.store.entity.Room;
import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.entity.UserRole;
import com.btlon.movie.store.repository.MovieGenreRepository;
import com.btlon.movie.store.repository.MovieRepository;
import com.btlon.movie.store.repository.RoomRepository;
import com.btlon.movie.store.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("!test")
public class DemoDataLoader {
    @Bean
    CommandLineRunner seedDemoData(UserRepository users, MovieGenreRepository genres,
                                   MovieRepository movies, RoomRepository rooms) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return args -> {
            if (!users.existsByEmail("admin@moviestore.vn")) {
                User admin = new User();
                admin.setFullName("Quản trị rạp");
                admin.setEmail("admin@moviestore.vn");
                admin.setPasswordHash(encoder.encode("admin123"));
                admin.setRole(UserRole.ADMIN);
                users.save(admin);
            }
            if (!users.existsByEmail("khach@moviestore.vn")) {
                User customer = new User();
                customer.setFullName("Nguyễn Văn A");
                customer.setEmail("khach@moviestore.vn");
                customer.setPasswordHash(encoder.encode("khach123"));
                customer.setRole(UserRole.CUSTOMER);
                users.save(customer);
            }
            if (genres.count() == 0) {
                MovieGenre action = genres.save(new MovieGenre(null, "Hành động"));
                MovieGenre comedy = genres.save(new MovieGenre(null, "Hài"));
                movies.save(new Movie(null, "Avengers: Endgame", 181, 2019, action));
                movies.save(new Movie(null, "Mai", 131, 2024, comedy));
            }
            if (rooms.count() == 0) {
                rooms.save(new Room("Phòng 1", 50));
                rooms.save(new Room("Phòng 2", 80));
            }
        };
    }
}
