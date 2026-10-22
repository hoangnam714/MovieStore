const API_BASE = "http://localhost:8080/api";

const api = axios.create({ baseURL: API_BASE });

const formatMoney = (value) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(Number(value));

const formatDateTime = (iso) => {
  if (!iso) return "";
  const d = new Date(iso);
  return d.toLocaleString("vi-VN", { dateStyle: "short", timeStyle: "short" });
};

const handleError = (error, fallback = "Đã xảy ra lỗi") => {
  const msg =
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    fallback;
  alert(msg);
};

const userApi = {
  register: (data) => api.post("/users/register", data),
  login: (data) => api.post("/users/login", data),
  get: (id) => api.get(`/users/${id}`),
};

const genreApi = {
  list: () => api.get("/genres"),
  create: (name) => api.post("/genres", { name }),
  update: (id, name) => api.put(`/genres/${id}`, { name }),
  remove: (id) => api.delete(`/genres/${id}`),
};

const movieApi = {
  list: () => api.get("/movies"),
  create: (data) => api.post("/movies", data),
  update: (id, data) => api.put(`/movies/${id}`, data),
  remove: (id) => api.delete(`/movies/${id}`),
};

const roomApi = {
  list: () => api.get("/rooms"),
  create: (data) => api.post("/rooms", data),
  update: (id, data) => api.put(`/rooms/${id}`, data),
  remove: (id) => api.delete(`/rooms/${id}`),
};

const showtimeApi = {
  list: () => api.get("/showtimes"),
  upcoming: () => api.get("/showtimes/upcoming"),
  get: (id) => api.get(`/showtimes/${id}`),
  create: (data) => api.post("/showtimes", data),
  update: (id, data) => api.put(`/showtimes/${id}`, data),
  remove: (id) => api.delete(`/showtimes/${id}`),
};

const ticketApi = {
  list: () => api.get("/tickets"),
  byUser: (userId) => api.get(`/tickets/user/${userId}`),
  bookedSeats: (showtimeId) => api.get(`/tickets/showtime/${showtimeId}/seats`),
  book: (data) => api.post("/tickets/book", data),
};
