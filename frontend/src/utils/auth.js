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
