<template>
  <main class="home-page public-blog-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">Wavelog Blog</p>
        <h1>我的博客</h1>
      </div>
    </header>

    <section class="hero">
      <h2>欢迎来到我的文字角落</h2>
      <p>这里展示已发布的文章，游客无需登录即可阅读。</p>
    </section>

    <section class="panel public-layout">
      <aside class="public-list">
        <div class="panel-title">最新发布</div>
        <div v-if="loading" class="muted">正在加载文章...</div>
        <p v-else-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
        <div v-else-if="!articles.length" class="empty-state">还没有已发布文章。</div>
        <button
          v-else
          v-for="item in articles"
          :key="item.id"
          type="button"
          class="public-item"
          :class="{ active: item.id === activeId }"
          @click="pickArticle(item.id)"
        >
          <p class="public-item-title">{{ item.title }}</p>
          <p class="public-item-summary">{{ item.summary || brief(item.content) }}</p>
          <span class="public-item-time">{{ formatDate(item.createTime) }}</span>
        </button>
      </aside>

      <article class="public-detail">
        <div v-if="loadingDetail" class="muted">正在加载正文...</div>
        <p v-else-if="detailError" class="error-msg">{{ detailError }}</p>
        <div v-else-if="currentArticle">
          <h2>{{ currentArticle.title }}</h2>
          <p class="public-meta">
            发布于 {{ formatDate(currentArticle.createTime) }}
            <span v-if="currentArticle.updateTime"> · 更新于 {{ formatDate(currentArticle.updateTime) }}</span>
          </p>
          <div class="public-content">{{ currentArticle.content || "暂无正文" }}</div>
        </div>
        <div v-else class="empty-state">请选择一篇文章开始阅读。</div>
      </article>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { publicDetailArticleApi, publicListArticleApi } from "../api/article";

const articles = ref([]);
const activeId = ref(null);
const currentArticle = ref(null);

const loading = ref(false);
const loadingDetail = ref(false);
const errorMsg = ref("");
const detailError = ref("");

function formatDate(raw) {
  if (!raw) {
    return "-";
  }
  return new Date(raw).toLocaleString("zh-CN", { hour12: false });
}

function brief(content) {
  if (!content) {
    return "暂无摘要";
  }
  return content.length > 70 ? `${content.slice(0, 70)}...` : content;
}

async function loadList() {
  loading.value = true;
  errorMsg.value = "";

  try {
    const res = await publicListArticleApi();
    if (res.code !== 200) {
      errorMsg.value = res.message || "获取文章列表失败";
      return;
    }

    articles.value = Array.isArray(res.data) ? res.data : [];
    if (articles.value.length > 0) {
      await pickArticle(articles.value[0].id);
    }
  } catch (error) {
    errorMsg.value = error.response?.data?.message || error.message || "获取文章列表失败";
  } finally {
    loading.value = false;
  }
}

async function pickArticle(id) {
  if (!id) {
    return;
  }

  activeId.value = id;
  loadingDetail.value = true;
  detailError.value = "";

  try {
    const res = await publicDetailArticleApi(id);
    if (res.code !== 200) {
      detailError.value = res.message || "获取文章详情失败";
      return;
    }

    currentArticle.value = res.data || null;
  } catch (error) {
    detailError.value = error.response?.data?.message || error.message || "获取文章详情失败";
    currentArticle.value = null;
  } finally {
    loadingDetail.value = false;
  }
}

onMounted(() => {
  loadList();
});
</script>