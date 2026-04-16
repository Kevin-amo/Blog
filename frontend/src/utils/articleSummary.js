export function finalizeArticleSummary(summary = "", maxLength = 180) {
  const normalized = String(summary)
    .replace(/\r/g, "")
    .replace(/\n+/g, " ")
    .replace(/\s+/g, " ")
    .trim();

  const limit = Number(maxLength);
  if (!normalized || !Number.isFinite(limit) || limit <= 0) {
    return normalized;
  }

  return normalized.length <= limit ? normalized : normalized.slice(0, limit).trim();
}