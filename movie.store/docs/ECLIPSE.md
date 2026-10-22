# Chạy Movie Store trên Eclipse

## Backend (`movie.store`)

1. **Eclipse IDE for Enterprise Java and Web Developers** (có m2e Maven).
2. **File → Import → Maven → Existing Maven Projects** → chọn thư mục `movie.store`.
3. JDK **17** (Project → Properties → Java Build Path / Java Compiler).
4. MySQL: tạo database `movie_store_db`, sửa `src/main/resources/application.properties` nếu cần.
5. **Run As → Spring Boot App** (cài *Spring Tools* nếu chưa có) hoặc **Run As → Maven build…** goal `spring-boot:run`.
6. API: `http://localhost:8080/api`

Tài khoản demo (tự seed khi DB trống):

| Email | Mật khẩu | Vai trò |
|-------|-----------|---------|
| admin@moviestore.vn | admin123 | ADMIN |
| khach@moviestore.vn | khach123 | CUSTOMER |

## Frontend (`FE` — project `MovieStore-FE`)

1. **File → Import → General → Existing Projects into Workspace** → chọn thư mục `FE`.
2. Cài extension **Eclipse Web Developer** hoặc dùng trình duyệt + server tĩnh:
   - Cách đơn giản: plugin **Live Server** (nếu dùng VS Code) hoặc **Web Server for PHP** trong Eclipse (*Run on Server* với Tomcat chỉ phục vụ static).
   - Hoặc từ thư mục `FE`: `python3 -m http.server 5500` → mở `http://localhost:5500/customer/`
3. Sửa `FE/assets/js/api.js` nếu API không chạy ở `localhost:8080`.

## Đường dẫn giao diện

| URL (ví dụ port 5500) | Mô tả |
|------------------------|--------|
| `/customer/index.html` | Khách: suất chiếu, đặt vé |
| `/customer/login.html` | Đăng nhập khách |
| `/customer/register.html` | Đăng ký |
| `/admin/login.html` | CMS Admin (AdminLTE 3) |
| `/admin/index.html` | Quản lý rạp |

CMS dựa trên [AdminLTE 3](https://adminlte.io/) (MIT), tùy biến cho rạp phim.
