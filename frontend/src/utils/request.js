import axios from "axios";
import { clearAuth, getToken } from "./auth";

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || "/api",
  timeout: 10000
});

request.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload?.code === 401) {
      clearAuth();
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
      return Promise.reject(new Error(payload.message || "µÇÂ¼ÒÑÊ§Ð§"));
    }
    return payload;
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuth();
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default request;
