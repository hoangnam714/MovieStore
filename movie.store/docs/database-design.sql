-- Movie Store — 6 bảng (thiết kế đơn giản, xem THIET_KE_DATABASE_DON_GIAN.md)

CREATE TABLE movie_genres (
    id   BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE movies (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    duration     INT          NOT NULL CHECK (duration > 0),
    release_year INT          NOT NULL,
    genre_id     BIGINT       NOT NULL,
    CONSTRAINT fk_movies_genre FOREIGN KEY (genre_id) REFERENCES movie_genres (id) ON DELETE RESTRICT
);

CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER'
);

CREATE TABLE rooms (
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,
    seat_count INT         NOT NULL CHECK (seat_count > 0)
);

CREATE TABLE showtimes (
    id           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    movie_id     BIGINT        NOT NULL,
    room_id      BIGINT        NOT NULL,
    start_time   DATETIME      NOT NULL,
    ticket_price DECIMAL(12,2) NOT NULL CHECK (ticket_price >= 0),
    CONSTRAINT fk_showtimes_movie FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE RESTRICT,
    CONSTRAINT fk_showtimes_room  FOREIGN KEY (room_id)  REFERENCES rooms (id)  ON DELETE RESTRICT
);

CREATE TABLE tickets (
    id          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    showtime_id BIGINT        NOT NULL,
    seat_number INT           NOT NULL,
    price       DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    booked_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tickets_user     FOREIGN KEY (user_id)     REFERENCES users (id)     ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes (id) ON DELETE RESTRICT,
    CONSTRAINT uq_tickets_showtime_seat UNIQUE (showtime_id, seat_number)
);
