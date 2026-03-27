<template>
  <main class="home-page public-detail-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">Wavelog Blog</p>
        <h1>博客全文</h1>
      </div>
      <a class="ghost-btn" href="/">返回首页</a>
    </header>

    <section class="panel">
      <div v-if="loading" class="muted">正在加载正文...</div>
      <p v-else-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

      <article v-else-if="article" class="public-article-detail">
        <h2>{{ article.title }}</h2>
        <p class="public-meta">
          发布时间：{{ formatDate(article.createTime) }}
          <span v-if="article.updateTime"> · 更新时间：{{ formatDate(article.updateTime) }}</span>
        </p>
        <div class="markdown-body" v-html="detailHtml"></div>
      </article>

      <div v-else class="empty-state">文章不存在或暂未发布。</div>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { publicDetailArticleApi } from "../api/article";
import { parseMarkdownToHtml } from "../utils/markdown";

const route = useRoute();
const article = ref(null);
const loading = ref(false);
const errorMsg = ref("");

const articleId = computed(() => {
  const raw = Number(route.params.id);
  return Number.isFinite(raw) && raw > 0 ? raw : null;
});

const detailHtml = computed(() => {
  if (!article.value?.content) {
    return "<p class='muted'>暂无正文</p>";
  }
  return parseMarkdownToHtml(article.value.content);
});

function formatDate(raw) {
  if (!raw) {
    return "-";
  }
  return new Date(raw).toLocaleString("zh-CN", { hour12: false });
}

async function loadDetail() {
  if (!articleId.value) {
    errorMsg.value = "文章参数不合法";
    return;
  }

  loading.value = true;
  errorMsg.value = "";

  try {
    const res = await publicDetailArticleApi(articleId.value);
    if (res.code !== 200) {
      errorMsg.value = res.message || "获取文章详情失败";
      article.value = null;
      return;
    }
    article.value = res.data || null;
  } catch (error) {
    errorMsg.value = error.response?.data?.message || error.message || "获取文章详情失败";
    article.value = null;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadDetail();
});
</script>
