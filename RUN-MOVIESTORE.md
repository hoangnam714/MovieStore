# MovieStore — chạy và kiểm tra

Chạy các lệnh từ thư mục gốc của repository `MovieStore`.

Hướng dẫn khởi động nhanh bằng terminal và `Run Task` trong Cursor / VS Code: [README.md](README.md).

## Chạy trong Eclipse

1. Chọn project `movie.store`, nhấn F5.
2. Run > Run Configurations > Launch Group > MovieStore - Full Stack > Run.
3. Khách hàng: http://localhost:8080/customer/home/index.html
4. Admin: http://localhost:8080/admin/login/index.html
5. Backend: http://localhost:5173/api (ví dụ GET /api/movies).

## Tài khoản mẫu local

| Vai trò | Email | Mật khẩu |
| --- | --- | --- |
| Admin | admin@moviestore.vn | admin123 |
| Khách | khach@moviestore.vn | khach123 |

Mã giảm giá mẫu: `WELCOME10` (giảm 10%, không giới hạn lượt).

## Đặt vé, giảm giá và VAT

- Khách chưa đăng nhập vẫn đặt được vé nhưng bắt buộc nhập số điện thoại di động Việt Nam (lưu vào `tickets.customer_phone`).
- Khách đã đăng nhập được giảm 5%; mã giảm giá cộng thêm phần trăm của mã. VAT 10% tính trên giá sau giảm.
- Tỷ lệ giảm thành viên và VAT cấu hình trong `application.properties` (`moviestore.pricing.*`).
- CMS > Mã giảm giá (`/admin/discounts/index.html`) để thêm, sửa, tắt hoặc xóa mã. Mã đã dùng cho vé chỉ tắt được, không xóa được.
- Database cũ được `TicketSchemaUpgrade` nâng cấp tự động khi backend khởi động; script chạy tay: `movie.store/docs/migration-guest-booking-discount.sql`.

## Đăng nhập và phân quyền API

- `POST /api/users/login` và `/register` trả về `token`; frontend lưu trong `sessionStorage` và tự gửi header `Authorization: Bearer <token>` (`assets/js/common.js`). Token hết hạn sau 12 giờ; `POST /api/users/logout` thu hồi token.
- Backend lấy người đặt vé từ token (`@CurrentUser`), bỏ qua mọi `userId` trong request. Vé của khách: `GET /api/tickets/me`.
- API có `@AdminOnly` (ghi phim/thể loại/phòng/rạp/suất chiếu, toàn bộ mã giảm giá, xem tất cả vé, hủy vé, xem/xóa user) trả 401 khi chưa đăng nhập, 403 khi không phải ADMIN.
- Gọi API admin bằng Postman: đăng nhập lấy `token`, rồi thêm header `Authorization: Bearer <token>`.

## Mật khẩu

- Quên mật khẩu: `/customer/forgot-password/index.html` (link ở trang đăng nhập khách và admin). Nhập email + số điện thoại đã đăng ký + mật khẩu mới; khớp thì đặt lại ngay (`POST /api/users/reset-password`) và đăng xuất mọi phiên của tài khoản. Không gửi email.
- Tài khoản tạo trước khi bắt buộc số điện thoại (cột `users.phone` trống) không tự đặt lại được.
- Đổi mật khẩu khi đã đăng nhập: bấm tên trên header → `/customer/account/index.html` (`PUT /api/users/me/password`). Các phiên đăng nhập khác bị đăng xuất.
- Tài khoản mẫu `khach@moviestore.vn` có SĐT `0901234567` khi được seed mới.

## Seed

`CinemaCatalogSeeder` chạy khi khởi động backend, trừ profile test.
Seed bổ sung 8 phim, 2 cụm rạp, 3 phòng chiếu mẫu 60 ghế và lịch chiếu cho 7 ngày kể từ ngày khởi động.
Các phòng/lịch cũ được giữ lại; lịch trùng tên phòng và thời gian không được thêm lần nữa.
Suất mới chỉ thêm vào khung giờ không đè lên suất có sẵn, có khoảng nghỉ 15 phút.
Số suất mới phụ thuộc giờ chạy và các suất đã có trong database; seed giữ nguyên vé và lịch cũ.

## Frontend

Mỗi chức năng là một module chứa `index.html` và `style.css` khi có CSS riêng. JavaScript riêng nằm trong thẻ `<script>` cuối `index.html`.

```text
FE/
  customer/
    home/  login/  register/  forgot-password/  account/  booking/  my-tickets/
  admin/
    login/  dashboard/  movies/  genres/  cinemas/  rooms/  showtimes/  discounts/  tickets/
  assets/
    css/  # customer.css và admin.css
    js/   # common.js, customer-layout.js và admin-layout.js
  server.cjs
```

HTML chứa bố cục và form của trang; thẻ `<script>` cuối trang tải dữ liệu, dựng danh sách động và xử lý sự kiện.

CSS riêng nằm cạnh HTML. Trang đăng nhập và đăng ký khách dùng chung CSS form trong `assets/css/customer.css`, không tạo file CSS riêng rỗng.

`assets/js/common.js` gom các hàm gọi API, quản lý phiên đăng nhập và định dạng. Hai file layout chứa header/footer khách và sidebar admin.

Các lời gọi API dùng `.then()` và `.catch()`. Hàm lấy dữ liệu dùng tiền tố `get`; hàm xử lý thao tác và cập nhật giao diện dùng tiền tố `on`.

Các thư viện CDN vẫn khai báo trực tiếp để giữ thứ tự tải rõ ràng. Trang khách và trang đăng nhập admin chỉ dùng Axios. Các trang quản lý dùng thêm jQuery, Bootstrap 4 và AdminLTE 3; không nạp cùng thư viện hai lần trong một trang.

Chạy frontend: `node FE/server.cjs`. Chạy backend trong terminal khác: vào `movie.store`, chạy `bash mvnw spring-boot:run` (macOS/Linux) hoặc `.\mvnw.cmd spring-boot:run` (Windows). Các URL `.html` cũ chuyển tiếp sang module mới và giữ tham số đặt vé.

- HTML, CSS, JavaScript thuần; khách dùng lưới và form Bootstrap 5.3.3.
- Admin dựa trên AdminLTE 3.2 (Bootstrap 4), có sidebar, dashboard và bảng quản lý.
- Không có React/Vue hoặc bước build FE.
- Node `FE/server.cjs` phục vụ file tĩnh ở 8080 và proxy /api tới 5173.
- Ảnh poster/backdrop được nhúng từ TMDB và SensCritique; Bootstrap, AdminLTE, Axios và font tải qua CDN, cần Internet.
- Tìm phim (không phân biệt dấu), lọc thể loại/ngày/cụm rạp, xóa nhanh bộ lọc, chọn suất/ghế, đăng ký/đăng nhập và xem vé dùng API thực tế.
- Mỗi lần đặt tối đa 8 ghế liền nhau trên cùng một hàng; FE hướng dẫn ngay khi chọn và backend xác thực lại trước khi lưu.
- Admin quản lý cụm rạp, phòng, phim và suất chiếu; form suất chiếu tìm phim theo tên, chọn phòng theo cụm rạp, có lọc và phân trang danh sách, sửa/xóa tại chỗ. Trang phim có tìm kiếm và sửa. Trang vé có lọc/hủy từng vé trước giờ chiếu. Hủy vé xóa bản ghi đặt chỗ và mở lại ghế; chưa có lịch sử hủy hoặc hoàn tiền.
- Chỉ là đặt vé demo; chưa có cổng thanh toán online.
- Phân quyền hiện có vẫn là demo ở frontend, chưa có xác thực/phân quyền backend đủ để triển khai công khai.

## Tham khảo thiết kế

- MUBI Film Database: https://mubi.com/en/films — danh mục phim, bộ lọc và trọng tâm hình ảnh.
- AMC Movies: https://www.amctheatres.com/movies?availability=NOW_PLAYING — tham khảo luồng phim/lịch chiếu/đặt vé.
- AdminLTE: https://adminlte.io/themes/v3/ — template quản trị.
- Dune: Part Two: https://www.themoviedb.org/movie/693134-dune-part-two — nguồn ảnh và thông tin phim nổi bật.

## Kiểm tra đã thực hiện

- Backend: 6 test pass (`bash mvnw test`), gồm luồng đặt ghế, xung đột ghế và mở lại ghế sau khi hủy.
- Seed thực tế trên MySQL và chạy lại để xác nhận không nhân đôi.
- Đặt vé qua FE proxy thành công; đặt lại cùng ghế trả HTTP 409; đọc được lịch sử vé.
- Kiểm tra trang chủ, tìm kiếm, lịch chiếu, sơ đồ ghế và dashboard admin trên trình duyệt.
- Kiểm tra responsive trang khách ở chiều rộng 390px.

Bản FE cũ đã được sao lưu trong `.codex-backups` ở thư mục gốc project.
