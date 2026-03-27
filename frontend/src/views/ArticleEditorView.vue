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
          {{ saving ? "保存中..." : isEdit ? "保存修改" : "发布文章" }}
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
          摘要
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
            状态
            <div class="select-shell">
              <select v-model="form.status" class="select-control">
                <option value="0">草稿</option>
                <option value="1">已发布</option>
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
import { computed, nextTick, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { addArticleApi, detailArticleApi, updateArticleApi } from "../api/article";
import { optionsCategoryApi } from "../api/category";
import { parseMarkdownToHtml } from "../utils/markdown";

const router = useRouter();
const route = useRoute();

const categoryOptions = ref([]);
const saving = ref(false);
const loadingDetail = ref(false);
const errorMsg = ref("");
const textareaRef = ref(null);

const form = reactive({
  title: "",
  summary: "",
  content: "",
  coverUrl: "",
  categoryId: "",
  status: "0",
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

function goBack() {
  router.push("/admin");
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

function validate() {
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

function buildPayload() {
  const payload = {
    title: form.title.trim(),
    summary: form.summary.trim(),
    content: form.content,
    coverUrl: form.coverUrl.trim(),
    categoryId: Number(form.categoryId),
    status: Number(form.status),
    isTop: Number(form.isTop)
  };

  if (isEdit.value) {
    payload.id = articleId.value;
  }

  return payload;
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
    form.title = detail.title || "";
    form.summary = detail.summary || "";
    form.content = detail.content || "";
    form.coverUrl = detail.coverUrl || "";
    form.categoryId = detail.categoryId ? String(detail.categoryId) : "";
    form.status = detail.status != null ? String(detail.status) : "0";
    form.isTop = detail.isTop != null ? String(detail.isTop) : "0";
  } catch (error) {
    errorMsg.value = error.response?.data?.message || error.message || "获取文章详情失败";
  } finally {
    loadingDetail.value = false;
  }
}

async function submitArticle() {
  if (saving.value || loadingDetail.value) {
    return;
  }

  errorMsg.value = validate();
  if (errorMsg.value) {
    return;
  }

  saving.value = true;
  try {
    const payload = buildPayload();
    const res = isEdit.value
      ? await updateArticleApi(payload)
      : await addArticleApi(payload);

    if (res.code !== 200) {
      errorMsg.value = res.message || "提交失败";
      return;
    }

    router.push("/admin");
  } catch (error) {
    errorMsg.value = error.response?.data?.message || error.message || "提交失败";
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  await loadCategoryOptions();
  await loadDetail();
});
</script>
