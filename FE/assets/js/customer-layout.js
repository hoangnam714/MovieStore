function onCustomerHeader() {
  const currentUser = getUser();
  const accountLinks = currentUser
    ? `
      <span>${getSafeHtml(currentUser.fullName)}</span>
      <button class="btn light" id="logout" onclick="onCustomerLogout()">Đăng xuất</button>
    `
    : `
      <a href="/customer/register/index.html">Đăng ký</a>
      <a class="btn" href="/customer/login/index.html">Đăng nhập</a>
    `;

  document.getElementById("header").innerHTML = `
    <nav class="topbar navbar">
      <div class="wrap container">
        <a href="/customer/home/index.html" class="brand">
          MOVIE
          <span>STORE</span>
          <small>CINEMA</small>
        </a>
        <div class="navlinks">
          <a class="active" href="/customer/home/index.html#movies">Phim & lịch chiếu</a>
          <a href="/customer/my-tickets/index.html">Vé của tôi</a>
        </div>
        <div class="userlinks">${accountLinks}</div>
      </div>
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
            MOVIE
            <span>STORE</span>
          </a>
          <p>Phim. Lịch chiếu. Vé của bạn.</p>
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
