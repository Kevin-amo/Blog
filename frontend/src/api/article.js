import request from "../utils/request";

export function pageArticleApi(params) {
  return request.get("/article/page", { params });
}

export function listArticleApi(params) {
  return request.get("/article/list", { params });
}

export function publicListArticleApi(params) {
  return request.get("/article/public/list", { params });
}

export function detailArticleApi(id) {
  return request.get(`/article/${id}`);
}

export function publicDetailArticleApi(id) {
  return request.get(`/article/public/${id}`);
}

export function addArticleApi(payload) {
  return request.post("/article", payload);
}

export function updateArticleApi(payload) {
  return request.put("/article", payload);
}

export function deleteArticleApi(id) {
  return request.delete(`/article/${id}`);
}
