import request from "../utils/request";

export function updateMyProfileApi(payload) {
  return request.put("/user/profile", payload);
}

export function updateMyPasswordApi(payload) {
  return request.put("/user/password", payload);
}
