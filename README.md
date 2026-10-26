# MovieStore

Ứng dụng đặt vé xem phim gồm backend Spring Boot (`movie.store`) và frontend HTML/CSS/JavaScript thuần (`FE`). Có thể chạy bằng terminal tích hợp trong Cursor, VS Code hoặc IDE khác.

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

## Kiểm thử backend

Từ thư mục `movie.store`:

```bash
bash ./mvnw test           # macOS / Linux
```

```powershell
.\mvnw.cmd test            # Windows
```

Tài khoản mẫu, cách chạy trong Eclipse và mô tả chức năng nằm trong [RUN-MOVIESTORE.md](RUN-MOVIESTORE.md).
