-- Movie Store — 9 bảng

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
    phone         VARCHAR(15),  -- bắt buộc khi đăng ký; NULL với tài khoản tạo trước khi có SĐT
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER'
);

-- Phiên đăng nhập: chỉ lưu SHA-256 của bearer token, token gốc chỉ trả về cho client.
CREATE TABLE auth_tokens (
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    token_hash CHAR(64)    NOT NULL UNIQUE,
    user_id    BIGINT      NOT NULL,
    expires_at DATETIME    NOT NULL,
    CONSTRAINT fk_auth_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE cinemas (
    id      BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL
);

CREATE TABLE rooms (
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,
    seat_count INT         NOT NULL CHECK (seat_count > 0),
    cinema_id  BIGINT,
    CONSTRAINT fk_rooms_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas (id) ON DELETE RESTRICT
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

CREATE TABLE discount_codes (
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(30)  NOT NULL UNIQUE,
    description      VARCHAR(255),
    discount_percent INT          NOT NULL CHECK (discount_percent BETWEEN 1 AND 100),
    start_date       DATE,
    end_date         DATE,
    usage_limit      INT          CHECK (usage_limit > 0),
    used_count       INT          NOT NULL DEFAULT 0,
    active           BOOLEAN      NOT NULL DEFAULT TRUE
);

-- user_id NULL = khách vãng lai, khi đó customer_phone bắt buộc (kiểm tra ở tầng service).
-- price là giá suất chiếu; total_amount = price - discount_amount + vat_amount.
CREATE TABLE tickets (
    id               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT,
    customer_phone   VARCHAR(15),
    showtime_id      BIGINT        NOT NULL,
    seat_number      INT           NOT NULL,
    price            DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    discount_code_id BIGINT,
    discount_amount  DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    vat_amount       DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (vat_amount >= 0),
    total_amount     DECIMAL(12,2) NOT NULL CHECK (total_amount >= 0),
    booked_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tickets_user     FOREIGN KEY (user_id)     REFERENCES users (id)     ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_discount FOREIGN KEY (discount_code_id) REFERENCES discount_codes (id) ON DELETE RESTRICT,
    CONSTRAINT uq_tickets_showtime_seat UNIQUE (showtime_id, seat_number),
    CONSTRAINT ck_tickets_customer CHECK (user_id IS NOT NULL OR customer_phone IS NOT NULL)
);
