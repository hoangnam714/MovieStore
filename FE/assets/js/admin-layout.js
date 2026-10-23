const adminUser = getUserRole("ADMIN", "/admin/login/index.html");

function onAdminLayout() {
  document.getElementById("admin-layout").outerHTML = `
    <nav class="main-header navbar navbar-expand navbar-white navbar-light">
      <ul class="navbar-nav">
        <li class="nav-item">
          <a class="nav-link" data-widget="pushmenu" href="#" role="button" aria-label="Mở menu">
            <i class="fas fa-bars"></i>
          </a>
        </li>
        <li class="nav-item d-none d-sm-block">
          <a class="nav-link" href="/customer/home/index.html" target="_blank" rel="noopener">
            Xem website
            <i class="fas fa-external-link-alt ml-2"></i>
          </a>
        </li>
      </ul>
      <ul class="navbar-nav ml-auto">
        <li class="nav-item">
          <span class="nav-link" id="admin-name"></span>
        </li>
        <li class="nav-item">
          <button class="btn btn-link nav-link" id="logout-btn">Đăng xuất</button>
        </li>
      </ul>
    </nav>
    <aside class="main-sidebar sidebar-dark-primary elevation-4">
      <a href="/admin/dashboard/index.html" class="brand-link">
        <span class="brand-text font-weight-light">Movie Store CMS</span>
      </a>
      <div class="sidebar">
        <nav class="mt-2">
          <ul id="admin-menu" class="nav nav-pills nav-sidebar flex-column">
            <li class="nav-item">
              <a href="/admin/dashboard/index.html" class="nav-link" data-view="dashboard">
                <i class="nav-icon fas fa-tachometer-alt"></i>
                <p>Tổng quan</p>
              </a>
            </li>
            <li class="nav-item">
              <a href="/admin/genres/index.html" class="nav-link" data-view="genres">
                <i class="nav-icon fas fa-tags"></i>
                <p>Thể loại</p>
              </a>
            </li>
            <li class="nav-item">
              <a href="/admin/movies/index.html" class="nav-link" data-view="movies">
                <i class="nav-icon fas fa-film"></i>
                <p>Phim</p>
              </a>
            </li>
            <li class="nav-item">
              <a href="/admin/rooms/index.html" class="nav-link" data-view="rooms">
                <i class="nav-icon fas fa-door-open"></i>
                <p>Phòng chiếu</p>
              </a>
            </li>
            <li class="nav-item">
              <a href="/admin/showtimes/index.html" class="nav-link" data-view="showtimes">
                <i class="nav-icon fas fa-calendar"></i>
                <p>Suất chiếu</p>
              </a>
            </li>
            <li class="nav-item">
              <a href="/admin/tickets/index.html" class="nav-link" data-view="tickets">
                <i class="nav-icon fas fa-ticket-alt"></i>
                <p>Vé đã bán</p>
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </aside>
  `;
  document.getElementById("admin-name").textContent = adminUser.fullName;
  document.getElementById("logout-btn").addEventListener("click", function () {
    onLogout();
    location.href = "/admin/login/index.html";
  });
  const currentPage = document.body.dataset.page;
  document
    .getElementById("admin-menu")
    .querySelectorAll("[data-view]")
    .forEach(function (link) {
      link.classList.toggle("active", link.dataset.view === currentPage);
    });
}

function onAdminNavigate(page) {
  location.href = "/admin/" + page + "/index.html";
}

function onAdminMessage(message) {
  const notification = document.createElement("div");
  notification.className = "cms-toast";
  notification.textContent = message;
  document.body.append(notification);
  setTimeout(() => notification.remove(), 5000);
}

function onAdminPage(getPage) {
  if (!adminUser) return;
  const content = document.getElementById("main-content");

  return getPage(content)
    .then(() => {
      content.querySelectorAll("table").forEach(function (table) {
        const wrapper = document.createElement("div");
        wrapper.className = "table-responsive";
        table.before(wrapper);
        wrapper.append(table);
      });
    })
    .catch((error) => {
      console.error(error);
    });
}

window.addEventListener("unhandledrejection", function (event) {
  event.preventDefault();
  onAdminMessage(
    event.reason?.response?.data?.message ||
      "Thao tác chưa thành công. Kiểm tra dữ liệu và thử lại."
  );
});

if (adminUser) onAdminLayout();
