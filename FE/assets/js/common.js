const API_BASE = "/api";

const api = axios.create({ baseURL: API_BASE });

// Backend identifies the user from this token; never send userId in request bodies.
api.interceptors.request.use((config) => {
  const token = getUser()?.token;
  if (token) config.headers.Authorization = "Bearer " + token;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const sentToken = Boolean(error?.config?.headers?.Authorization);
    if (error?.response?.status === 401 && sentToken) {
      sessionStorage.removeItem(AUTH_KEY);
      const isAdminPage = location.pathname.startsWith("/admin/");
      location.href = isAdminPage
        ? "/admin/login/index.html"
        : "/customer/login/index.html?next=" + encodeURIComponent(location.pathname + location.search);
    }
    return Promise.reject(error);
  }
);

const onError = (error, fallback = "Đã xảy ra lỗi") => {
  const msg =
    error?.response?.data?.message || error?.response?.data?.error || error?.message || fallback;
  alert(msg);
};

const userApi = {
  register: (data) => api.post("/users/register", data),
  login: (data) => api.post("/users/login", data),
  resetPassword: (data) => api.post("/users/reset-password", data),
  changePassword: (data) => api.put("/users/me/password", data),
  get: (id) => api.get(`/users/${id}`)
};

const genreApi = {
  list: () => api.get("/genres"),
  create: (name) => api.post("/genres", { name }),
  update: (id, name) => api.put(`/genres/${id}`, { name }),
  remove: (id) => api.delete(`/genres/${id}`)
};

const movieApi = {
  list: () => api.get("/movies"),
  create: (data) => api.post("/movies", data),
  update: (id, data) => api.put(`/movies/${id}`, data),
  remove: (id) => api.delete(`/movies/${id}`)
};

const roomApi = {
  list: () => api.get("/rooms"),
  create: (data) => api.post("/rooms", data),
  update: (id, data) => api.put(`/rooms/${id}`, data),
  remove: (id) => api.delete(`/rooms/${id}`)
};

const cinemaApi = {
  list: () => api.get("/cinemas"),
  create: (data) => api.post("/cinemas", data),
  update: (id, data) => api.put(`/cinemas/${id}`, data),
  remove: (id) => api.delete(`/cinemas/${id}`)
};

const showtimeApi = {
  list: () => api.get("/showtimes"),
  upcoming: () => api.get("/showtimes/upcoming"),
  get: (id) => api.get(`/showtimes/${id}`),
  create: (data) => api.post("/showtimes", data),
  update: (id, data) => api.put(`/showtimes/${id}`, data),
  remove: (id) => api.delete(`/showtimes/${id}`)
};

const discountApi = {
  list: () => api.get("/discount-codes"),
  create: (data) => api.post("/discount-codes", data),
  update: (id, data) => api.put(`/discount-codes/${id}`, data),
  remove: (id) => api.delete(`/discount-codes/${id}`)
};

const ticketApi = {
  list: () => api.get("/tickets"),
  mine: () => api.get("/tickets/me"),
  bookedSeats: (showtimeId) => api.get(`/tickets/showtime/${showtimeId}/seats`),
  quote: (data) => api.post("/tickets/quote", data),
  book: (data) => api.post("/tickets/book", data),
  cancel: (id) => api.delete(`/tickets/${id}`)
};

// Vietnamese text for account/password errors returned by the backend.
const accountErrorMessages = {
  "Email already registered": "Email này đã được đăng ký.",
  "Phone number must be a valid Vietnamese mobile number": "Số điện thoại không hợp lệ (10 số, VD: 0912345678).",
  "Password confirmation does not match": "Mật khẩu xác nhận không khớp.",
  "Password must be at least 6 characters": "Mật khẩu tối thiểu 6 ký tự.",
  "Email or phone number is incorrect": "Email hoặc số điện thoại không khớp với tài khoản đã đăng ký. Tài khoản tạo trước khi có số điện thoại cần liên hệ quầy vé để được hỗ trợ.",
  "Current password is incorrect": "Mật khẩu hiện tại không đúng.",
  "New password must be different from the current password": "Mật khẩu mới phải khác mật khẩu hiện tại."
};

const getAccountErrorMessage = (error, fallback) => {
  const message = error?.response?.data?.message;
  return accountErrorMessages[message] || message || fallback;
};

const AUTH_KEY = "movieStoreUser";

const getUser = () => {
  try {
    const user = JSON.parse(sessionStorage.getItem(AUTH_KEY));
    // Sessions saved before token login are treated as logged out.
    return user?.token ? user : null;
  } catch (error) {
    console.error(error);
  }
  return null;
};

const onSaveUser = (user) => {
  // A backend older than token login answers without a token; saving that would look like a silent logout.
  if (!user?.token) {
    const message = "Máy chủ chưa trả về phiên đăng nhập. Vui lòng khởi động lại backend bằng bản mới nhất.";
    throw Object.assign(new Error(message), { response: { data: { message } } });
  }
  sessionStorage.setItem(AUTH_KEY, JSON.stringify(user));
};

const onLogout = () => {
  const token = getUser()?.token;
  sessionStorage.removeItem(AUTH_KEY);
  // keepalive lets the revoke request finish even though the page navigates right after.
  if (token) {
    fetch(API_BASE + "/users/logout", {
      method: "POST",
      keepalive: true,
      headers: { Authorization: "Bearer " + token }
    }).catch(() => {});
  }
};

const getUserRole = (role, loginPath) => {
  const user = getUser();
  if (!user || user.role !== role) {
    window.location.href = loginPath;
    return null;
  }
  return user;
};

const getMoney = (value) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(Number(value));

const getDateTime = (iso) => {
  if (!iso) return "";
  const d = new Date(iso);
  return d.toLocaleString("vi-VN", { dateStyle: "short", timeStyle: "short" });
};

const getDateKey = (value) => String(value).slice(0, 10);

const getTime = (value) =>
  new Date(value).toLocaleTimeString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit"
  });

const getDayLabel = (value) =>
  new Date(value + "T12:00:00").toLocaleDateString("vi-VN", {
    weekday: "short",
    day: "2-digit",
    month: "2-digit"
  });

function getSeatLabel(number) {
  return String.fromCharCode(65 + Math.floor((number - 1) / 10)) + (((number - 1) % 10) + 1);
}

function getSafeHtml(value) {
  const characters = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  };
  return String(value ?? "").replace(/[&<>"']/g, (character) => characters[character]);
}
