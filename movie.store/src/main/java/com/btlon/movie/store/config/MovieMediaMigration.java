package com.btlon.movie.store.config;

import com.btlon.movie.store.entity.Movie;
import com.btlon.movie.store.repository.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/** One-time migration of the old frontend catalog; never invent release dates. */
@Component
@Profile("!test")
@Order(-2)
public class MovieMediaMigration implements CommandLineRunner {
    private record Details(String image, String subtitle, String description, Integer position) {
    }

    private static final Map<String, String> LOCAL_FILES = Map.of(
            "Dune: Part Two", "10000000-0000-4000-8000-000000000001.jpg",
            "Inside Out 2", "10000000-0000-4000-8000-000000000002.jpg",
            "Avengers: Endgame", "10000000-0000-4000-8000-000000000003.jpg",
            "Mai", "10000000-0000-4000-8000-000000000004.jpg",
            "Interstellar", "10000000-0000-4000-8000-000000000005.jpg",
            "Spirited Away", "10000000-0000-4000-8000-000000000006.jpg",
            "The Grand Budapest Hotel", "10000000-0000-4000-8000-000000000007.jpg",
            "Your Name", "10000000-0000-4000-8000-000000000008.jpg");

    private static final Map<String, Details> CATALOG = Map.of(
            "Dune: Part Two", new Details("https://image.tmdb.org/t/p/w500/6izwz7rsy95ARzTR3poZ8H6c5pp.jpg",
                    "HÀNH TRÌNH TRÊN HÀNH TINH CÁT",
                    "Paul Atreides cùng người Fremen viết tiếp cuộc chiến trên hành tinh cát Arrakis.", 50),
            "Inside Out 2", new Details("https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg",
                    "THẾ GIỚI CẢM XÚC ĐẦY SẮC MÀU",
                    "Một chuyến phiêu lưu rực rỡ khi những cảm xúc mới xuất hiện trong tâm trí Riley.", 40),
            "Avengers: Endgame", new Details("https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg",
                    "HỒI KẾT CỦA NHỮNG ANH HÙNG", "Các siêu anh hùng tập hợp cho trận chiến quyết định của cả vũ trụ.",
                    30),
            "Mai", new Details("https://media.senscritique.com/media/000021934906/0/mai.jpg",
                    "MỘT CÂU CHUYỆN VỀ TÌNH YÊU", "Một câu chuyện về tình yêu, hy vọng và những khởi đầu mới.", 20),
            "Interstellar", new Details("https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                    "VƯỢT QUA KHÔNG GIAN VÀ THỜI GIAN",
                    "Băng qua những vì sao trong hành trình tìm kiếm tương lai cho nhân loại.", 10),
            "Spirited Away",
            new Details("https://image.tmdb.org/t/p/w500/39wmItIWsg5sZMyRUHLkWBcuVCM.jpg", null, null, null),
            "The Grand Budapest Hotel",
            new Details("https://image.tmdb.org/t/p/w500/eKi8dIrr8voobbaGzDpe8w0PVbC.jpg", null, null, null),
            "Your Name",
            new Details("https://image.tmdb.org/t/p/w500/q719jXXEzOoYaps6babgKnONONX.jpg", null, null, null));

    private final MovieRepository movies;

    public MovieMediaMigration(MovieRepository movies) {
        this.movies = movies;
    }

    @Override
    @Transactional
    public void run(String... args) {
        int featuredCount = movies.findByFeaturedTrue().size();
        for (Movie movie : movies.findByFeaturedIsNull()) {
            applyCatalog(movie, featuredCount < 5);
            if (movie.isFeatured()) featuredCount++;
        }
        // Also upgrade databases that already ran the previous URL-based migration.
        // Match only original sample URLs; never replace custom uploads or other CMS changes.
        CATALOG.forEach((title, details) -> movies.findByPosterUrl(details.image()).forEach(movie ->
                movie.setPosterUrl("/api/uploads/images/" + LOCAL_FILES.get(title))));
    }

    public static void applyCatalog(Movie movie, boolean allowFeatured) {
        Details details = CATALOG.get(movie.getTitle());
        movie.setFeatured(false);
        if (details == null) return;
        movie.setPosterUrl("/api/uploads/images/" + LOCAL_FILES.get(movie.getTitle()));
        movie.setSubtitle(details.subtitle());
        movie.setDescription(details.description());
        movie.setBannerPosition(details.position());
        movie.setFeatured(allowFeatured && details.position() != null);
    }
}
