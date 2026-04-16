import request from "../utils/request";

export function listCommentApi(articleId) {
  return request.get(`/comment/list/${articleId}`);
}

export function addCommentApi(payload) {
  return request.post("/comment/add", payload);
}

export function deleteCommentApi(id) {
  return request.delete(`/comment/${id}`);
}
