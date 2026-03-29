import request from "../utils/request";

export function loginApi(payload) {
  return request.post("/user/login", payload);
}

export function registerApi(payload) {
  return request.post("/user/register", payload);
}

export function logoutApi() {
  return request.post("/user/logout");
}

export function getMyProfileApi() {
  return request.get("/user/me");
}
