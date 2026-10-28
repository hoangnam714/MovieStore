# Thiết kế database mua vé xem phim — bản đơn giản

Dùng **10 bảng**, gồm bảng nối phim/thể loại. Phạm vi: nhiều cụm rạp, mỗi cụm có nhiều phòng; khách chọn cụm rạp → phim → suất chiếu → ghế → đặt vé. Mỗi vé tương ứng với một ghế của một suất chiếu.

Schema SQL: `database-design.sql`, ERD: `ERD.md`. Entity Java và API bám theo 10 bảng.

## 1. `movie_genres` — Thể loại phim (đã có)

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `name` | VARCHAR(100) | Tên thể loại, không trùng |

Ví dụ: Hành động, Hài, Kinh dị. Mỗi phim chọn một thể loại.

## 2. `movies` — Phim (đã có)

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `title` | VARCHAR(150) | Tên phim |
| `duration` | INT | Thời lượng, tính bằng phút |
| `release_year` | INT | Năm phát hành |
| `genres` | Quan hệ N:N | Qua bảng nối `movie_genre_links` |

Giữ nguyên các cột hiện tại. Chưa cần thêm mô tả, trailer hay poster để hoàn thành chức năng mua vé cơ bản.

## 3. `users` — Người dùng

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `full_name` | VARCHAR(100) | Họ tên |
| `email` | VARCHAR(254) | Email đăng nhập, không trùng |
| `phone` | VARCHAR(15) | Số điện thoại di động, bắt buộc khi đăng ký (NULL với tài khoản cũ). Vé của thành viên lưu SĐT này vào `tickets.customer_phone` |
| `password_hash` | VARCHAR(255) | Mật khẩu đã băm |
| `role` | VARCHAR(20) | `CUSTOMER` hoặc `ADMIN` |

Khách hàng đăng nhập để mua và xem vé của mình. Quản trị viên quản lý phim, phòng và suất chiếu.

## 4. `cinemas` — Cụm rạp

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `name` | VARCHAR(100) | Tên cụm rạp, không trùng |
| `address` | VARCHAR(255) | Địa chỉ cụm rạp |

## 5. `rooms` — Phòng chiếu

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `name` | VARCHAR(50) | Tên phòng, không trùng |
| `seat_count` | INT | Số ghế trong phòng |
| `cinema_id` | BIGINT | Khóa ngoại → `cinemas.id`; nullable để nâng cấp dữ liệu phòng cũ |

Để đơn giản, ghế đánh số từ **1 đến `seat_count`**. Ví dụ phòng có 50 ghế thì khách chọn ghế từ 1 đến 50. Không cần bảng ghế riêng.

## 6. `showtimes` — Suất chiếu

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `movie_id` | BIGINT | Khóa ngoại → `movies.id` |
| `room_id` | BIGINT | Khóa ngoại → `rooms.id` |
| `start_time` | DATETIME | Ngày và giờ bắt đầu chiếu |
| `ticket_price` | DECIMAL(12,2) | Giá một vé |

Ví dụ: phim A, phòng 1, ngày 20/10/2026 lúc 19:00, giá 80.000 đồng.

## 7. `tickets` — Vé đã mua

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `user_id` | BIGINT | Khóa ngoại → `users.id`; NULL nếu khách vãng lai |
| `customer_phone` | VARCHAR(15) | Số điện thoại; bắt buộc khi `user_id` NULL |
| `showtime_id` | BIGINT | Khóa ngoại → `showtimes.id` |
| `seat_number` | INT | Số ghế khách chọn |
| `price` | DECIMAL(12,2) | Giá suất chiếu tại thời điểm mua, chưa giảm, chưa VAT |
| `discount_code_id` | BIGINT | Khóa ngoại → `discount_codes.id`; NULL nếu không dùng mã |
| `discount_amount` | DECIMAL(12,2) | Tiền giảm (thành viên 5% + mã giảm giá) |
| `vat_amount` | DECIMAL(12,2) | Thuế VAT tính trên giá sau giảm |
| `total_amount` | DECIMAL(12,2) | Số tiền khách trả = `price - discount_amount + vat_amount` |
| `booked_at` | DATETIME | Thời điểm mua vé |

**Ràng buộc quan trọng:** `UNIQUE(showtime_id, seat_number)` để một ghế không bị bán hai lần trong cùng suất chiếu. Cùng ghế đó vẫn được bán ở suất chiếu khác.

Mua 3 ghế thì tạo 3 dòng vé trong cùng một transaction: thành công tất cả hoặc không tạo vé nào. Chưa cần bảng đơn hàng riêng. Giá vé được lấy từ suất chiếu trên server và lưu lại để việc đổi giá sau này không làm thay đổi vé cũ.

Cách tính giá cho mỗi vé (làm tròn đến đồng):

1. Tỷ lệ giảm = 5% nếu khách đã đăng nhập + % của mã giảm giá (tối đa 100%).
2. `discount_amount = price × tỷ lệ giảm`.
3. `vat_amount = (price - discount_amount) × VAT` (mặc định 10%, cấu hình `moviestore.pricing.vat-percent`).
4. `total_amount = price - discount_amount + vat_amount`. Doanh thu là tổng `total_amount`.

## 8. `discount_codes` — Mã giảm giá

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `code` | VARCHAR(30) | Mã khách nhập, duy nhất, lưu chữ in hoa |
| `description` | VARCHAR(255) | Mô tả chương trình |
| `discount_percent` | INT | Phần trăm giảm, từ 1 đến 100 |
| `start_date`, `end_date` | DATE | Thời gian hiệu lực; NULL = không giới hạn |
| `usage_limit` | INT | Số lượt dùng tối đa; NULL = không giới hạn |
| `used_count` | INT | Số lượt đã dùng (mỗi lần đặt vé tính 1 lượt) |
| `active` | BOOLEAN | Bật/tắt mã |

Mã đã được dùng trong vé thì không xóa, chỉ tắt `active`.

## 9. `auth_tokens` — Phiên đăng nhập

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `token_hash` | CHAR(64) | SHA-256 của token, duy nhất. Token gốc chỉ trả về khi đăng nhập/đăng ký |
| `user_id` | BIGINT | Khóa ngoại → `users.id` |
| `expires_at` | DATETIME | Hết hạn sau 12 giờ (`moviestore.auth.token-hours`) |

Client gửi `Authorization: Bearer <token>`. Backend xác định người đặt vé và quyền ADMIN từ token, không tin `userId` trong body. Đăng xuất xóa dòng token.

## Quan hệ giữa các bảng

| Bảng cha | Quan hệ | Bảng con | Khóa ngoại ở bảng con |
|---|---|---|---|
| `movies` | nhiều — nhiều | `movie_genres` | `movie_genre_links(movie_id, genre_id)` |
| `movies` | 1 — nhiều | `showtimes` | `movie_id` |
| `cinemas` | 1 — nhiều | `rooms` | `cinema_id` |
| `rooms` | 1 — nhiều | `showtimes` | `room_id` |
| `users` | 0..1 — nhiều | `tickets` | `user_id` |
| `users` | 1 — nhiều | `auth_tokens` | `user_id` |
| `discount_codes` | 0..1 — nhiều | `tickets` | `discount_code_id` |
| `showtimes` | 1 — nhiều | `tickets` | `showtime_id` |

Ví dụ: **Hành động → Phim A → Suất 19:00, phòng 1 → Vé ghế 12 của khách Nguyễn Văn A.**

## Các quy tắc tối thiểu khi lập trình

- Các cột đều bắt buộc có giá trị, trừ `rooms.cinema_id` của dữ liệu cũ trong lúc nâng cấp, `tickets.user_id`/`customer_phone`/`discount_code_id` và các cột tùy chọn của `discount_codes`. Khách vãng lai phải nhập số điện thoại. API tạo/sửa phòng mới yêu cầu `cinema_id`. ID tự tăng, `booked_at` mặc định là thời điểm hiện tại, `role` mặc định là `CUSTOMER`.
- Thời lượng phim và số ghế phải lớn hơn 0; giá vé không âm; tên phim không rỗng.
- Chỉ bán vé trước giờ chiếu. Số ghế phải nằm từ 1 đến `rooms.seat_count` của phòng thuộc suất chiếu đó.
- Một lần đặt tối đa 8 ghế. Nếu đặt từ 2 ghế trở lên, các ghế phải nối với nhau theo 4 hướng: trên, dưới, trái, phải (khớp với `TicketService`).
- Đặt ràng buộc chống trùng ghế trong database; nếu hai khách mua cùng lúc thì chỉ một người mua thành công.
- Khi tạo hoặc sửa suất chiếu, không cho trùng thời gian trong cùng phòng. Giờ kết thúc = giờ bắt đầu + thời lượng phim. Khóa dòng phòng trong transaction trước khi kiểm tra và lưu lịch để tránh hai yêu cầu tạo lịch trùng nhau.
- Không đổi phim, phòng hoặc giờ của suất đã có vé; không đổi thời lượng phim hay giảm số ghế phòng nếu làm sai lịch chiếu hoặc vé đã bán.
- Không xóa người dùng hoặc suất chiếu đã có vé; không xóa phim đang có suất chiếu, phòng đang có suất chiếu hoặc thể loại đang có phim. Dùng khóa ngoại `ON DELETE RESTRICT`.

## Kiểm tra lại 2 bảng hiện tại

`Movie` và `MovieGenre` ánh xạ `@ManyToMany` qua `movie_genre_links`. `Movie` quản lý bảng nối; mỗi phim cần ít nhất một thể loại theo kiểm tra của service. Không cascade xóa phim hoặc thể loại khi xóa liên kết. Thể loại đang có phim bị chặn xóa.

Bảng nối có hai cột `movie_id`, `genre_id`, khóa duy nhất trên cặp này để không gán trùng. Database cũ được `MovieGenreMigration` sao chép liên kết rồi bỏ cột `movies.genre_id` khi khởi động BE.

## Giới hạn để giữ dự án đơn giản

Bản này ghi nhận vé đặt thành công ngay, chưa tích hợp thanh toán thật, giữ ghế tạm, hoàn tiền, đơn hàng, ghế VIP hoặc mã giảm giá. Admin có thể hủy một vé chưa đến giờ chiếu; thao tác xóa vé và mở lại ghế, chưa lưu lịch sử hủy.
