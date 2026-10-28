# MovieStore

Ứng dụng đặt vé xem phim gồm backend Spring Boot (`movie.store`) và frontend HTML/CSS/JavaScript thuần (`FE`). Có thể chạy bằng terminal tích hợp trong Cursor, VS Code hoặc IDE khác.

Chạy nhanh từ thư mục gốc `MovieStore` trong hai terminal PowerShell riêng:

- **BE:** `.\movie.store\mvnw.cmd -f .\movie.store\pom.xml spring-boot:run`
- **FE:** `node FE/server.cjs`

## Chuẩn bị

- JDK 17 trở lên và Node.js 18 trở lên.
- MySQL đang chạy ở `localhost:3306`. Tạo database nếu chưa có:

  ```sql
  CREATE DATABASE IF NOT EXISTS movie_store_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```

- Kiểm tra URL, tài khoản và mật khẩu MySQL trong [application.properties](movie.store/src/main/resources/application.properties). Hibernate sẽ tạo/cập nhật bảng khi backend khởi động.
- Mở **thư mục gốc `MovieStore`** trong Cursor hoặc VS Code để các đường dẫn bên dưới hoạt động.

## Chạy bằng terminal trong Cursor / VS Code

Mở hai terminal riêng (`Terminal → New Terminal`). Chạy backend trước, rồi chạy frontend. Giữ cả hai terminal mở trong lúc sử dụng.

### macOS / Linux

Terminal 1 — backend:

```bash
cd movie.store
bash ./mvnw spring-boot:run
```

Terminal 2 — frontend, từ thư mục gốc `MovieStore`:

```bash
node FE/server.cjs
```

### Windows (PowerShell)

Terminal 1 — backend:

```powershell
cd movie.store
.\mvnw.cmd spring-boot:run
```

Terminal 2 — frontend, từ thư mục gốc `MovieStore`:

```powershell
node FE/server.cjs
```

Truy cập [trang khách](http://localhost:8080/customer/home/index.html) hoặc [trang admin](http://localhost:8080/admin/login/index.html). Backend dùng cổng `5173`; frontend dùng cổng `8080` và chuyển tiếp `/api` sang backend. Dừng mỗi server bằng `Ctrl+C` trong terminal tương ứng.

## Chạy bằng nút Run Task trong Cursor / VS Code

Repo có sẵn [.vscode/tasks.json](.vscode/tasks.json). Chọn `Terminal → Run Task… → MovieStore: Full Stack` để chạy backend và frontend. Bạn cũng có thể chọn `MovieStore: Backend` hoặc `MovieStore: Frontend` để chạy riêng từng phần. Lần đầu dùng Maven Wrapper có thể cần tải Maven và dependencies.

## Poster phim và banner trang chủ

Trong **CMS → Phim**, quản trị viên có thể chọn poster, xem trước ảnh, nhập ngày phát hành, tiêu đề phụ (tối đa 100 ký tự), mô tả (tối đa 500 ký tự) và bật/tắt banner. Độ ưu tiên banner không bắt buộc; nếu nhập thì từ 0 đến 99, số càng lớn hiển thị càng sớm. Tối đa 5 phim được bật banner cùng lúc, kể cả phim hiện chưa có suất chiếu tương lai.

Backend cung cấp `GET /api/movies/banners`: lấy phim đã bật banner và có ảnh, kể cả phim chưa có suất chiếu hoặc chỉ có suất chiếu đã qua; sắp xếp theo độ ưu tiên giảm dần (để trống xếp sau), ngày phát hành giảm dần, rồi ID giảm dần; trả tối đa 5 phim. Frontend hiển thị đúng danh sách và thứ tự API trả về, không tự chọn thêm phim.

Upload ảnh qua `POST /api/uploads/images` (multipart field `file`, chỉ admin). Nhận JPG/PNG/WebP tối đa 5 MB; loại file được chọn bằng `switch` theo MIME do trình duyệt gửi lên. Bản đơn giản này không kiểm tra phần đầu file, giải mã nội dung ảnh hoặc kiểm tra số pixel. Service tạo tên UUID và lưu file bằng `transferTo`, sau đó trả URL `/api/uploads/images/<uuid>.png`. `WebConfig` ánh xạ đường dẫn này tới thư mục upload để Spring tự phục vụ ảnh và trả 404 khi file không tồn tại. Cả API và ảnh đều đi qua proxy `/api` ở frontend. Ảnh không có hoặc bị lỗi được thay bằng ảnh mặc định.

Luồng xử lý trong code:

1. CMS chọn ảnh và xem trước trên trình duyệt; chưa gửi file lên BE.
2. Khi bấm lưu, `onCreateMovie` dùng chuỗi `.then() / .catch() / .finally()` để upload ảnh mới bằng `movieApi.uploadImage` rồi lưu phim. Nếu không chọn ảnh mới thì giữ URL hiện tại.
3. `ImageUploadController` nhận file; `ImageStorageService.store` kiểm tra file rỗng/dung lượng, xác định đuôi ảnh, lưu file và trả URL.
4. CMS gửi thông tin phim cùng `posterUrl` vào API thêm/sửa phim. Chỉ đường dẫn được lưu trong bảng `movies`.
5. Trang khách dùng `posterUrl` trong thẻ `img`; Spring đọc ảnh từ thư mục upload qua cấu hình trong `WebConfig`.

Danh sách phim hỗ trợ ba bộ lọc tùy chọn qua `GET /api/movies?title=mai&genreId=1&featured=true`: `title` tìm gần đúng không phân biệt hoa/thường; `genreId` chọn thể loại; `featured=true/false` chọn banner đã bật/tắt. Các điều kiện kết hợp bằng AND. Không truyền tham số hoặc để trống thì lấy tất cả phim như trước. Response JSON gồm `movies` là danh sách phim sau lọc và `featuredCount` là tổng số phim bật banner do BE đếm trong toàn hệ thống, không thay đổi theo kết quả tìm kiếm. Ba ô lọc CMS nằm trên một hàng: chọn thể loại/banner sẽ lọc ngay, tên phim tự tìm sau 300 ms ngừng gõ. Xóa tên và chọn lại các mục “Tất cả” để xem toàn bộ phim. BE dùng Java để lọc danh sách phim và sắp xếp banner. Phần này chưa thêm phân trang.

Ảnh mới được lưu trong `movie.store/uploads/movies` khi chạy từ thư mục gốc hoặc từ `movie.store`. Có thể đặt biến môi trường `MOVIESTORE_UPLOAD_DIR` thành đường dẫn tuyệt đối để chọn nơi lưu khác. Thư mục ảnh được phép đưa vào Git theo cấu hình hiện tại; 8 poster mẫu đã được tải sẵn để chạy thử. Danh sách ảnh nằm trong [uploads/README.md](movie.store/uploads/README.md). Khi chuyển máy chủ cần chuyển cả thư mục ảnh, kể cả các file upload chưa commit.

Khởi động backend với database cũ sẽ tự thêm các cột mới qua Hibernate và chuyển poster/nội dung banner từng viết trong frontend sang dữ liệu phim **một lần**. Các thay đổi sau đó trong CMS không bị ghi đè khi khởi động lại. URL poster mẫu cũ được đổi sang ảnh local đã tải sẵn; các ảnh upload hoặc URL tùy chỉnh khác được giữ nguyên. Nếu dùng `MOVIESTORE_UPLOAD_DIR`, hãy chép các file trong `movie.store/uploads/movies` sang thư mục đã cấu hình.

Phim cũ giữ `release_year` và để `release_date` trống; CMS hiển thị nhắc bổ sung ngày chính xác trước khi lưu. API thêm/sửa phim yêu cầu `releaseDate` (`YYYY-MM-DD`), không còn nhận `releaseYear` thay cho ngày. Cột năm cũ được giữ trong giai đoạn chuyển đổi; không tự gán ngày 01/01 hoặc xóa dữ liệu cũ. Các phim mẫu mới cũng chưa được tự đặt ngày phát hành.

Phim và thể loại có quan hệ nhiều–nhiều qua bảng `movie_genre_links`. Body thêm/sửa dùng `genreIds` (chọn ít nhất một ID hợp lệ); response phim có `genres: [{"id": 1, "name": "Hành động"}]`. Bộ lọc `genreId` vẫn chọn một thể loại và tìm mọi phim có thể loại đó. CMS cho tick nhiều thể loại; khi sửa, danh sách gửi lên thay thế các thể loại đang gán. Xóa phim chỉ xóa các liên kết, không xóa thể loại; thể loại đang có phim vẫn bị chặn xóa.

Khi khởi động BE với database cũ, Hibernate tạo bảng nối rồi `MovieGenreMigration` chuyển các liên kết `movies.genre_id` sang bảng nối trước khi bỏ khóa ngoại và cột cũ. Những lần khởi động sau không gán lại thể loại cũ. Không chạy đồng thời BE bản cũ trong lúc chuyển đổi.

Ví dụ body thêm/sửa phim:

```json
{
  "title": "Phim mới",
  "duration": 120,
  "releaseDate": "2026-10-28",
  "genreIds": [1, 2],
  "posterUrl": "/api/uploads/images/00000000-0000-4000-8000-000000000000.png",
  "subtitle": "CÂU CHUYỆN TRÊN MÀN ẢNH RỘNG",
  "description": "Nội dung giới thiệu phim.",
  "featured": true,
  "bannerPosition": null
}
```

## Lịch chiếu theo rạp và phòng

CMS gọi `GET /api/showtimes/schedule?date=2026-10-30&cinemaIds=1,2&keyword=soul`. `date` mặc định là hôm nay theo giờ Việt Nam; bỏ `cinemaIds` để lấy tất cả rạp; `keyword` tìm tên phim, phòng hoặc rạp, không phân biệt hoa/thường và dấu tiếng Việt. ID `0` dành cho phòng cũ chưa được gán cụm rạp.

Response gồm `date`, `totalShowtimes` và `cinemas`. Mỗi rạp có `id`, `name`, `address`, `totalShowtimes`, `rooms`; mỗi phòng có `id`, `name`, `seatCount`, `showtimes`. Phòng trống trả `showtimes: []`. Khi tìm kiếm, chỉ giữ các phòng khớp từ khóa hoặc có suất chiếu khớp; không có kết quả trả `cinemas: []` và tổng bằng 0.

BE dùng vòng `for` và `ArrayList` để lọc, gom nhóm; suất chiếu được lấy theo ngày và sắp theo giờ bắt đầu, rồi ID tăng dần. `ShowtimeResponse` có thêm `endTime` do BE tính từ thời lượng phim, kể cả khi kết thúc qua ngày hôm sau. FE chỉ hiển thị cấu trúc đã nhận, định dạng ngày/giờ và tiền; tìm kiếm tự gọi API sau 300 ms ngừng gõ. API danh sách phẳng, chi tiết, thêm/sửa/xóa vẫn giữ nguyên đường dẫn.

## Format code chung trong VS Code / Cursor

Mở thư mục gốc `MovieStore`, sau đó cài hai extension được đề xuất trong tab Extensions (`@recommended`): **Prettier - Code formatter** và **Language Support for Java by Red Hat**.

Cài công cụ format dùng chung từ terminal tại thư mục gốc:

```powershell
npm.cmd ci
```

Trên macOS/Linux dùng `npm ci`. File `package-lock.json` cố định phiên bản Prettier để cả nhóm có cùng kết quả format.

Sau khi cài, lưu file bằng `Ctrl+S` sẽ tự format; dùng `Shift+Alt+F` trên Windows để format thủ công. HTML/CSS/JavaScript/JSON dùng 2 dấu cách và cấu hình [.prettierrc.json](.prettierrc.json). Java dùng 4 dấu cách và profile [.vscode/java-formatter.xml](.vscode/java-formatter.xml). Các thiết lập VS Code được chia sẻ qua [.vscode/settings.json](.vscode/settings.json).

Format toàn bộ frontend trước khi commit:

```powershell
npm.cmd run format
```

Kiểm tra định dạng frontend mà không sửa file:

```powershell
npm.cmd run format:check
```

Trên macOS/Linux thay `npm.cmd` bằng `npm`. Các lệnh này chỉ xử lý frontend; Java được format khi lưu bằng extension Java. Quy ước đặt tên `on...`/`get...` vẫn cần nhóm tự tuân thủ, formatter không tự đổi tên hàm.

## Kiểm thử backend

Từ thư mục `movie.store`:

```bash
bash ./mvnw test           # macOS / Linux
```

```powershell
.\mvnw.cmd test            # Windows
```

Tài khoản mẫu, cách chạy trong Eclipse và mô tả chức năng nằm trong [RUN-MOVIESTORE.md](RUN-MOVIESTORE.md).
