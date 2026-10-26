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

FE là **web tĩnh** (HTML/JS), **không** có `main()` như Spring Boot → không có *Run As → Java Application*. Vẫn chạy **trong Eclipse** bằng Tomcat:

### Cách 1 — Run on Server (chuẩn Eclipse)

Cần **Eclipse IDE for Enterprise Java and Web Developers** (có *Web Tools Platform*).

1. **File → Import → General → Existing Projects into Workspace** → chọn thư mục `FE`.
2. Nếu Eclipse hỏi convert facet: bật **Dynamic Web Module** (project đã cấu hình `jst.web` 4.0).
3. **Window → Show View → Servers** → chuột phải → **New → Server** → **Apache Tomcat** (cài Tomcat 9/10, trỏ tới thư mục Tomcat trên máy).
4. Kéo project **MovieStore-FE** vào server, hoặc chuột phải project → **Run As → Run on Server**.
5. Mở trình duyệt (context root thường là tên project):
   - `http://localhost:8080/MovieStore-FE/customer/index.html`
   - `http://localhost:8080/MovieStore-FE/admin/login.html`

**Lưu ý:** Tomcat mặc định cũng dùng port **8080** — trùng với Spring Boot. Chỉnh một trong hai:
   - Tomcat: đổi HTTP port (ví dụ **8081**) trong *double-click server → Ports*;
   - hoặc chạy BE trên port khác trong `application.properties` (`server.port=8080`, Tomcat `8081`).

Backend API vẫn phải chạy riêng (*Run As → Spring Boot App* trên `movie.store`).

### Cách 2 — Terminal (không cần Tomcat)

Từ thư mục `FE`: `python3 -m http.server 5500` → `http://localhost:5500/customer/`

### Cấu hình API

Sửa `FE/assets/js/api.js` nếu backend không ở `http://localhost:8080/api`.

## Đường dẫn giao diện

| URL (ví dụ port 5500) | Mô tả |
|------------------------|--------|
| `/customer/index.html` | Khách: suất chiếu, đặt vé |
| `/customer/login.html` | Đăng nhập khách |
| `/customer/register.html` | Đăng ký |
| `/admin/login.html` | CMS Admin (AdminLTE 3) |
| `/admin/index.html` | Quản lý rạp |

CMS dựa trên [AdminLTE 3](https://adminlte.io/) (MIT), tùy biến cho rạp phim.
