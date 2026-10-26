package com.btlon.movie.store.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Brings an existing MySQL schema in line with guest booking and ticket pricing.
 * Hibernate ddl-auto=update adds new columns but never relaxes NOT NULL on old ones.
 * Same statements as docs/migration-guest-booking-discount.sql; safe to run on every start.
 */
@Component
@Profile("!test")
@Order(-1)
public class TicketSchemaUpgrade implements CommandLineRunner {
    private final JdbcTemplate jdbc;

    public TicketSchemaUpgrade(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void run(String... args) {
        String userIdNullable = jdbc.queryForObject(
                "SELECT IS_NULLABLE FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tickets' AND COLUMN_NAME = 'user_id'",
                String.class);
        if ("NO".equals(userIdNullable)) {
            jdbc.execute("ALTER TABLE tickets MODIFY user_id BIGINT NULL");
        }
        // Tickets booked before pricing existed were paid at the showtime price.
        jdbc.update("UPDATE tickets SET total_amount = price "
                + "WHERE total_amount = 0 AND discount_amount = 0 AND vat_amount = 0 AND price > 0");
    }
}
