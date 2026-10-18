# Thiết kế database mua vé xem phim — bản đơn giản

Dùng **6 bảng**, giữ lại 2 bảng hiện tại. Phạm vi: một rạp có nhiều phòng, khách chọn phim → suất chiếu → ghế → mua vé. Mỗi vé tương ứng với một ghế của một suất chiếu.

Bản này thay thế đề xuất 8 bảng trong `database-design.sql` về mặt thiết kế. Chưa thay đổi entity Java hay cơ sở dữ liệu.

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
| `genre_id` | BIGINT | Khóa ngoại → `movie_genres.id` |

Giữ nguyên các cột hiện tại. Chưa cần thêm mô tả, trailer hay poster để hoàn thành chức năng mua vé cơ bản.

## 3. `users` — Người dùng

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `full_name` | VARCHAR(100) | Họ tên |
| `email` | VARCHAR(254) | Email đăng nhập, không trùng |
| `password_hash` | VARCHAR(255) | Mật khẩu đã băm |
| `role` | VARCHAR(20) | `CUSTOMER` hoặc `ADMIN` |

Khách hàng đăng nhập để mua và xem vé của mình. Quản trị viên quản lý phim, phòng và suất chiếu.

## 4. `rooms` — Phòng chiếu

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `name` | VARCHAR(50) | Tên phòng, không trùng |
| `seat_count` | INT | Số ghế trong phòng |

Để đơn giản, ghế đánh số từ **1 đến `seat_count`**. Ví dụ phòng có 50 ghế thì khách chọn ghế từ 1 đến 50. Không cần bảng ghế riêng.

## 5. `showtimes` — Suất chiếu

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `movie_id` | BIGINT | Khóa ngoại → `movies.id` |
| `room_id` | BIGINT | Khóa ngoại → `rooms.id` |
| `start_time` | DATETIME | Ngày và giờ bắt đầu chiếu |
| `ticket_price` | DECIMAL(12,2) | Giá một vé |

Ví dụ: phim A, phòng 1, ngày 20/10/2026 lúc 19:00, giá 80.000 đồng.

## 6. `tickets` — Vé đã mua

| Cột | Kiểu dữ liệu | Ý nghĩa |
|---|---|---|
| `id` | BIGINT | Khóa chính, tự tăng |
| `user_id` | BIGINT | Khóa ngoại → `users.id` |
| `showtime_id` | BIGINT | Khóa ngoại → `showtimes.id` |
| `seat_number` | INT | Số ghế khách chọn |
| `price` | DECIMAL(12,2) | Giá vé tại thời điểm mua |
| `booked_at` | DATETIME | Thời điểm mua vé |

**Ràng buộc quan trọng:** `UNIQUE(showtime_id, seat_number)` để một ghế không bị bán hai lần trong cùng suất chiếu. Cùng ghế đó vẫn được bán ở suất chiếu khác.

Mua 3 ghế thì tạo 3 dòng vé trong cùng một transaction: thành công tất cả hoặc không tạo vé nào. Chưa cần bảng đơn hàng riêng. Giá vé được lấy từ suất chiếu trên server và lưu lại để việc đổi giá sau này không làm thay đổi vé cũ.

## Quan hệ giữa các bảng

| Bảng cha | Quan hệ | Bảng con | Khóa ngoại ở bảng con |
|---|---|---|---|
| `movie_genres` | 1 — nhiều | `movies` | `genre_id` |
| `movies` | 1 — nhiều | `showtimes` | `movie_id` |
| `rooms` | 1 — nhiều | `showtimes` | `room_id` |
| `users` | 1 — nhiều | `tickets` | `user_id` |
| `showtimes` | 1 — nhiều | `tickets` | `showtime_id` |

Ví dụ: **Hành động → Phim A → Suất 19:00, phòng 1 → Vé ghế 12 của khách Nguyễn Văn A.**

## Các quy tắc tối thiểu khi lập trình

- Các cột trên đều bắt buộc có giá trị. ID tự tăng, `booked_at` mặc định là thời điểm hiện tại, `role` mặc định là `CUSTOMER`.
- Thời lượng phim và số ghế phải lớn hơn 0; giá vé không âm; tên phim không rỗng.
- Chỉ bán vé trước giờ chiếu. Số ghế phải nằm từ 1 đến `rooms.seat_count` của phòng thuộc suất chiếu đó.
- Đặt ràng buộc chống trùng ghế trong database; nếu hai khách mua cùng lúc thì chỉ một người mua thành công.
- Khi tạo hoặc sửa suất chiếu, không cho trùng thời gian trong cùng phòng. Giờ kết thúc = giờ bắt đầu + thời lượng phim. Khóa dòng phòng trong transaction trước khi kiểm tra và lưu lịch để tránh hai yêu cầu tạo lịch trùng nhau.
- Không đổi phim, phòng hoặc giờ của suất đã có vé; không đổi thời lượng phim hay giảm số ghế phòng nếu làm sai lịch chiếu hoặc vé đã bán.
- Không xóa người dùng hoặc suất chiếu đã có vé; không xóa phim đang có suất chiếu, phòng đang có suất chiếu hoặc thể loại đang có phim. Dùng khóa ngoại `ON DELETE RESTRICT`.

## Kiểm tra lại 2 bảng hiện tại

`Movie` và `MovieGenre` đang ánh xạ đúng quan hệ **một thể loại có nhiều phim** qua `genre_id`. Có thể giữ nguyên cấu trúc cột.

Trong `MovieGenre.java`, khi triển khai mô hình này nên bỏ `CascadeType.REMOVE` (đang nằm trong `CascadeType.ALL`) và `orphanRemoval=true` để tránh tự xóa phim khi xóa thể loại hoặc bỏ phim khỏi danh sách. Nếu vẫn tạo phim cùng thể loại, có thể giữ cascade `PERSIST` và `MERGE`. Cần sửa helper `removeMovie()` và test liên quan cho phù hợp vì phim bắt buộc phải có thể loại.

## Giới hạn để giữ dự án đơn giản

Bản này ghi nhận vé mua thành công ngay, chưa tích hợp thanh toán thật, giữ ghế tạm, hủy/hoàn vé, đơn hàng, ghế VIP, mã giảm giá hoặc nhiều chi nhánh rạp. Có thể làm màn hình chọn nhiều ghế và danh sách vé đã mua với đúng 6 bảng trên.
