<template>
  <main class="home-page editor-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">Wavelog Blog</p>
        <h1>{{ isEdit ? "编辑博客" : "新建博客" }}</h1>
      </div>
      <div class="editor-top-actions">
        <button class="ghost-btn" type="button" @click="goBack">返回管理台</button>
        <button class="submit-btn" type="button" :disabled="saving" @click="submitArticle">
          {{ saving ? "发布中..." : "发布文章" }}
        </button>
      </div>
    </header>

    <section class="panel article-editor-form-panel">
      <form class="editor-form" @submit.prevent="submitArticle">
        <label>
          标题
          <input v-model.trim="form.title" type="text" maxlength="100" placeholder="请输入文章标题" />
        </label>

        <label>
          <span class="field-label-with-action">
            <span>摘要</span>
            <button
              class="ghost-btn"
              type="button"
              :disabled="summaryGenerating || !canGenerateSummary"
              @click="generateSummary"
            >
              {{ summaryGenerating ? "生成中..." : "AI 生成摘要" }}
            </button>
          </span>
          <textarea
            v-model.trim="form.summary"
            rows="2"
            maxlength="200"
            placeholder="一句话概括这篇文章（可选）"
          ></textarea>
        </label>

        <div class="editor-grid">
          <label>
            分类
            <div class="select-shell">
              <select v-model="form.categoryId" class="select-control">
                <option value="">请选择分类</option>
                <option v-for="item in categoryOptions" :key="item.id" :value="String(item.id)">
                  {{ item.name }}
                </option>
              </select>
            </div>
          </label>

          <label>
            是否置顶
            <div class="select-shell">
              <select v-model="form.isTop" class="select-control">
                <option value="0">否</option>
                <option value="1">是</option>
              </select>
            </div>
          </label>
        </div>

        <label>
          封面URL
          <input v-model.trim="form.coverUrl" type="text" placeholder="可选，如 https://..." />
        </label>
      </form>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
      <p v-if="autoSaveMessage" class="muted">{{ autoSaveMessage }}</p>
    </section>

    <section class="panel editor-shell">
      <div class="editor-toolbar">
        <button class="ghost-btn" type="button" @click="insertHeading(2)">H2</button>
        <button class="ghost-btn" type="button" @click="insertHeading(3)">H3</button>
        <button class="ghost-btn" type="button" @click="insertBold">加粗</button>
        <button class="ghost-btn" type="button" @click="insertItalic">斜体</button>
        <button class="ghost-btn" type="button" @click="insertQuote">引用</button>
        <button class="ghost-btn" type="button" @click="insertCodeBlock">代码块</button>
        <button class="ghost-btn" type="button" @click="insertLink">链接</button>
        <button class="ghost-btn" type="button" @click="insertUnorderedList">无序列表</button>
        <button class="ghost-btn" type="button" @click="insertOrderedList">有序列表</button>
      </div>

      <div class="editor-workspace">
        <section class="editor-pane">
          <div class="editor-pane-title">Markdown 输入</div>
          <textarea
            ref="textareaRef"
            v-model="form.content"
            class="markdown-input"
            placeholder="在这里输入 Markdown 内容..."
          ></textarea>
        </section>

        <section class="editor-pane">
          <div class="editor-pane-title">实时预览</div>
          <div class="markdown-preview markdown-body" v-html="previewHtml"></div>
        </section>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  addArticleApi,
  detailArticleApi,
  generateArticleSummaryApi,
  updateArticleApi
} from "../api/article";
import { optionsCategoryApi } from "../api/category";
import { parseMarkdownToHtml } from "../utils/markdown";

const DRAFT_STATUS = 0;
const PUBLISHED_STATUS = 1;
const AUTO_SAVE_DELAY = 1200;
const LOCAL_DRAFT_KEY = "blog_editor_local_draft";

const router = useRouter();
const route = useRoute();

const categoryOptions = ref([]);
const saving = ref(false);
const loadingDetail = ref(false);
const autoSaving = ref(false);
const summaryGenerating = ref(false);
const errorMsg = ref("");
const autoSaveMessage = ref("");
const textareaRef = ref(null);

const hydrating = ref(true);
const hasPendingChanges = ref(false);
const draftArticleId = ref(null);
let autoSaveTimer = null;
let autoSavePromise = null;

const form = reactive({
  title: "",
  summary: "",
  content: "",
  coverUrl: "",
  categoryId: "",
  isTop: "0"
});

const articleId = computed(() => {
  const raw = Number(route.params.id);
  return Number.isFinite(raw) && raw > 0 ? raw : null;
});
const isEdit = computed(() => Boolean(articleId.value));

const previewHtml = computed(() => {
  if (!form.content.trim()) {
    return "<p class='muted'>预览区会在这里实时显示。</p>";
  }
  return parseMarkdownToHtml(form.content);
});

const canGenerateSummary = computed(() => Boolean(form.title.trim() && form.content.trim()));

function getCurrentArticleId() {
  return articleId.value || draftArticleId.value;
}

function hasDraftContent() {
  return Boolean(
    form.title.trim() ||
      form.summary.trim() ||
      form.content.trim() ||
      form.coverUrl.trim() ||
      form.categoryId
  );
}

function clearAutoSaveTimer() {
  if (!autoSaveTimer) {
    return;
  }
  clearTimeout(autoSaveTimer);
  autoSaveTimer = null;
}

function scheduleAutoSave() {
  if (saving.value || loadingDetail.value || hydrating.value) {
    return;
  }
  clearAutoSaveTimer();
  autoSaveTimer = setTimeout(() => {
    void autoSaveDraft();
  }, AUTO_SAVE_DELAY);
}

function markDirty() {
  if (hydrating.value) {
    return;
  }
  hasPendingChanges.value = true;
  scheduleAutoSave();
}

watch(
  [
    () => form.title,
    () => form.summary,
    () => form.content,
    () => form.coverUrl,
    () => form.categoryId,
    () => form.isTop
  ],
  () => {
    markDirty();
  }
);

function formatTime() {
  return new Date().toLocaleTimeString("zh-CN", {
    hour12: false
  });
}

function saveLocalDraft() {
  if (isEdit.value || draftArticleId.value) {
    return;
  }
  const localPayload = {
    title: form.title,
    summary: form.summary,
    content: form.content,
    coverUrl: form.coverUrl,
    categoryId: form.categoryId,
    isTop: form.isTop
  };
  localStorage.setItem(LOCAL_DRAFT_KEY, JSON.stringify(localPayload));
}

function loadLocalDraft() {
  if (isEdit.value) {
    return;
  }
  const raw = localStorage.getItem(LOCAL_DRAFT_KEY);
  if (!raw) {
    return;
  }
  try {
    const data = JSON.parse(raw);
    form.title = data.title || "";
    form.summary = data.summary || "";
    form.content = data.content || "";
    form.coverUrl = data.coverUrl || "";
    form.categoryId = data.categoryId || "";
    form.isTop = data.isTop != null ? String(data.isTop) : "0";
  } catch {
    localStorage.removeItem(LOCAL_DRAFT_KEY);
  }
}

function clearLocalDraft() {
  localStorage.removeItem(LOCAL_DRAFT_KEY);
}

function buildPayload(status) {
  const payload = {
    title: form.title.trim(),
    summary: form.summary.trim(),
    content: form.content,
    coverUrl: form.coverUrl.trim(),
    categoryId: form.categoryId ? Number(form.categoryId) : null,
    status,
    isTop: Number(form.isTop)
  };

  const currentId = getCurrentArticleId();
  if (currentId) {
    payload.id = currentId;
  }

  return payload;
}

async function runAutoSaveDraft() {
  if (saving.value || loadingDetail.value || hydrating.value || autoSaving.value) {
    return;
  }
  if (!hasPendingChanges.value || !hasDraftContent()) {
    return;
  }
  if (!getCurrentArticleId() && !form.categoryId) {
    saveLocalDraft();
    autoSaveMessage.value = "已暂存本地草稿，选择分类后会自动保存到服务器";
    return;
  }

  autoSaving.value = true;
  autoSaveMessage.value = "草稿自动保存中...";

  try {
    const payload = buildPayload(DRAFT_STATUS);
    const editingId = getCurrentArticleId();
    const res = editingId ? await updateArticleApi(payload) : await addArticleApi(payload);

    if (res.code !== 200) {
      autoSaveMessage.value = res.message || "草稿自动保存失败";
      return;
    }

    if (!editingId) {
      const createdId = Number(res.data || 0);
      if (createdId > 0) {
        draftArticleId.value = createdId;
        if (!articleId.value) {
          await router.replace(`/user/articles/${createdId}/edit`);
        }
      }
    }

    hasPendingChanges.value = false;
    errorMsg.value = "";
    clearLocalDraft();
    autoSaveMessage.value = `草稿已自动保存（${formatTime()}）`;
  } catch (error) {
    autoSaveMessage.value = error.response?.data?.message || error.message || "草稿自动保存失败";
  } finally {
    autoSaving.value = false;
  }
}

async function autoSaveDraft() {
  if (autoSavePromise) {
    await autoSavePromise;
  }
  const task = runAutoSaveDraft();
  autoSavePromise = task;
  await task;
  if (autoSavePromise === task) {
    autoSavePromise = null;
  }
}

async function flushAutoSave() {
  clearAutoSaveTimer();
  await autoSaveDraft();
}

async function goBack() {
  await flushAutoSave();
  router.push("/user");
}

function applyContentAndSelection(nextValue, start, end) {
  form.content = nextValue;
  nextTick(() => {
    if (!textareaRef.value) {
      return;
    }
    textareaRef.value.focus();
    textareaRef.value.setSelectionRange(start, end);
  });
}

function wrapSelection(prefix, suffix, placeholder) {
  const el = textareaRef.value;
  if (!el) {
    return;
  }
  const start = el.selectionStart;
  const end = el.selectionEnd;
  const selected = form.content.slice(start, end) || placeholder;
  const inserted = `${prefix}${selected}${suffix}`;
  const nextValue = `${form.content.slice(0, start)}${inserted}${form.content.slice(end)}`;
  const selectionStart = start + prefix.length;
  const selectionEnd = selectionStart + selected.length;
  applyContentAndSelection(nextValue, selectionStart, selectionEnd);
}

function prefixCurrentLines(prefix) {
  const el = textareaRef.value;
  if (!el) {
    return;
  }

  const start = el.selectionStart;
  const end = el.selectionEnd;
  const value = form.content;

  const lineStart = value.lastIndexOf("\n", Math.max(0, start - 1)) + 1;
  const lineEndRaw = value.indexOf("\n", end);
  const lineEnd = lineEndRaw === -1 ? value.length : lineEndRaw;

  const selectedBlock = value.slice(lineStart, lineEnd);
  const prefixed = selectedBlock
    .split("\n")
    .map((line) => `${prefix}${line}`)
    .join("\n");

  const nextValue = `${value.slice(0, lineStart)}${prefixed}${value.slice(lineEnd)}`;
  applyContentAndSelection(nextValue, lineStart, lineStart + prefixed.length);
}

function insertHeading(level) {
  prefixCurrentLines(`${"#".repeat(level)} `);
}

function insertBold() {
  wrapSelection("**", "**", "加粗文本");
}

function insertItalic() {
  wrapSelection("*", "*", "斜体文本");
}

function insertQuote() {
  prefixCurrentLines("> ");
}

function insertUnorderedList() {
  prefixCurrentLines("- ");
}

function insertOrderedList() {
  prefixCurrentLines("1. ");
}

function insertLink() {
  wrapSelection("[", "](https://example.com)", "链接文本");
}

function insertCodeBlock() {
  const el = textareaRef.value;
  if (!el) {
    return;
  }

  const start = el.selectionStart;
  const end = el.selectionEnd;
  const selected = form.content.slice(start, end) || "在这里写代码";
  const inserted = `\n\`\`\`\n${selected}\n\`\`\`\n`;
  const nextValue = `${form.content.slice(0, start)}${inserted}${form.content.slice(end)}`;
  const selectionStart = start + 5;
  const selectionEnd = selectionStart + selected.length;
  applyContentAndSelection(nextValue, selectionStart, selectionEnd);
}

function validatePublish() {
  if (!form.title.trim()) {
    return "请输入文章标题";
  }
  if (!form.content.trim()) {
    return "请输入文章内容";
  }
  if (!form.categoryId) {
    return "请选择分类";
  }
  return "";
}

async function loadCategoryOptions() {
  try {
    const res = await optionsCategoryApi();
    if (res.code === 200) {
      categoryOptions.value = Array.isArray(res.data) ? res.data : [];
      return;
    }
    categoryOptions.value = [];
  } catch {
    categoryOptions.value = [];
  }
}

async function loadDetail() {
  if (!isEdit.value) {
    return;
  }

  loadingDetail.value = true;
  errorMsg.value = "";

  try {
    const res = await detailArticleApi(articleId.value);
    if (res.code !== 200) {
      errorMsg.value = res.message || "获取文章详情失败";
      return;
    }

    const detail = res.data || {};
    draftArticleId.value = detail.id || articleId.value;
    form.title = detail.title || "";
    form.summary = detail.summary || "";
    form.content = detail.content || "";
    form.coverUrl = detail.coverUrl || "";
    form.categoryId = detail.categoryId ? String(detail.categoryId) : "";
    form.isTop = detail.isTop != null ? String(detail.isTop) : "0";
  } catch (error) {
    errorMsg.value = error.response?.data?.message || error.message || "获取文章详情失败";
  } finally {
    loadingDetail.value = false;
  }
}

async function generateSummary() {
  if (summaryGenerating.value || !canGenerateSummary.value) {
    return;
  }

  if (form.summary.trim()) {
    const shouldOverwrite = window.confirm("当前已填写摘要，是否使用 AI 结果覆盖？");
    if (!shouldOverwrite) {
      return;
    }
  }

  summaryGenerating.value = true;
  errorMsg.value = "";

  try {
    const res = await generateArticleSummaryApi({
      title: form.title.trim(),
      content: form.content.trim(),
      maxLength: 120
    });

    if (res.code !== 200) {
      errorMsg.value = res.message || "生成摘要失败";
      return;
    }

    form.summary = res.data?.summary || "";
  } catch (error) {
    errorMsg.value = error.response?.data?.message || error.message || "生成摘要失败";
  } finally {
    summaryGenerating.value = false;
  }
}

async function submitArticle() {
  if (saving.value || loadingDetail.value) {
    return;
  }

  errorMsg.value = validatePublish();
  if (errorMsg.value) {
    return;
  }

  clearAutoSaveTimer();
  if (autoSavePromise) {
    await autoSavePromise;
  }

  saving.value = true;
  autoSaveMessage.value = "";
  try {
    const payload = buildPayload(PUBLISHED_STATUS);
    const currentId = getCurrentArticleId();
    const res = currentId
      ? await updateArticleApi(payload)
      : await addArticleApi(payload);

    if (res.code !== 200) {
      errorMsg.value = res.message || "发布失败";
      return;
    }

    hasPendingChanges.value = false;
    clearLocalDraft();
    router.push("/user");
  } catch (error) {
    errorMsg.value = error.response?.data?.message || error.message || "发布失败";
  } finally {
    saving.value = false;
  }
}

function handleBeforeUnload() {
  if (!hasPendingChanges.value || !hasDraftContent()) {
    return;
  }
  saveLocalDraft();
  void autoSaveDraft();
}

onMounted(async () => {
  await loadCategoryOptions();
  await loadDetail();
  loadLocalDraft();
  hydrating.value = false;
  window.addEventListener("beforeunload", handleBeforeUnload);
});

onBeforeUnmount(() => {
  window.removeEventListener("beforeunload", handleBeforeUnload);
  clearAutoSaveTimer();
  if (hasPendingChanges.value && hasDraftContent() && !saving.value && !loadingDetail.value) {
    saveLocalDraft();
    void autoSaveDraft();
  }
});
</script>
