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

// 读取已登录的手机号（用户通过详情页弹层输入后写入）。
// 首次访问没有 cookie 时返回 null，由前端决定是否弹出登录层。
// cookie key = killPhone，与后端 SeckillController.@CookieValue("killPhone") 一致（沿用慕课原版命名）。
// 生产应改为真实登录态（JWT / session），避免前端伪造。
function getPhone() {
  return getCookie('killPhone');
}

// 将用户输入的手机号写入 cookie，保留 7 天。
function setPhone(p) {
  setCookie('killPhone', p, 7);
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

// POST 无请求体（后端用 @RequestParam 接收），仅带 query 参数即可。
// credentials:'include' 显式带上 cookie（后端从 killPhone cookie 读 userPhone）
async function postJSON(url) {
  const res = await fetch(url, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' }
  });
  const data = await res.json().catch(() => null);
  if (data && typeof data === 'object' && 'code' in data) return data;
  return { code: res.status || 500, message: (data && data.message) || '请求失败', data: null };
}

// 解析秒杀执行结果 Result<SecKillExecution>：
//   - 成功：code === 0 且 data.state === 2，stateInfo = "秒杀成功"
//   - 失败：后端把 SecKillExecution 放进 data，stateInfo 即标准失败原因
//           （重复秒杀 / 数据篡改 / 用户未注册 / 秒杀结束 等）
// 返回 { success, msg, cls }，页面直接展示。失败原因优先 data.stateInfo，兜底 message。
function resolveExec(r) {
  const exec = (r && r.data) || null;
  const reason = (exec && exec.stateInfo) || (r && r.message) || '未知原因';
  if (r && r.code === 0 && exec && exec.state === 2) {
    return { success: true, msg: '秒杀成功！', cls: 'alert-success' };
  }
  const codeSuffix = (r && r.code && r.code !== 0) ? '（code ' + r.code + '）' : '';
  return { success: false, msg: '秒杀失败：' + reason + codeSuffix, cls: 'alert-danger' };
}
