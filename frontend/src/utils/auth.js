const TOKEN_KEY = "blog_token";
const USER_KEY = "blog_user";

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || "";
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function getUserInfo() {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function setUserInfo(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearUserInfo() {
  localStorage.removeItem(USER_KEY);
}

export function clearAuth() {
  clearToken();
  clearUserInfo();
}

export function getUserRole() {
  const user = getUserInfo();
  if (!user) {
    return null;
  }
  const role = Number(user.role);
  return Number.isFinite(role) ? role : null;
}

export function isAdminRole(role) {
  return Number(role) === 1;
}

export function getHomePathByRole(role) {
  return isAdminRole(role) ? "/admin" : "/user";
}

export function getCurrentHomePath() {
  return getHomePathByRole(getUserRole());
}
