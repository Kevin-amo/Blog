import request from "../utils/request";

export function pageAdminUsersApi(params) {
  return request.get("/admin/users/page", { params });
}

export function updateAdminUserStatusApi(id, status) {
  return request.put(`/admin/users/${id}/status`, { status });
}

export function resetAdminUserPasswordApi(id) {
  return request.put(`/admin/users/${id}/reset-password`);
}

export function deleteAdminUserApi(id) {
  return request.delete(`/admin/users/${id}`);
}

export function pageReviewArticlesApi(params) {
  return request.get("/admin/articles/review/page", { params });
}

export function detailReviewArticleApi(id) {
  return request.get(`/admin/articles/${id}`);
}

export function auditReviewArticleApi(id, auditStatus) {
  return request.put(`/admin/articles/${id}/audit`, { auditStatus });
}
