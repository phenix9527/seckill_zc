// 通用工具：cookie 登录态(模拟)、时间格式化、AJAX 封装
// 当前端通过 /seckill/... 调后端 REST 时使用。

function getCookie(name) {
  const m = document.cookie.match(new RegExp('(^| )' + name + '=([^;]*)'));
  return m ? decodeURIComponent(m[2]) : null;
}

function setCookie(name, value, days) {
  const d = new Date();
  d.setTime(d.getTime() + days * 24 * 3600 * 1000);
  document.cookie = name + '=' + encodeURIComponent(value) + ';expires=' + d.toUTCString() + ';path=/';
}

// 模拟登录：首次访问随机生成一个 11 位手机号，存到 cookie，后续请求复用。
// 生产应改为真实登录态（JWT / session），避免前端伪造 userPhone。
function getPhone() {
  let p = getCookie('userPhone');
  if (!p) {
    const suffix = Math.floor(Math.random() * 9000000000) + 1000000000; // 10 位
    p = '1' + suffix; // 11 位，以 1 开头
    setCookie('userPhone', p, 7);
  }
  return p;
}

// LocalDateTime(JSON 为 ISO 字符串) -> 毫秒时间戳
function fmt(iso) {
  return iso ? new Date(iso).getTime() : 0;
}

function pad(n) {
  return n < 10 ? '0' + n : '' + n;
}

function fmtDateTime(ms) {
  if (!ms) return '';
  const d = new Date(ms);
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) +
    ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
}

// 后端统一返回 Result{code,message,data}；这里无论 HTTP 状态码，都尽量解析成 Result 结构，
// 让页面统一通过 resp.code / resp.data 处理（成功 code=0）。
async function getJSON(url) {
  const res = await fetch(url, { method: 'GET', headers: { 'Accept': 'application/json' } });
  const data = await res.json().catch(() => null);
  if (data && typeof data === 'object' && 'code' in data) return data;
  return { code: res.status || 500, message: (data && data.message) || '请求失败', data: null };
}

// POST 无请求体（后端用 @RequestParam 接收），仅带 query 参数即可
async function postJSON(url) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' }
  });
  const data = await res.json().catch(() => null);
  if (data && typeof data === 'object' && 'code' in data) return data;
  return { code: res.status || 500, message: (data && data.message) || '请求失败', data: null };
}
