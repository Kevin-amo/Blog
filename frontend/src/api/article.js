import request from "../utils/request";
import { getToken } from "../utils/auth";

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

export async function generateArticleSummaryStreamApi(payload, handlers = {}) {
  const baseURL = import.meta.env.VITE_API_BASE || "/api";
  const token = getToken();
  const response = await fetch(`${baseURL}/ai/article-summary/stream`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(payload),
    signal: handlers.signal
  });

  if (response.status === 401) {
    throw new Error("登录已失效，请重新登录");
  }
  if (!response.ok || !response.body) {
    throw new Error("AI 总结生成失败，请稍后重试");
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");
  let buffer = "";

  while (true) {
    const { value, done } = await reader.read();
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });
    buffer = consumeSseBuffer(buffer, handlers);
  }

  buffer += decoder.decode();
  consumeSseBuffer(`${buffer}\n\n`, handlers);
}

function consumeSseBuffer(buffer, handlers) {
  const parts = buffer.split(/\r?\n\r?\n/);
  const rest = parts.pop() || "";
  parts.forEach((part) => dispatchSseEvent(part, handlers));
  return rest;
}

function dispatchSseEvent(rawEvent, handlers) {
  const lines = rawEvent.split(/\r?\n/);
  let eventName = "message";
  const dataLines = [];

  lines.forEach((line) => {
    if (line.startsWith("event:")) {
      eventName = line.slice(6).trim();
      return;
    }
    if (line.startsWith("data:")) {
      dataLines.push(line.slice(5).trimStart());
    }
  });

  const data = dataLines.join("\n");
  if (eventName === "chunk") {
    handlers.onChunk?.(data);
    return;
  }
  if (eventName === "done") {
    handlers.onDone?.();
    return;
  }
  if (eventName === "error") {
    handlers.onError?.(data || "AI 总结生成失败，请稍后重试");
  }
}