# Ôn nhanh JavaScript — MovieStore

Khi trả lời: **Nó là gì → dùng để làm gì → trong code này làm gì.**

## 1. Luồng chương trình

- Mở trang: `onInitPage()` tải thể loại rồi tải phim. Thể loại phải có trước để tìm tên thể loại của từng phim.
- Lọc: `onChangeGenre(genreId)` tải phim thuộc thể loại được chọn; ID rỗng thì tải tất cả.
- Sửa: `onEditMovie(id)` lấy chi tiết, lưu ID đang sửa và điền dữ liệu vào form.
- Lưu: có `editingMovieId` thì cập nhật; không có thì thêm mới. Sau đó reset và tải lại danh sách.
- Xóa: xác nhận, gọi API xóa rồi tải lại danh sách.

## 2. Biến, dữ liệu và hàm

### `const` là gì?

Khai báo biến không được gán lại. Code dùng cho `API`, `movieApi`, `movieForm` và các tham chiếu cố định.

### `let` là gì?

Khai báo biến có thể gán lại. `genres` và `editingMovieId` thay đổi khi chương trình chạy.

### Object `{}` là gì?

Đối tượng chứa các cặp key–value. `genreApi` và `movieApi` là object gom các hàm API.

### Array `[]` là gì?

Mảng lưu danh sách nhiều phần tử. `genres` là mảng chứa các thể loại.

### `null` là gì?

Giá trị biểu thị không có dữ liệu. `editingMovieId === null` nghĩa là đang thêm phim.

### Hàm mũi tên `=>` là gì?

Cú pháp ngắn để khai báo hàm. Phần trước `=>` là tham số; phần sau là nội dung hoặc giá trị trả về.

### Tham số là gì?

Biến khai báo trong hàm để nhận giá trị được truyền vào., như `id`, `data`, `genreId` và `movie`.

### `return` là gì?

Kết thúc hàm và có thể trả kết quả. Các hàm API trả Promise; `fetchMovies()` dùng `return` để dừng khi không có dữ liệu.

### Tham số mặc định là gì?

Giá trị dùng khi không truyền đối số. `genreId = ""` nghĩa là mặc định không lọc thể loại.

## 3. Bất đồng bộ

### Promise là gì?

Đối tượng đại diện cho kết quả của một tác vụ bất đồng bộ (chờ, thành công hoặc thất bại).

### `async` là gì?
Khai báo hàm bất đồng bộ, cho phép dùng `await` và luôn trả về Promise.

### `await` là gì?
Chờ một Promise hoàn thành rồi mới chạy tiếp, chỉ dùng trong hàm `async`.

### `then()` là gì?
Xử lý kết quả khi Promise thành công. Code dùng nó để lấy `response.data.movies`.

### `try...catch` là gì?
`try` chứa code có thể lỗi; `catch` nhận và xử lý lỗi khi gọi API thất bại.

### `console.error()` là gì?
In thông tin lỗi ra Console để kiểm tra.

### `const { data }` là gì?
Đây là destructuring object, dùng để lấy thuộc tính data từ object kết quả.

## 4. Mảng

### `map()` là gì?
Ánh xạ từng phần tử thành giá trị mới và trả về mảng mới. 

### `find()` là gì?
Trả phần tử đầu tiên thỏa điều kiện; không tìm thấy trả `undefined`

### `join("")` là gì?
Nối các phần tử mảng thành một chuỗi không có ký tự phân cách. 

### `length` là gì?
Thuộc tính chỉ độ dài. Với mảng, nó là số phần tử; `data.length === 0` kiểm tra mảng rỗng.

### `map()` khác `forEach()` thế nào?
`map()` trả mảng mới sau khi ánh xạ; `forEach()` chỉ duyệt và không trả mảng kết quả hữu ích.

## 5. Toán tử

### `===` là gì?
So sánh bằng nghiêm ngặt: hai bên phải cùng giá trị và cùng kiểu dữ liệu.

### `!==` là gì?
So sánh khác nghiêm ngặt: trả về `true` nếu hai bên khác giá trị hoặc khác kiểu dữ liệu.

### `!` là gì?
Phủ định logic, đảo đúng thành sai và sai thành đúng.

### `||` là gì?
Toán tử HOẶC logic; điều kiện đúng khi ít nhất một vế đúng.

### `&&` là gì?
Toán tử VÀ logic; điều kiện chỉ đúng khi tất cả các vế đều đúng.

### Toán tử ba ngôi `? :` là gì?
Nếu điều kiện đúng thì lấy giá trị sau `?`, sai thì lấy giá trị sau `:`.

### Truthy và falsy là gì?
Giá trị được xem như đúng hoặc sai trong điều kiện. Falsy gồm `false`, `0`, `""`, `null`, `undefined`, `NaN`.

### `?.` là gì?
Optional chaining: truy cập thuộc tính an toàn; trả `undefined` thay vì lỗi nếu bên trái là `null/undefined`.

### `??` là gì?
Lấy giá trị dự phòng: khi bên trái là `null/undefined`. Khác `||` vì `||` còn thay thế `0`, `false` và `""`.

## 6. Chuỗi và chuyển kiểu

### Template literal là gì?
Chuỗi viết bằng backtick, cho phép xuống dòng và chèn biểu thức bằng `${...}`. Code dùng để tạo URL và HTML.

### `trim()` là gì?
Tạo chuỗi mới sau khi xóa khoảng trắng ở đầu và cuối.

### `Number()` là gì?
Chuyển giá trị thành số.


## 7. DOM và form

### DOM là gì?
Cây object của trang HTML mà JavaScript có thể thao tác.

### `document` là gì?
Object đại diện cho tài liệu HTML hiện tại.

### `getElementById()` là gì?
Tìm và trả về phần tử HTML có ID tương ứng, không thấy thì trả `null`.

### `elements` là gì?
Tập hợp các control trong form, truy cập được theo thuộc tính `name`.

### `value` là gì?
Giá trị hiện tại của `input`, `select` hoặc `textarea` (luôn là chuỗi).

### `reset()` là gì?
Đưa các control của form về giá trị ban đầu.

### `textContent` là gì?
Đọc hoặc thay văn bản thuần; không phân tích chuỗi thành HTML.

### `innerHTML` là gì?
Đọc hoặc thay nội dung HTML; trình duyệt phân tích chuỗi thành phần tử HTML.

## 8. Sự kiện

### Sự kiện là gì?
Thông báo một hành động đã xảy ra trên trang, như `click`, `change` hoặc `submit`.

### `addEventListener()` là gì?
Đăng ký hàm xử lý khi sự kiện xác định xảy ra.

### `submit` là gì?
Sự kiện gửi form, thường xảy ra khi bấm nút submit hoặc nhấn Enter trong form.


### `onclick` và `onchange` là gì?
Thuộc tính HTML gọi hàm khi phần tử được nhấp hoặc khi giá trị thay đổi.

### `this.value` là gì?
`this` là phần tử đang phát sự kiện; `this.value` là giá trị hiện tại của phần tử đó.

### `window.confirm()` là gì?
Hiện hộp thoại xác nhận; OK trả `true`, Cancel trả `false`.

### `e` là gì?
Đối tượng chứa thông tin về sự kiện vừa xảy ra (ở đây là `submit`).

### `e.preventDefault()` là gì?
Ngăn hành động mặc định của sự kiện (ở đây là form gửi đi và tải lại trang).

<!-- ### `stopPropagation()` là gì?
Ngăn sự kiện lan lên phần tử cha; khác `preventDefault()` là hủy hành động mặc định. -->

## 9. Axios và HTTP

### Axios là gì?
Thư viện gửi HTTP request tới server và nhận response; các hàm Axios trả Promise.

### `axios.create()` là gì?
Tạo Axios instance có cấu hình dùng chung cho nhiều request.

### `baseURL` là gì?
Địa chỉ gốc được ghép với đường dẫn của mỗi request.

### Các HTTP method là gì?
- `GET`: lấy dữ liệu.
- `POST`: tạo dữ liệu.
- `PUT`: cập nhật dữ liệu.
- `DELETE`: xóa dữ liệu.

### Các method API làm gì?
- `list`: lấy danh sách.
- `detail`: lấy chi tiết theo ID.
- `create`: tạo mới.
- `update`: cập nhật theo ID.
- `remove`: xóa theo ID.


## 11. Hai điểm dễ bị hỏi

### `baseURL: "http://localhost:8080/api"` có phải cổng 8080 không?

Không. Nó là đường dẫn `/8080/api` trên cùng host. Muốn chỉ định cổng thường phải viết `http://localhost:8080/api`; nếu cùng server thường dùng `/api`.

### Handler `submit` đang thiếu gì?

Nó không nhận `event` và không gọi `event.preventDefault()`, nên trình duyệt có thể submit form mặc định và tải lại trang.

## 12. Bảng học thuộc

| Cú pháp | Ý chính |
|---|---|
| `const` | Biến không gán lại |
| `let` | Biến có thể gán lại |
| `[]` / `{}` | Mảng / object |
| `=>` | Hàm mũi tên |
| `return` | Trả kết quả, kết thúc hàm |
| `async` / `await` | Hàm bất đồng bộ / chờ Promise |
| `then()` | Xử lý Promise thành công |
| `map()` | Ánh xạ, trả mảng mới |
| `find()` | Tìm phần tử đầu tiên |
| `join("")` | Nối mảng thành chuỗi |
| `length` | Độ dài, số phần tử |
| `===` / `!==` | Bằng / khác nghiêm ngặt |
| `!` / `||` | Phủ định / hoặc logic |
| `? :` | Toán tử ba ngôi |
| `?.` / `??` | Truy cập an toàn / giá trị dự phòng |
| `trim()` / `Number()` | Xóa khoảng trắng / chuyển số |
| `try...catch` | Chạy code và bắt lỗi |
| `innerHTML` | Nội dung HTML |
| `textContent` | Nội dung văn bản |
| `addEventListener()` | Lắng nghe sự kiện |
| `preventDefault()` | Hủy hành động mặc định |
| GET / POST | Lấy / tạo dữ liệu |
| PUT / DELETE | Cập nhật / xóa dữ liệu |

## 13. Câu tự kiểm tra

1. `map()` khác `forEach()` thế nào?
2. Vì sao cần `join("")` sau `map()`?
3. `===` khác `==` thế nào?
4. `?.` và `??` làm gì?
5. Vì sao phải dùng `Number()`?
6. `async`, `await` và Promise liên quan thế nào?
7. `innerHTML` khác `textContent` thế nào?
8. `editingMovieId` quản lý trạng thái gì?
9. Vì sao tải thể loại trước phim?
10. `preventDefault()` hủy hành động nào?
