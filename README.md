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
