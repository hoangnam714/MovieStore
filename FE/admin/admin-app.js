const user = requireRole("ADMIN", "login.html");
if (!user) {
  // redirecting
} else {
  initAdmin(user);
}

const safeText = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
function cmsToast(message) { const el = document.createElement('div'); el.className = 'cms-toast'; el.textContent = message; document.body.append(el); setTimeout(() => el.remove(),5000); }
window.addEventListener('unhandledrejection', event => { event.preventDefault(); cmsToast(event.reason?.response?.data?.message || 'Thao tác chưa thành công. Kiểm tra dữ liệu và thử lại.'); });
function initAdmin(user) {
  let genres = [];

  document.getElementById("admin-name").textContent = user.fullName;
  document.getElementById("logout-btn").addEventListener("click", () => {
    clearUser();
    location.href = "login.html";
  });

  document.querySelectorAll("[data-view]").forEach((link) => {
    link.addEventListener("click", (e) => {
      e.preventDefault();
      showView(link.dataset.view);
    });
  });

  async function showView(view) {
    document.querySelectorAll(".nav-link[data-view]").forEach((el) => {
      el.classList.toggle("active", el.dataset.view === view);
    });
    const title = {
      dashboard: "Tổng quan",
      genres: "Thể loại",
      movies: "Phim",
      rooms: "Phòng chiếu",
      showtimes: "Suất chiếu",
      tickets: "Vé đã bán",
    }[view];
    document.getElementById("page-title").textContent = title;
    const content = document.getElementById("main-content");
    content.innerHTML = "<p class='text-muted'>Đang tải…</p>";
    try {
      if (view === "dashboard") await renderDashboard(content);
      else if (view === "genres") await renderGenres(content);
      else if (view === "movies") await renderMovies(content);
      else if (view === "rooms") await renderRooms(content);
      else if (view === "showtimes") await renderShowtimes(content);
      else if (view === "tickets") await renderTickets(content);
      content.querySelectorAll('table').forEach(table => { const wrapper = document.createElement('div'); wrapper.className = 'table-responsive'; table.before(wrapper); wrapper.append(table); });
    } catch (err) {
      handleError(err);
      content.innerHTML = "<p class='text-danger'>Không tải được dữ liệu. Kiểm tra API backend.</p>";
    }
  }

  async function renderDashboard(el) {
    const [movies, rooms, showtimes, tickets] = await Promise.all([
      movieApi.list(), roomApi.list(), showtimeApi.list(), ticketApi.list(),
    ]);
    const upcoming = showtimes.data.filter(s => new Date(s.startTime) > new Date()).sort((a,b) => a.startTime.localeCompare(b.startTime));
    const revenue = tickets.data.reduce((sum,t) => sum + Number(t.price),0);
    el.innerHTML = `<div class="cms-welcome"><div><h2>Hoạt động rạp chiếu</h2><p>${new Date().toLocaleDateString('vi-VN',{weekday:'long',day:'numeric',month:'long',year:'numeric'})} · ${upcoming.length} suất chiếu đang mở bán</p></div><button class="btn" id="quick-showtime">+ Tạo suất chiếu</button></div>
    <div class="row">${[[movies.data.length,'Phim trong danh mục','film'],[rooms.data.length,'Phòng chiếu','door-open'],[upcoming.length,'Suất chiếu sắp tới','calendar-alt'],[tickets.data.length,'Vé đã bán','ticket-alt']].map(([count,label,icon])=>`<div class="col-6 col-xl-3"><div class="stat-card"><i class="fas fa-${icon}"></i><div><strong>${count}</strong><span>${label}</span></div></div></div>`).join('')}</div>
    <div class="row"><div class="col-lg-8"><div class="card"><div class="card-heading"><h3>Lịch chiếu tiếp theo</h3><span class="status-pill">Đang mở bán</span></div><table class="table mb-0"><thead><tr><th>Phim</th><th>Phòng</th><th>Giờ chiếu</th><th>Giá vé</th></tr></thead><tbody>${upcoming.slice(0,8).map(s=>`<tr><td><strong>${safeText(s.movieTitle)}</strong></td><td>${safeText(s.roomName)}</td><td>${formatDateTime(s.startTime)}</td><td>${formatMoney(s.ticketPrice)}</td></tr>`).join('') || '<tr><td colspan="4">Chưa có lịch chiếu.</td></tr>'}</tbody></table></div></div><div class="col-lg-4"><div class="card"><div class="card-body"><p class="text-muted">TỔNG GIÁ TRỊ VÉ ĐÃ ĐẶT</p><h2 style="font-weight:700;color:#f2674a">${formatMoney(revenue)}</h2><p class="text-muted mt-3">Tính từ giá vé lưu trong hệ thống; không đồng nghĩa với tiền đã thu.</p><hr><h6>Thao tác nhanh</h6><button class="btn btn-primary btn-block mt-3" id="quick-movies">Quản lý phim</button><button class="btn btn-light btn-block" id="quick-tickets">Xem vé đã bán</button></div></div><p class="cms-foot">Giao diện CMS dựa trên AdminLTE 3.2.</p></div></div>`;
    document.getElementById('quick-showtime').onclick=()=>showView('showtimes');
    document.getElementById('quick-movies').onclick=()=>showView('movies');
    document.getElementById('quick-tickets').onclick=()=>showView('tickets');
  }

  async function renderGenres(el) {
    const { data } = await genreApi.list();
    genres = data;
    el.innerHTML = `
    <div class="card"><div class="card-body">
      <form id="genre-form" class="form-inline mb-3">
        <input class="form-control mr-2" id="genre-name" placeholder="Tên thể loại" required>
        <button class="btn btn-primary">Thêm</button>
      </form>
      <table class="table table-bordered"><thead><tr><th>ID</th><th>Tên</th><th></th></tr></thead>
      <tbody>${data.map((g) => `<tr><td>${g.id}</td><td>${g.name}</td><td>
        <button class="btn btn-sm btn-warning" data-edit-genre="${g.id}">Sửa</button>
        <button class="btn btn-sm btn-danger" data-del-genre="${g.id}">Xóa</button></td></tr>`).join("")}
      </tbody></table></div></div>`;
    document.getElementById("genre-form").onsubmit = async (e) => {
      e.preventDefault();
      await genreApi.create(document.getElementById("genre-name").value.trim());
      showView("genres");
    };
    el.querySelectorAll("[data-edit-genre]").forEach((btn) => {
      btn.onclick = async () => {
        const g = data.find((x) => x.id === Number(btn.dataset.editGenre));
        const name = prompt("Tên mới", g.name);
        if (name) {
          await genreApi.update(g.id, name.trim());
          showView("genres");
        }
      };
    });
    el.querySelectorAll("[data-del-genre]").forEach((btn) => {
      btn.onclick = async () => {
        if (confirm("Xóa thể loại?")) {
          try {
            await genreApi.remove(btn.dataset.delGenre);
            showView("genres");
          } catch (e) {
            handleError(e);
          }
        }
      };
    });
  }

  async function renderMovies(el) {
    if (!genres.length) {
      const g = await genreApi.list();
      genres = g.data;
    }
    const { data } = await movieApi.list();
    const genreOpts = genres.map((g) => `<option value="${g.id}">${g.name}</option>`).join("");
    el.innerHTML = `
    <div class="card"><div class="card-body">
      <form id="movie-form" class="mb-3">
        <div class="form-row">
          <div class="col-md-3"><input class="form-control" name="title" placeholder="Tên phim" required></div>
          <div class="col-md-2"><input class="form-control" name="duration" type="number" min="1" placeholder="Phút" required></div>
          <div class="col-md-2"><input class="form-control" name="releaseYear" type="number" placeholder="Năm" required></div>
          <div class="col-md-3"><select class="form-control" name="genreId" required><option value="">Thể loại</option>${genreOpts}</select></div>
          <div class="col-md-2"><button class="btn btn-primary btn-block">Thêm phim</button></div>
        </div>
      </form>
      <table class="table table-striped"><thead><tr><th>Tên</th><th>Phút</th><th>Năm</th><th>Thể loại</th><th></th></tr></thead>
      <tbody>${data.map((m) => `<tr>
        <td>${m.title}</td><td>${m.duration}</td><td>${m.releaseYear}</td>
        <td>${genres.find((g) => g.id === m.genreId)?.name ?? ""}</td>
        <td><button class="btn btn-sm btn-danger" data-del-movie="${m.id}">Xóa</button></td></tr>`).join("")}
      </tbody></table></div></div>`;
    document.getElementById("movie-form").onsubmit = async (e) => {
      e.preventDefault();
      const f = e.target;
      await movieApi.create({
        title: f.title.value.trim(),
        duration: Number(f.duration.value),
        releaseYear: Number(f.releaseYear.value),
        genreId: Number(f.genreId.value),
      });
      showView("movies");
    };
    el.querySelectorAll("[data-del-movie]").forEach((btn) => {
      btn.onclick = async () => {
        if (confirm("Xóa phim?")) {
          try {
            await movieApi.remove(btn.dataset.delMovie);
            showView("movies");
          } catch (err) {
            handleError(err);
          }
        }
      };
    });
  }

  async function renderRooms(el) {
    const { data } = await roomApi.list();
    el.innerHTML = `
    <div class="card"><div class="card-body">
      <form id="room-form" class="form-inline mb-3">
        <input class="form-control mr-2" name="name" placeholder="Tên phòng" required>
        <input class="form-control mr-2" name="seatCount" type="number" min="1" placeholder="Số ghế" required>
        <button class="btn btn-primary">Thêm phòng</button>
      </form>
      <table class="table table-bordered"><thead><tr><th>Tên</th><th>Số ghế</th><th></th></tr></thead>
      <tbody>${data.map((r) => `<tr><td>${r.name}</td><td>${r.seatCount}</td>
        <td><button class="btn btn-sm btn-danger" data-del-room="${r.id}">Xóa</button></td></tr>`).join("")}
      </tbody></table></div></div>`;
    document.getElementById("room-form").onsubmit = async (e) => {
      e.preventDefault();
      const f = e.target;
      await roomApi.create({ name: f.name.value.trim(), seatCount: Number(f.seatCount.value) });
      showView("rooms");
    };
    el.querySelectorAll("[data-del-room]").forEach((btn) => {
      btn.onclick = async () => {
        if (confirm("Xóa phòng?")) {
          try {
            await roomApi.remove(btn.dataset.delRoom);
            showView("rooms");
          } catch (err) {
            handleError(err);
          }
        }
      };
    });
  }

  async function renderShowtimes(el) {
    const [st, movies, rooms] = await Promise.all([showtimeApi.list(), movieApi.list(), roomApi.list()]);
    const movieOpts = movies.data.map((m) => `<option value="${m.id}">${m.title}</option>`).join("");
    const roomOpts = rooms.data.map((r) => `<option value="${r.id}">${r.name}</option>`).join("");
    el.innerHTML = `
    <div class="card"><div class="card-body">
      <form id="st-form" class="mb-3">
        <div class="form-row">
          <div class="col-md-3"><select class="form-control" name="movieId" required><option value="">Phim</option>${movieOpts}</select></div>
          <div class="col-md-2"><select class="form-control" name="roomId" required><option value="">Phòng</option>${roomOpts}</select></div>
          <div class="col-md-3"><input class="form-control" name="startTime" type="datetime-local" required></div>
          <div class="col-md-2"><input class="form-control" name="ticketPrice" type="number" min="0" step="1000" placeholder="Giá vé" required></div>
          <div class="col-md-2"><button class="btn btn-primary btn-block">Tạo suất</button></div>
        </div>
      </form>
      <table class="table table-sm table-hover"><thead><tr><th>Phim</th><th>Phòng</th><th>Bắt đầu</th><th>Giá</th><th></th></tr></thead>
      <tbody>${st.data.map((s) => `<tr>
        <td>${s.movieTitle}</td><td>${s.roomName}</td><td>${formatDateTime(s.startTime)}</td>
        <td>${formatMoney(s.ticketPrice)}</td>
        <td><button class="btn btn-sm btn-danger" data-del-st="${s.id}">Xóa</button></td></tr>`).join("")}
      </tbody></table></div></div>`;
    document.getElementById("st-form").onsubmit = async (e) => {
      e.preventDefault();
      const f = e.target;
      const local = f.startTime.value;
      const startTime = local.length === 16 ? `${local}:00` : local;
      await showtimeApi.create({
        movieId: Number(f.movieId.value),
        roomId: Number(f.roomId.value),
        startTime,
        ticketPrice: Number(f.ticketPrice.value),
      });
      showView("showtimes");
    };
    el.querySelectorAll("[data-del-st]").forEach((btn) => {
      btn.onclick = async () => {
        if (confirm("Xóa suất chiếu?")) {
          try {
            await showtimeApi.remove(btn.dataset.delSt);
            showView("showtimes");
          } catch (err) {
            handleError(err);
          }
        }
      };
    });
  }

  async function renderTickets(el) {
    const { data } = await ticketApi.list();
    el.innerHTML = `
    <table class="table table-bordered table-sm"><thead><tr>
      <th>Khách</th><th>Phim</th><th>Phòng</th><th>Suất</th><th>Ghế</th><th>Giá</th><th>Đặt lúc</th>
    </tr></thead><tbody>
    ${data.map((t) => `<tr>
      <td>${t.userFullName}<br><small>${t.userEmail}</small></td>
      <td>${t.movieTitle}</td><td>${t.roomName}</td><td>${formatDateTime(t.startTime)}</td>
      <td>${t.seatNumber}</td><td>${formatMoney(t.price)}</td><td>${formatDateTime(t.bookedAt)}</td>
    </tr>`).join("")}
    </tbody></table>`;
  }

  showView("dashboard");
}
