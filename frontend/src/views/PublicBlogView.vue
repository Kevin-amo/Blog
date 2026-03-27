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
      <p>这里展示已发布文章的预览，点击卡片会在新窗口打开完整内容。</p>
    </section>

    <section class="panel">
      <div class="panel-title">最新发布</div>
      <div v-if="loading" class="muted">正在加载文章...</div>
      <p v-else-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
      <div v-else-if="!articles.length" class="empty-state">还没有已发布文章。</div>

      <div v-else class="public-grid">
        <article
          v-for="item in articles"
          :key="item.id"
          class="public-card"
          role="button"
          tabindex="0"
          @click="openArticle(item.id)"
          @keydown.enter="openArticle(item.id)"
        >
          <h3>{{ item.title }}</h3>
          <p class="public-item-summary">{{ item.summary || brief(item.content) }}</p>
          <div class="public-card-foot">
            <span class="public-item-time">{{ formatDate(item.createTime) }}</span>
            <button class="submit-btn preview-btn" type="button" @click.stop="openArticle(item.id)">阅读全文</button>
          </div>
        </article>
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { publicListArticleApi } from "../api/article";
import { extractPlainText } from "../utils/markdown";

const router = useRouter();
const articles = ref([]);
const loading = ref(false);
const errorMsg = ref("");

function formatDate(raw) {
  if (!raw) {
    return "-";
  }
  return new Date(raw).toLocaleString("zh-CN", { hour12: false });
}

function brief(content) {
  const plain = extractPlainText(content);
  if (!plain) {
    return "暂无摘要";
  }
  return plain.length > 120 ? `${plain.slice(0, 120)}...` : plain;
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
  } catch (error) {
    errorMsg.value = error.response?.data?.message || error.message || "获取文章列表失败";
  } finally {
    loading.value = false;
  }
}

function openArticle(id) {
  const href = router.resolve({ name: "public-blog-detail", params: { id } }).href;
  window.open(href, "_blank", "noopener");
}

onMounted(() => {
  loadList();
});
</script>
