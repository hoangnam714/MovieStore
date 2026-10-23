const API_BASE = "/api";

const api = axios.create({ baseURL: API_BASE });

const onError = (error, fallback = "Đã xảy ra lỗi") => {
  const msg =
    error?.response?.data?.message || error?.response?.data?.error || error?.message || fallback;
  alert(msg);
};

const userApi = {
  register: (data) => api.post("/users/register", data),
  login: (data) => api.post("/users/login", data),
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

const showtimeApi = {
  list: () => api.get("/showtimes"),
  upcoming: () => api.get("/showtimes/upcoming"),
  get: (id) => api.get(`/showtimes/${id}`),
  create: (data) => api.post("/showtimes", data),
  update: (id, data) => api.put(`/showtimes/${id}`, data),
  remove: (id) => api.delete(`/showtimes/${id}`)
};

const ticketApi = {
  list: () => api.get("/tickets"),
  byUser: (userId) => api.get(`/tickets/user/${userId}`),
  bookedSeats: (showtimeId) => api.get(`/tickets/showtime/${showtimeId}/seats`),
  book: (data) => api.post("/tickets/book", data)
};

const AUTH_KEY = "movieStoreUser";

const getUser = () => {
  try {
    return JSON.parse(sessionStorage.getItem(AUTH_KEY));
  } catch (error) {
    console.error(error);
  }
  return null;
};

const onSaveUser = (user) => sessionStorage.setItem(AUTH_KEY, JSON.stringify(user));

const onLogout = () => sessionStorage.removeItem(AUTH_KEY);

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
