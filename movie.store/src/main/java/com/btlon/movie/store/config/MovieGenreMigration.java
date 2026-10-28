package com.btlon.movie.store.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Chuyển liên kết thể loại cũ sang bảng nối một lần trước khi chạy các seeder. */
@Component
@Profile("!test")
@Order(-3)
public class MovieGenreMigration implements CommandLineRunner {
    private final JdbcTemplate jdbc;

    public MovieGenreMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        // Không còn cột cũ nghĩa là database mới hoặc đã chuyển đổi xong.
        var columns = jdbc.queryForRowSet("SELECT * FROM movies WHERE 1 = 0").getMetaData();
        boolean hasOldColumn = false;
        for (String column : columns.getColumnNames()) {
            if (column.equalsIgnoreCase("genre_id")) hasOldColumn = true;
        }
        if (!hasOldColumn) return;

        // Lưu liên kết cũ trước, không tạo trùng nếu lần chạy trước bị gián đoạn.
        jdbc.update("""
                INSERT INTO movie_genre_links (movie_id, genre_id)
                SELECT m.id, m.genre_id FROM movies m
                WHERE m.genre_id IS NOT NULL
                  AND NOT EXISTS (SELECT 1 FROM movie_genre_links link
                                  WHERE link.movie_id = m.id AND link.genre_id = m.genre_id)
                """);

        // Bỏ khóa ngoại và cột cũ để có thể thêm phim chỉ dùng bảng nối.
        List<String> dropForeignKeys = jdbc.execute((ConnectionCallback<List<String>>) connection -> {
            List<String> statements = new ArrayList<>();
            boolean mysql = connection.getMetaData().getDatabaseProductName().equals("MySQL");
            try (var keys = connection.getMetaData().getImportedKeys(connection.getCatalog(), null, "movies")) {
                while (keys.next()) {
                    if (!keys.getString("FKCOLUMN_NAME").equalsIgnoreCase("genre_id")) continue;
                    String name = keys.getString("FK_NAME").replace("`", "``");
                    statements.add("ALTER TABLE movies DROP " + (mysql ? "FOREIGN KEY" : "CONSTRAINT")
                            + " `" + name + "`");
                }
            }
            return statements;
        });
        for (String statement : dropForeignKeys) jdbc.execute(statement);
        jdbc.execute("ALTER TABLE movies DROP COLUMN genre_id");
    }
}
