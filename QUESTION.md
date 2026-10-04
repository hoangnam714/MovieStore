# Câu hỏi tự kiểm tra JavaScript — MovieStore

Khi trả lời mỗi câu, hãy theo công thức:

> **Nó là gì? → Dùng để làm gì? → Trong code MovieStore nó làm gì?**

## 1. Biến và kiểu dữ liệu

1. `const` là gì?
2. Biến khai báo bằng `const` có được gán lại không?
3. Nếu `const` chứa object hoặc array thì dữ liệu bên trong có thay đổi được không?
4. `let` là gì?
5. `const` khác `let` như thế nào?
6. Vì sao `genres` và `editingMovieId` được khai báo bằng `let`?
7. Object `{}` là gì?
8. Cặp `key: value` trong object nghĩa là gì?
9. `genreApi` và `movieApi` là loại dữ liệu gì?
10. Array `[]` là gì?
11. Mảng `genres` dùng để lưu gì?
12. `null` là gì?
13. `editingMovieId === null` biểu thị trạng thái gì?
14. Boolean là gì?
15. `true` và `false` được dùng để biểu diễn điều gì?

## 2. Hàm

16. Hàm là gì?
17. Hàm mũi tên `=>` là gì?
18. Phần đứng trước và sau dấu `=>` có ý nghĩa gì?
19. Khi arrow function có một tham số, có bắt buộc dùng dấu ngoặc tròn không?
20. Khi arrow function có nhiều tham số, phải viết như thế nào?
21. Tham số của hàm là gì?
22. Đối số là gì?
23. Tham số và đối số khác nhau thế nào?
24. `return` là gì?
25. `return` ảnh hưởng thế nào đến việc thực thi hàm?
26. Vì sao các hàm trong `movieApi` phải `return` kết quả của Axios?
27. `return` trong trường hợp không có dữ liệu ở `fetchMovies()` dùng để làm gì?
28. Tham số mặc định là gì?
29. `genreId = ""` có ý nghĩa gì?
30. Khi gọi `fetchMovies()` mà không truyền đối số thì `genreId` nhận giá trị gì?

## 3. Promise và xử lý bất đồng bộ

31. Lập trình bất đồng bộ là gì?
32. Vì sao việc gọi API là tác vụ bất đồng bộ?
33. Promise là gì?
34. Promise có những trạng thái nào?
35. `pending` là trạng thái gì?
36. `fulfilled` là trạng thái gì?
37. `rejected` là trạng thái gì?
38. `async` là gì?
39. Hàm `async` luôn trả về kiểu gì?
40. `await` là gì?
41. `await` được sử dụng ở đâu?
42. `await` có làm treo toàn bộ trang web không?
43. Trong MovieStore, `await` dùng để chờ điều gì?
44. `then()` là gì?
45. `then()` chạy khi Promise thành công hay thất bại?
46. `async/await` và `then()` có điểm chung gì?
47. Trong `fetchMovies()`, `then()` biến đổi kết quả API như thế nào?
48. `try...catch` là gì?
49. Khối `try` dùng để làm gì?
50. Khối `catch` chạy khi nào?
51. Biến `error` trong `catch` chứa gì?
52. Vì sao các lời gọi API nên được đặt trong `try...catch`?
53. `console.error()` là gì?

## 4. Destructuring và object trả về

54. `const { data }` là gì?
55. Destructuring object dùng để làm gì?
56. Thuộc tính `data` trong kết quả Axios thường chứa gì?
57. Cú pháp `({ data: response.data.movies })` tạo ra giá trị gì?
58. Vì sao object được trả trực tiếp từ arrow function phải đặt trong dấu ngoặc tròn?

## 5. Phương thức của mảng

59. `map()` là gì?
60. Từ “ánh xạ từng phần tử” có nghĩa là gì?
61. `map()` trả về giá trị gì?
62. `map()` có làm thay đổi trực tiếp mảng ban đầu không?
63. Trong `fetchGenres()`, `map()` biến mỗi thể loại thành gì?
64. Trong `fetchMovies()`, `map()` biến mỗi phim thành gì?
65. `map()` khác `forEach()` như thế nào?
66. Khi nào nên dùng `map()` thay vì `forEach()`?
67. `find()` là gì?
68. `find()` trả về gì khi tìm thấy phần tử?
69. `find()` trả về gì khi không tìm thấy phần tử?
70. Trong MovieStore, `find()` được dùng để tìm gì?
71. `join("")` là gì?
72. Vì sao phải dùng `join("")` sau `map()`?
73. Đối số chuỗi rỗng trong `join("")` có ý nghĩa gì?
74. `length` là gì?
75. `data.length === 0` kiểm tra điều gì?

## 6. Toán tử và điều kiện

76. `===` là gì?
77. “So sánh bằng nghiêm ngặt” nghĩa là gì?
78. `===` kiểm tra những yếu tố nào?
79. `===` khác `==` như thế nào?
80. `!==` là gì?
81. `!==` kiểm tra hai giá trị khác nhau về những yếu tố nào?
82. Trong MovieStore, `editingMovieId !== null` kiểm tra điều gì?
83. Toán tử `!` là gì?
84. `!` thay đổi giá trị logic như thế nào?
85. `!data` có ý nghĩa gì?
86. Toán tử `||` là gì?
87. Biểu thức dùng `||` đúng khi nào?
88. `!data || data.length === 0` có ý nghĩa gì?
89. Toán tử `&&` là gì?
90. Biểu thức dùng `&&` đúng khi nào?
91. JavaScript dừng đánh giá biểu thức `&&` khi gặp loại giá trị nào?
92. `&&` khác `||` như thế nào?
93. Toán tử ba ngôi `? :` là gì?
94. Ba phần của toán tử ba ngôi là gì?
95. Toán tử ba ngôi trong `fetchMovies()` lựa chọn giữa hai thao tác nào?
96. Truthy là gì?
97. Falsy là gì?
98. Những giá trị falsy thường gặp trong JavaScript là gì?
99. Vì sao chuỗi rỗng `""` khiến code lấy tất cả phim?
100. Optional chaining `?.` là gì?
101. `?.` giúp tránh lỗi trong trường hợp nào?
102. Nullish coalescing `??` là gì?
103. `??` sử dụng giá trị bên phải khi nào?
104. `??` khác `||` như thế nào?
105. Biểu thức `genres.find(...)?.name ?? ""` hoạt động ra sao?

## 7. Chuỗi và chuyển đổi kiểu

106. Template literal là gì?
107. Template literal được bao bởi loại dấu nào?
108. Template literal có hai ưu điểm chính nào?
109. `${...}` là gì?
110. Nội suy chuỗi là gì?
111. `${...}` có thể chèn những loại giá trị nào vào chuỗi?
112. `${...}` hoạt động bên trong loại chuỗi nào?
113. MovieStore dùng template literal để tạo những gì?
114. `trim()` là gì?
115. `trim()` xóa khoảng trắng ở những vị trí nào?
116. Vì sao tên phim được gọi `trim()` trước khi gửi?
117. `Number()` là gì?
118. Vì sao phải dùng `Number()` cho các input có `type="number"`?
119. Thuộc tính `value` của input trả về kiểu dữ liệu gì?

## 8. DOM và thao tác với HTML

120. DOM là gì?
121. DOM giúp JavaScript làm gì với trang HTML?
122. `document` là gì?
123. `getElementById()` là gì?
124. `getElementById()` tìm phần tử dựa vào thuộc tính nào?
125. `movieRows`, `movieForm`, `genreSelect` và `genreFilter` đại diện cho gì?
126. Thuộc tính `elements` của form là gì?
127. `movieForm.elements.title` truy cập phần tử dựa vào thuộc tính HTML nào?
128. `value` là gì?
129. `reset()` là gì?
130. `onResetForm()` ngoài reset dữ liệu còn thay đổi những gì?
131. `textContent` là gì?
132. `innerHTML` là gì?
133. `innerHTML` khác `textContent` như thế nào?
134. Vì sao danh sách phim được gán bằng `innerHTML` thay vì `textContent`?

## 9. Sự kiện

135. Sự kiện trong JavaScript là gì?
136. Hãy nêu các sự kiện xuất hiện trong MovieStore.
137. `addEventListener()` là gì?
138. `addEventListener()` nhận những thông tin chính nào?
139. Sự kiện `submit` là gì?
140. Sự kiện `submit` có thể xảy ra bằng những cách nào?
141. Vì sao nên xử lý `submit` trên form thay vì chỉ xử lý `click` trên nút Lưu?
142. Object `event` là gì?
143. `event.preventDefault()` là gì?
144. Trong form, `preventDefault()` hủy hành động mặc định nào?
145. `preventDefault()` có ngăn callback chạy không?
146. `preventDefault()` có ngăn sự kiện lan truyền không?
147. `stopPropagation()` là gì?
148. `stopPropagation()` khác `preventDefault()` thế nào?
149. `onclick` là gì?
150. `onchange` là gì?
151. `this` trong `onchange="onChangeGenre(this.value)"` là gì?
152. `this.value` trả về gì?
153. `window.confirm()` là gì?
154. `confirm()` trả về giá trị gì khi chọn OK và Cancel?
155. Dòng `if (!window.confirm(...)) return;` hoạt động như thế nào?

## 10. Axios và HTTP API

156. Axios là gì?
157. Axios dùng để giao tiếp giữa những thành phần nào?
158. Các hàm Axios trả về gì?
159. `axios.create()` là gì?
160. Vì sao tạo một Axios instance riêng?
161. `baseURL` là gì?
162. `baseURL` được kết hợp với đường dẫn request như thế nào?
163. `baseURL: "/8080/api"` có phải là cổng 8080 không?
164. Nếu muốn chỉ định server ở cổng 8080 thì URL phải có thành phần nào?
165. GET là gì?
166. POST là gì?
167. PUT là gì?
168. DELETE là gì?
169. `API.get("/movies")` dùng để làm gì?
170. `API.post("/movies", data)` dùng để làm gì?
171. ``API.put(`/movies/${id}`, data)`` dùng để làm gì?
172. ``API.delete(`/movies/${id}`)`` dùng để làm gì?
173. `genreApi` là gì?
174. `movieApi` là gì?
175. Các method `list`, `detail`, `create`, `update`, `remove` lần lượt làm gì?
176. Vì sao nên gom các hàm API vào object riêng?

## 11. Các hàm trong MovieStore

177. `onResetForm()` làm những việc gì?
178. Vì sao `onResetForm()` phải đặt `editingMovieId = null`?
179. `fetchGenres()` làm những việc gì?
180. Vì sao `fetchGenres()` gán dữ liệu API vào biến `genres`?
181. Vì sao cùng một chuỗi `options` được đưa vào cả `genreSelect` và `genreFilter`?
182. `onChangeGenre()` làm gì?
183. `async` trong cách viết hiện tại của `onChangeGenre()` có bắt buộc không? Vì sao?
184. `fetchMovies()` làm những việc gì?
185. `fetchMovies()` quyết định lấy tất cả phim hay phim theo thể loại như thế nào?
186. Vì sao `fetchMovies()` phải kiểm tra dữ liệu rỗng?
187. Vì sao phải dùng `colspan="6"` trong dòng thông báo?
188. `onEditMovie(id)` làm những việc gì?
189. Vì sao khi sửa phải lưu ID vào `editingMovieId`?
190. `onDeleteMovie(id)` làm những việc gì?
191. Vì sao xóa phim đang được sửa thì phải gọi `onResetForm()`?
192. Handler `submit` thu thập những dữ liệu nào?
193. Handler `submit` phân biệt thêm và sửa bằng cách nào?
194. Sau khi thêm hoặc sửa thành công, chương trình làm gì?
195. `onInitPage()` làm gì?
196. Vì sao `fetchGenres()` phải hoàn thành trước `fetchMovies()`?
197. Dòng `onInitPage();` ở cuối có tác dụng gì?

## 12. Các lỗi và điểm dễ bị hỏi

198. Handler `submit` hiện tại đang thiếu tham số nào?
199. Handler `submit` hiện tại đang thiếu lời gọi phương thức nào?
200. Điều gì có thể xảy ra nếu không gọi `event.preventDefault()`?
201. Vì sao `baseURL: "/8080/api"` có thể không đúng với ý định dùng cổng 8080?
202. Nếu giao diện và API chạy cùng server, `baseURL` thường có thể viết thế nào?
203. Nếu `find()` không tìm thấy thể loại và code không dùng `?.`, lỗi gì có thể xảy ra?
204. Nếu không dùng `Number()`, dữ liệu số gửi lên API có thể mang kiểu gì?
205. Nếu gọi `fetchMovies()` trước khi tải `genres`, tên thể loại có thể hiển thị thế nào?

## 13. Câu tổng hợp để luyện vấn đáp

206. Hãy trình bày toàn bộ luồng chạy khi trang vừa được mở.
207. Hãy trình bày luồng xử lý khi người dùng chọn một thể loại.
208. Hãy trình bày luồng xử lý khi người dùng bấm Sửa.
209. Hãy trình bày luồng xử lý khi người dùng bấm Lưu để thêm phim.
210. Hãy trình bày luồng xử lý khi người dùng bấm Lưu để cập nhật phim.
211. Hãy trình bày luồng xử lý khi người dùng bấm Xóa.
212. Dữ liệu đi từ form tới server như thế nào?
213. Dữ liệu đi từ server tới bảng HTML như thế nào?
214. `map()`, `join("")` và `innerHTML` phối hợp với nhau như thế nào?
215. `async`, `await`, Promise và `try...catch` phối hợp với nhau như thế nào?
216. `editingMovieId` quản lý trạng thái thêm/sửa như thế nào?
217. `find()`, `?.` và `??` phối hợp để hiển thị tên thể loại như thế nào?
218. Hãy giải thích vai trò của từng tầng: HTML form, JavaScript, Axios và REST API.
