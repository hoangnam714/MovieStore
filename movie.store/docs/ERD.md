# ERD — Movie Store (10 bảng)

```mermaid
erDiagram
    movie_genres ||--o{ movie_genre_links : "genre_id"
    movies ||--|{ movie_genre_links : "movie_id"
    movies ||--o{ showtimes : "movie_id"
    cinemas ||--o{ rooms : "cinema_id"
    rooms ||--o{ showtimes : "room_id"
    users |o--o{ tickets : "user_id (NULL = khách vãng lai)"
    users ||--o{ auth_tokens : "user_id"
    discount_codes |o--o{ tickets : "discount_code_id"
    showtimes ||--o{ tickets : "showtime_id"

    movie_genres {
        bigint id PK
        varchar name UK
    }
    movies {
        bigint id PK
        varchar title
        int duration
        int release_year
    }
    movie_genre_links {
        bigint movie_id PK, FK
        bigint genre_id PK, FK
    }
    users {
        bigint id PK
        varchar full_name
        varchar email UK
        varchar phone
        varchar password_hash
        varchar role
    }
    cinemas {
        bigint id PK
        varchar name UK
        varchar address
    }
    rooms {
        bigint id PK
        varchar name UK
        int seat_count
        bigint cinema_id FK
    }
    showtimes {
        bigint id PK
        bigint movie_id FK
        bigint room_id FK
        datetime start_time
        decimal ticket_price
    }
    auth_tokens {
        bigint id PK
        char token_hash UK
        bigint user_id FK
        datetime expires_at
    }
    discount_codes {
        bigint id PK
        varchar code UK
        varchar description
        int discount_percent
        date start_date
        date end_date
        int usage_limit
        int used_count
        boolean active
    }
    tickets {
        bigint id PK
        bigint user_id FK "NULL khi khách vãng lai"
        varchar customer_phone
        bigint showtime_id FK
        int seat_number
        decimal price
        bigint discount_code_id FK
        decimal discount_amount
        decimal vat_amount
        decimal total_amount
        datetime booked_at
    }
```

Ràng buộc nổi bật: `UNIQUE(showtime_id, seat_number)` trên `tickets`; khóa ngoại `ON DELETE RESTRICT`; `CHECK(user_id IS NOT NULL OR customer_phone IS NOT NULL)` trên `tickets`.
