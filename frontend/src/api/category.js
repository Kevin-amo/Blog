import request from "../utils/request";

export function pageCategoryApi(params) {
  return request.get("/category", { params });
}

export function detailCategoryApi(id) {
  return request.get(`/category/${id}`);
}

export function addCategoryApi(payload) {
  return request.post("/category", payload);
}

export function updateCategoryApi(payload) {
  return request.put("/category", payload);
}

export function deleteCategoryApi(id) {
  return request.delete(`/category/${id}`);
}

export function optionsCategoryApi() {
  return request.get("/category/options");
}
