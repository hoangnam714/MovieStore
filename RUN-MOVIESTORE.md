# MovieStore — chạy và kiểm tra

Chạy các lệnh từ thư mục gốc của repository `MovieStore`.

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

## Seed

`CinemaCatalogSeeder` chạy khi khởi động backend, trừ profile test.
Seed bổ sung 8 phim, 3 phòng chiếu mẫu 60 ghế và lịch chiếu cho 7 ngày kể từ ngày khởi động.
Các phòng/lịch cũ được giữ lại; lịch trùng tên phòng và thời gian không được thêm lần nữa.
Suất mới chỉ thêm vào khung giờ không đè lên suất có sẵn, có khoảng nghỉ 15 phút.
Đã chạy seed hai lần: lần đầu thêm 75 suất, lần sau thêm 0 suất.
Cơ sở dữ liệu thực tế sau seed: 8 phim, 5 phòng, 75 suất sắp tới.
Một vé demo cho Interstellar, ghế số 1 (A1), được đặt khi kiểm tra bằng tài khoản khách mẫu.

## Frontend

Mỗi chức năng là một module chứa `index.html` và `style.css` khi có CSS riêng. JavaScript riêng nằm trong thẻ `<script>` cuối `index.html`.

```text
FE/
  customer/
    home/  login/  register/  booking/  my-tickets/
  admin/
    login/  dashboard/  movies/  genres/  rooms/  showtimes/  tickets/
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

Chạy frontend: `node FE/server.cjs`. Chạy backend trong terminal khác: vào `movie.store`, chạy `.\mvnw.cmd spring-boot:run`. Các URL `.html` cũ chuyển tiếp sang module mới và giữ tham số đặt vé.

- HTML, CSS, JavaScript thuần; khách dùng lưới và form Bootstrap 5.3.3.
- Admin dựa trên AdminLTE 3.2 (Bootstrap 4), có sidebar, dashboard và bảng quản lý.
- Không có React/Vue hoặc bước build FE.
- Node `FE/server.cjs` phục vụ file tĩnh ở 8080 và proxy /api tới 5173.
- Ảnh poster/backdrop được nhúng từ TMDB và SensCritique; Bootstrap, AdminLTE, Axios và font tải qua CDN, cần Internet.
- Tìm phim, lọc thể loại/ngày, chọn suất/ghế, đăng ký/đăng nhập và xem vé dùng API thực tế.
- Chỉ là đặt vé demo; chưa có cổng thanh toán online.
- Phân quyền hiện có vẫn là demo ở frontend, chưa có xác thực/phân quyền backend đủ để triển khai công khai.

## Tham khảo thiết kế

- MUBI Film Database: https://mubi.com/en/films — danh mục phim, bộ lọc và trọng tâm hình ảnh.
- AMC Movies: https://www.amctheatres.com/movies?availability=NOW_PLAYING — tham khảo luồng phim/lịch chiếu/đặt vé.
- AdminLTE: https://adminlte.io/themes/v3/ — template quản trị.
- Dune: Part Two: https://www.themoviedb.org/movie/693134-dune-part-two — nguồn ảnh và thông tin phim nổi bật.

## Kiểm tra đã thực hiện

- Backend: 5 test pass.
- Seed thực tế trên MySQL và chạy lại để xác nhận không nhân đôi.
- Đặt vé qua FE proxy thành công; đặt lại cùng ghế trả HTTP 409; đọc được lịch sử vé.
- Kiểm tra trang chủ, tìm kiếm, lịch chiếu, sơ đồ ghế và dashboard admin trên trình duyệt.
- Kiểm tra responsive trang khách ở chiều rộng 390px.

Bản FE cũ đã được sao lưu trong `.codex-backups` ở thư mục gốc project.
