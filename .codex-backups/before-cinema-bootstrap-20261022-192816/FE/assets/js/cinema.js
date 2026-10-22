const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const $ = id => document.getElementById(id);
const artwork = [
 ['Dune: Part Two','#e1a575','#532e30','DUNE','PART TWO','Một hành trình vượt qua cát bụi, định mệnh và những vì sao.'],
 ['Inside Out 2','#80c8ae','#365979','INSIDE','OUT 2','Bước vào thế giới cảm xúc đầy sắc màu và những bất ngờ.'],
 ['Mai','#c77e75','#383352','MAI','MỘT CÂU CHUYỆN TÌNH','Một câu chuyện về tình yêu, hy vọng và những khởi đầu mới.'],
 ['Interstellar','#9aaec2','#1c304a','INTER','STELLAR','Vượt qua không gian và thời gian để tìm đường trở về.'],
 ['Spirited Away','#80af98','#184c4b','SPIRITED','AWAY','Lạc vào thế giới thần linh cùng một hành trình trưởng thành.'],
 ['Avengers: Endgame','#ac84ca','#302856','AVENGERS','ENDGAME','Các anh hùng tập hợp cho trận chiến quyết định.'],
 ['The Grand Budapest Hotel','#e8a0b3','#854d73','GRAND','BUDAPEST','Một chuyến phiêu lưu tinh tế, rực rỡ và đầy bất ngờ.'],
 ['Your Name','#859adc','#283772','YOUR','NAME','Hai cuộc đời, một sợi dây kết nối vượt qua khoảng cách.']
];
function art(title) { return artwork.find(a => a[0] === title) || [title,'#efb68f','#42496b',title.slice(0,14).toUpperCase(),'MOVIE STORE','Khám phá câu chuyện trên màn ảnh rộng.']; }
function poster(title) {
 const a = art(title); const uid = 'p' + Math.random().toString(36).slice(2);
 return `<svg viewBox="0 0 300 400" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Poster minh họa ${esc(title)}"><defs><linearGradient id="${uid}" x2=".5" y2="1"><stop stop-color="${a[1]}"/><stop offset="1" stop-color="${a[2]}"/></linearGradient></defs><rect width="300" height="400" fill="url(#${uid})"/><circle cx="180" cy="125" r="70" fill="#fff" opacity=".28"/><circle cx="180" cy="125" r="48" fill="none" stroke="#fff" opacity=".4"/><path d="M-20 300Q100 120 320 290V420H-20Z" fill="${a[2]}" opacity=".55"/><path d="M-20 320Q180 220 320 310V420H-20Z" fill="${a[2]}" opacity=".65"/><text x="25" y="280" fill="#fff" font-family="system-ui" font-size="31" font-weight="800" letter-spacing="2">${esc(a[3])}</text><text x="25" y="310" fill="#fff" font-family="system-ui" font-size="14" letter-spacing="3">${esc(a[4])}</text><text x="25" y="372" fill="#fff" opacity=".65" font-family="system-ui" font-size="9" letter-spacing="3">MOVIE STORE · SPECIAL SCREENING</text></svg>`;
}
function toast(message) { const node = document.createElement('div'); node.className = 'toast'; node.textContent = message; document.body.append(node); setTimeout(() => node.remove(),4000); }
function header() {
 const u = getUser();
 $('header').innerHTML = `<div class="topbar"><div class="wrap"><a href="index.html" class="brand"><b>m</b>movie<em>store.</em></a><nav class="navlinks"><a class="active" href="index.html#movies">Phim đang chiếu</a><a href="index.html#movies">Lịch chiếu</a><a href="my-tickets.html">Vé của tôi</a></nav><div class="userlinks">${u ? `<span>Chào, ${esc(u.fullName.split(' ').pop())}</span><button class="btn light" id="logout">Đăng xuất</button>` : '<a href="register.html">Đăng ký</a><a class="btn" href="login.html">Đăng nhập ↗</a>'}</div></div></div>`;
 if ($('logout')) $('logout').onclick = () => { clearUser(); location.href = 'index.html'; };
}
function footer() { $('footer').innerHTML = '<footer><div class="wrap"><a class="brand" href="index.html"><b>m</b>movie<em>store.</em></a><p>Chạm cảm xúc. Trọn trải nghiệm.</p><a class="muted tiny" href="../admin/login.html">Dành cho quản trị viên ↗</a></div></footer>'; }
const dayKey = value => String(value).slice(0,10);
const clock = value => new Date(value).toLocaleTimeString('vi-VN',{hour:'2-digit',minute:'2-digit'});
const dayLabel = value => new Date(value + 'T12:00:00').toLocaleDateString('vi-VN',{weekday:'short',day:'2-digit',month:'2-digit'});
let catalog = [], schedule = [], genresList = [], pickedDay = '', genreFilter = '', query = '';
async function home() {
 $('main').innerHTML = `<div class="wrap"><section class="hero"><div><span class="eyebrow">ĐIỆN ẢNH GẦN HƠN BAO GIỜ HẾT</span><h1>Một chiếc vé.<br><em>Ngàn cảm xúc.</em></h1><p>Những bộ phim bạn mong đợi, chỗ ngồi bạn yêu thích.<br>Chọn phim và lên lịch cho buổi hẹn tiếp theo.</p><a class="btn" href="#movies">Khám phá phim ngay ↗</a><div class="perks"><span>✓ Chọn ghế dễ dàng</span><span>✓ Đặt vé nhanh chóng</span><span>✓ Lưu vé trong tài khoản</span></div></div><div class="hero-art">${poster('Dune: Part Two')}<div class="floating">Màn ảnh lớn. <strong>Cảm xúc thật.</strong></div></div></section><section id="movies"><div class="section-head"><div><span class="eyebrow">LIGHTS · CAMERA · ACTION</span><h2>Hôm nay, xem gì nhỉ?</h2></div><p class="muted tiny">Phim tuyển chọn · Lịch chiếu 7 ngày tới</p></div><div class="searchbar"><input id="search" class="input" placeholder="Tìm bộ phim bạn yêu thích…" aria-label="Tìm phim"><select id="genre" class="input" aria-label="Thể loại"><option value="">Tất cả thể loại</option></select></div><div id="date-tabs" class="filters"></div><div id="movies-grid" class="movie-grid"><div class="loading">Đang tìm phim hay cho bạn…</div></div></section><div class="feature-strip"><article><b>◈</b><div><h3>Ghế đẹp trong tầm tay</h3><p>Chọn vị trí yêu thích trên sơ đồ phòng chiếu.</p></div></article><article><b>↗</b><div><h3>Đặt nhanh, tận hưởng lâu</h3><p>Vài bước đơn giản cho một buổi xem trọn vẹn.</p></div></article><article><b>♡</b><div><h3>Mọi vé, một nơi</h3><p>Xem lại thông tin vé bất cứ khi nào bạn cần.</p></div></article></div></div>`;
 try {
  const result = await Promise.all([movieApi.list(),showtimeApi.upcoming(),genreApi.list()]);
  catalog = result[0].data; schedule = result[1].data; genresList = result[2].data;
  $('genre').innerHTML += genresList.map(g => `<option value="${g.id}">${esc(g.name)}</option>`).join('');
  const days = [...new Set(schedule.map(s => dayKey(s.startTime)))].slice(0,7);
  $('date-tabs').innerHTML = '<button class="chip active" data-day="">Tất cả ngày</button>' + days.map(d => `<button class="chip" data-day="${d}">${esc(dayLabel(d))}</button>`).join('');
  document.querySelectorAll('[data-day]').forEach(b => b.onclick = () => { pickedDay = b.dataset.day; document.querySelectorAll('[data-day]').forEach(x => x.classList.toggle('active',x === b)); renderMovies(); });
  $('search').oninput = e => { query = e.target.value.toLocaleLowerCase('vi'); renderMovies(); };
  $('genre').onchange = e => { genreFilter = e.target.value; renderMovies(); };
  renderMovies();
 } catch(e) { $('movies-grid').innerHTML = '<div class="empty error">Chưa kết nối được rạp. Hãy kiểm tra backend cổng 5173 rồi tải lại trang.</div>'; }
}
function renderMovies() {
 const movies = catalog.filter(m => m.title.toLocaleLowerCase('vi').includes(query) && (!genreFilter || String(m.genreId) === genreFilter) && (!pickedDay || schedule.some(s => s.movieId === m.id && dayKey(s.startTime) === pickedDay)));
 $('movies-grid').innerHTML = movies.length ? movies.map(m => {
 const times = schedule.filter(s => s.movieId === m.id && (!pickedDay || dayKey(s.startTime) === pickedDay));
 return `<article class="movie-card"><div class="poster">${poster(m.title)}<span class="badge">${m.releaseYear} · 2D</span></div><div class="movie-info"><h3>${esc(m.title)}</h3><p>${esc(genresList.find(g => g.id === m.genreId)?.name || 'Điện ảnh')} · ${m.duration} phút</p>${times.length ? `<a class="btn" href="booking.html?movieId=${m.id}${pickedDay ? '&date='+pickedDay : ''}">Đặt vé · từ ${formatMoney(Math.min(...times.map(s => s.ticketPrice)))} ↗</a>` : '<button class="btn light" disabled>Chưa có lịch chiếu</button>'}</div></article>`;
 }).join('') : '<div class="empty">Không tìm thấy phim phù hợp. Thử tên phim hoặc ngày khác nhé.</div>';
}
let activeShowtime = null, selectedSeats = new Set(), occupied = new Set(), seatRequest = 0;
async function booking() {
 const params = new URLSearchParams(location.search);
 $('main').innerHTML = '<div class="wrap loading">Đang tải phòng chiếu…</div>';
 try {
 const [s,m] = await Promise.all([showtimeApi.upcoming(),movieApi.list()]);
 schedule = s.data;
 const requested = schedule.find(x => x.id === Number(params.get('showtimeId')));
 const movieId = Number(params.get('movieId')) || requested?.movieId;
 const movie = m.data.find(x => x.id === movieId);
 if (!movie) throw new Error('Không tìm thấy phim.');
 const movieTimes = schedule.filter(x => x.movieId === movieId);
 $('main').innerHTML = `<div class="wrap"><div class="section-head"><div><a href="index.html#movies" class="muted tiny">← Quay lại danh sách phim</a><h2>${esc(movie.title)}</h2><p class="muted">${esc(art(movie.title)[5])}</p></div><span class="eyebrow">CHỌN SUẤT → CHỌN GHẾ → NHẬN VÉ</span></div><div class="booking-layout"><section><div class="panel"><span class="eyebrow">01 · LỊCH CHIẾU</span><h2>Chọn một buổi hẹn</h2><div class="dates" id="book-dates"></div><div class="showtimes" id="times"></div></div><div class="panel" style="margin-top:20px"><span class="eyebrow">02 · VỊ TRÍ YÊU THÍCH</span><h2>Chọn ghế của bạn</h2><div class="legend"><span><i></i>Còn trống</span><span><i class="chosen"></i>Đang chọn</span><span><i class="sold"></i>Đã bán</span></div><div class="screen">MÀN HÌNH</div><div class="seat-scroll"><div class="seat-grid" id="seat-grid"></div></div></div></section><aside class="panel summary"><span class="eyebrow">VÉ XEM PHIM CỦA BẠN</span><h2>${esc(movie.title)}</h2><dl><div><dt>Suất chiếu</dt><dd id="sum-time">—</dd></div><div><dt>Phòng</dt><dd id="sum-room">—</dd></div><div><dt>Ghế</dt><dd id="sum-seats">—</dd></div></dl><div class="total"><span>Tổng cộng</span><strong id="sum-total">0 ₫</strong></div><button class="btn full" id="confirm" disabled>Xác nhận đặt vé ↗</button><p class="muted tiny" style="line-height:1.8">Kiểm tra phim, suất chiếu và ghế trước khi xác nhận. Hệ thống phát hành vé ngay sau khi đặt thành công.</p><div id="booking-error" class="error tiny" role="alert"></div></aside></div></div><dialog id="success" class="success"><div class="checkmark">✓</div><h2>Hẹn bạn tại rạp!</h2><p>Đặt vé thành công. Thông tin đã được lưu trong tài khoản.</p><a class="btn full" href="my-tickets.html">Xem vé của tôi ↗</a></dialog>`;
 const days = [...new Set(movieTimes.map(x => dayKey(x.startTime)))];
 let date = params.get('date'); if (!days.includes(date)) date = requested ? dayKey(requested.startTime) : days[0];
 function selectDay(d) {
  $('book-dates').innerHTML = days.map(x => `<button class="chip ${x === d ? 'active':''}" data-date="${x}">${esc(dayLabel(x))}</button>`).join('');
  $('book-dates').querySelectorAll('button').forEach(b => b.onclick = () => selectDay(b.dataset.date));
  const todayTimes = movieTimes.filter(x => dayKey(x.startTime) === d);
  $('times').innerHTML = todayTimes.map(x => `<button class="showtime" data-time="${x.id}">${clock(x.startTime)}<small>${esc(x.roomName)} · ${formatMoney(x.ticketPrice)}</small></button>`).join('') || '<p>Chưa có suất chiếu.</p>';
  $('times').querySelectorAll('button').forEach(b => b.onclick = () => selectTime(movieTimes.find(x => x.id === Number(b.dataset.time))));
  if (todayTimes.length) selectTime(todayTimes.find(x => x.id === requested?.id) || todayTimes[0]);
 }
 $('confirm').onclick = confirmBooking; selectDay(date);
 } catch(e) { $('main').innerHTML = `<div class="wrap"><div class="notice">${esc(e.message)} <a href="index.html">Về trang chủ →</a></div></div>`; }
}
async function selectTime(showtime) {
 const requestId = ++seatRequest; activeShowtime = null; selectedSeats.clear(); $('confirm').disabled = true; $('seat-grid').innerHTML = 'Đang tải ghế…';
 document.querySelectorAll('[data-time]').forEach(b => b.classList.toggle('active',Number(b.dataset.time) === showtime.id));
 try { const { data } = await ticketApi.bookedSeats(showtime.id); if (requestId !== seatRequest) return; activeShowtime = showtime; occupied = new Set(data.map(Number)); renderSeats(); updateOrder(); }
 catch { if(requestId === seatRequest) $('seat-grid').textContent = 'Không tải được ghế. Vui lòng chọn lại suất.'; }
}
function seatLabel(number) { return String.fromCharCode(65 + Math.floor((number-1)/10)) + (((number-1)%10)+1); }
function renderSeats() {
 $('seat-grid').innerHTML = Array.from({length:activeShowtime.seatCount},(_,i) => `<button aria-label="Ghế ${seatLabel(i+1)}" class="seat ${selectedSeats.has(i+1)?'selected':''}" data-seat="${i+1}" ${occupied.has(i+1)?'disabled':''}>${seatLabel(i+1)}</button>`).join('');
 $('seat-grid').querySelectorAll('button:not(:disabled)').forEach(b => b.onclick = () => { const n = Number(b.dataset.seat); if (selectedSeats.has(n)) selectedSeats.delete(n); else if (selectedSeats.size < 8) selectedSeats.add(n); else return toast('Mỗi lần vui lòng chọn tối đa 8 ghế.'); renderSeats(); updateOrder(); });
}
function updateOrder() { $('sum-time').textContent = formatDateTime(activeShowtime.startTime); $('sum-room').textContent = activeShowtime.roomName; $('sum-seats').textContent = [...selectedSeats].sort((a,b)=>a-b).map(seatLabel).join(', ') || 'Chưa chọn'; $('sum-total').textContent = formatMoney(activeShowtime.ticketPrice * selectedSeats.size); $('confirm').disabled = !selectedSeats.size; }
async function confirmBooking() {
 const u = getUser(); if (!u) { location.href = 'login.html?next=' + encodeURIComponent(location.pathname + location.search); return; }
 $('confirm').disabled = true; $('booking-error').textContent = '';
 try { await ticketApi.book({userId:u.id,showtimeId:activeShowtime.id,seatNumbers:[...selectedSeats]}); selectedSeats.clear(); $('success').showModal(); }
 catch(e) { $('booking-error').textContent = e.response?.data?.message || 'Ghế vừa được đặt hoặc có lỗi kết nối. Vui lòng chọn lại.'; await selectTime(activeShowtime); }
}
async function tickets() {
 const u = getUser(); if (!u) { location.href = 'login.html?next=my-tickets.html'; return; }
 $('main').innerHTML = '<div class="wrap"><div class="section-head"><div><span class="eyebrow">NHỮNG BUỔI HẸN ĐIỆN ẢNH</span><h2>Vé của tôi</h2><p class="muted">Mọi thông tin bạn cần trước khi đến rạp.</p></div><a class="btn light" href="index.html">Đặt thêm vé ↗</a></div><div id="tickets" class="ticket-grid"></div></div>';
 try { const { data } = await ticketApi.byUser(u.id); $('tickets').innerHTML = data.map(t => `<article class="ticket"><span class="ticket-code">MOVIESTORE · #${String(t.id).padStart(6,'0')}</span><h3>${esc(t.movieTitle)}</h3><p class="muted">${esc(t.roomName)} · ${formatDateTime(t.startTime)}</p><div class="total"><span>Ghế <b>${seatLabel(t.seatNumber)}</b></span><strong>${formatMoney(t.price)}</strong></div><span class="tiny muted">Đã đặt: ${formatDateTime(t.bookedAt)}</span></article>`).join('') || '<div class="empty">Bạn chưa có vé. Một bộ phim hay đang chờ bạn!</div>'; }
 catch { $('tickets').innerHTML = '<div class="empty error">Không tải được vé. Vui lòng thử lại.</div>'; }
}
function authPage(register) {
 $('main').innerHTML = `<div class="auth-layout"><section class="auth-art"><span class="eyebrow">YOUR NEXT MOVIE NIGHT</span><h1>Để mỗi buổi xem<br>là một kỷ niệm.</h1><p>Phim hay, ghế đẹp và những khoảnh khắc đáng nhớ.<br>Tất cả bắt đầu từ đây.</p>${poster('Your Name')}</section><section class="auth-form"><span class="eyebrow">MOVIE STORE MEMBERS</span><h1>${register?'Chào bạn mới!':'Chào mừng trở lại!'}</h1><p class="muted">${register?'Tạo tài khoản và lên lịch cho bộ phim tiếp theo.':'Đăng nhập để tiếp tục đặt vé yêu thích.'}</p><form id="auth-form">${register?'<label class="field">Họ và tên<input name="fullName" class="input" required minlength="2" maxlength="100" autocomplete="name" placeholder="Nguyễn Minh Anh"></label>':''}<label class="field">Email<input name="email" type="email" class="input" required autocomplete="email" placeholder="ban@example.com"></label><label class="field">Mật khẩu<input name="password" type="password" class="input" required minlength="6" autocomplete="${register?'new-password':'current-password'}" placeholder="Tối thiểu 6 ký tự"></label><p id="auth-error" class="error tiny" role="alert"></p><button class="btn full" type="submit">${register?'Tạo tài khoản':'Đăng nhập'} ↗</button></form><p class="auth-foot">${register?'Đã có tài khoản? <a href="login.html">Đăng nhập</a>':'Chưa có tài khoản? <a href="register.html">Đăng ký ngay</a>'}</p></section></div>`;
 $('auth-form').onsubmit = async e => { e.preventDefault(); const button = e.target.querySelector('button'); button.disabled = true; $('auth-error').textContent = ''; const values = Object.fromEntries(new FormData(e.target));
  try { const { data } = await (register ? userApi.register({...values,role:'CUSTOMER'}) : userApi.login(values)); setUser(data); const next = new URLSearchParams(location.search).get('next'); const destination = next ? new URL(next,location.href) : null; location.href = destination && destination.origin === location.origin && destination.pathname.startsWith('/customer/') ? destination.href : 'index.html'; }
  catch(e) { $('auth-error').textContent = e.response?.data?.message || (register?'Không tạo được tài khoản. Email có thể đã được sử dụng.':'Email hoặc mật khẩu không đúng.'); button.disabled = false; }
 };
}
header(); footer();
const page = document.body.dataset.page;
if(page === 'home') home(); else if(page === 'booking') booking(); else if(page === 'tickets') tickets(); else authPage(page === 'register');
