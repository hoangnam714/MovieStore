-- Nâng cấp database cũ (7 bảng) cho đặt vé khách vãng lai, mã giảm giá, VAT và phiên đăng nhập bằng token.
-- Backend tự chạy phần tương đương trong TicketSchemaUpgrade khi khởi động; file này dùng khi cần chạy tay.
-- Chạy một lần, TRƯỚC khi khởi động backend phiên bản mới.

CREATE TABLE IF NOT EXISTS discount_codes (
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

ALTER TABLE tickets
    MODIFY user_id BIGINT NULL,
    ADD COLUMN customer_phone   VARCHAR(15)   NULL AFTER user_id,
    ADD COLUMN discount_code_id BIGINT        NULL AFTER price,
    ADD COLUMN discount_amount  DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER discount_code_id,
    ADD COLUMN vat_amount       DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER discount_amount,
    ADD COLUMN total_amount     DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER vat_amount,
    ADD CONSTRAINT fk_tickets_discount FOREIGN KEY (discount_code_id) REFERENCES discount_codes (id) ON DELETE RESTRICT;

ALTER TABLE users ADD COLUMN phone VARCHAR(15) NULL AFTER email;

CREATE TABLE IF NOT EXISTS auth_tokens (
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    token_hash CHAR(64)    NOT NULL UNIQUE,
    user_id    BIGINT      NOT NULL,
    expires_at DATETIME    NOT NULL,
    CONSTRAINT fk_auth_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Vé cũ được tính đúng giá suất chiếu, chưa có giảm giá/VAT.
UPDATE tickets SET total_amount = price WHERE total_amount = 0;
