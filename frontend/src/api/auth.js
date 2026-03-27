import request from "../utils/request";

export function loginApi(payload) {
  return request.post("/auth/login", payload);
}

export function logoutApi() {
  return request.post("/auth/logout");
}

export function getMyProfileApi() {
  return request.get("/user/me");
}
