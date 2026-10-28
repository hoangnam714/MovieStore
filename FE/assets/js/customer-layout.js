function onCustomerHeader() {
  const currentUser = getUser();
  const ticketsPage = location.pathname.includes("/my-tickets/");
  const accountPage = location.pathname.includes("/account/");
  const homeActive = ticketsPage || accountPage ? "" : "active";
  const accountLinks = currentUser
    ? `
      <a class="account-link" href="/customer/account/index.html" title="Tài khoản và đổi mật khẩu">
        ${getSafeHtml(currentUser.fullName)}
      </a>
      <button class="btn light" id="logout" onclick="onCustomerLogout()">Đăng xuất</button>
    `
    : `
      <a href="/customer/register/index.html">Đăng ký</a>
      <a class="btn" href="/customer/login/index.html">Đăng nhập</a>
    `;

  document.getElementById("header").innerHTML = `
    <nav class="topbar navbar">
      <div class="wrap container">
        <a href="/customer/home/index.html" class="brand" aria-label="Movie Store Cinema - trang chủ">
          <span class="brand-line">
            <span class="brand-mark" aria-hidden="true">✦</span>
            MOVIE
            <span>STORE</span>
          </span>
          <small>CINEMA & TICKETS</small>
        </a>
        <div class="navlinks">
          <a class="${homeActive}" href="/customer/home/index.html#movies">Phim & lịch chiếu</a>
          <a class="${ticketsPage ? "active" : ""}" href="/customer/my-tickets/index.html">Vé của tôi</a>
        </div>
        <div class="userlinks">${accountLinks}</div>
      </div>
    </nav>
    <nav class="mobile-nav" aria-label="Điều hướng khách hàng">
      <a class="${homeActive}" href="/customer/home/index.html#movies">Phim & lịch chiếu</a>
      <a class="${ticketsPage ? "active" : ""}" href="/customer/my-tickets/index.html">Vé của tôi</a>
      ${
        currentUser
          ? `
            <a class="${accountPage ? "active" : ""}" href="/customer/account/index.html">Tài khoản</a>
          `
          : ""
      }
    </nav>
  `;
}

function onCustomerLogout() {
  onLogout();
  location.href = "/customer/home/index.html";
}

function onCustomerFooter() {
  document.getElementById("footer").innerHTML = `
    <footer>
      <div class="wrap container">
        <div>
          <a class="brand" href="/customer/home/index.html">
            <span class="brand-line">
              <span class="brand-mark" aria-hidden="true">✦</span>
              MOVIE
              <span>STORE</span>
            </span>
            <small>CINEMA & TICKETS</small>
          </a>
          <p>Khoảnh khắc điện ảnh bắt đầu từ đây.</p>
        </div>
        <div class="footer-links">
          <a href="/customer/home/index.html#movies">Lịch chiếu</a>
          <a href="/customer/my-tickets/index.html">Vé của tôi</a>
          <a href="/admin/login/index.html">Quản trị rạp</a>
        </div>
      </div>
      <div class="wrap container footer-credit">
        © 2026 MovieStore · Dữ liệu lịch chiếu mẫu. Ảnh phim: TMDB / SensCritique.
      </div>
    </footer>
  `;
}

onCustomerHeader();
onCustomerFooter();
