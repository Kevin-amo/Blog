<template>
  <main class="home-page public-blog-page site-shell">
    <header class="site-header">
      <div class="site-brand">
        <p class="eyebrow">Wavelog Blog</p>
        <h1>博客</h1>
        <p class="site-description">记录文章、技术笔记和日常输出，按时间顺序浏览已发布内容。</p>
      </div>
      <div class="site-header-actions">
        <div class="admin-user-menu" @click.stop>
          <button class="admin-avatar-btn" type="button" :title="avatarTitle" @click="toggleUserMenu">
            <img v-if="isLoggedIn && user.avatar" :src="user.avatar" alt="用户头像" />
            <span v-else class="admin-avatar-fallback">{{ avatarFallback }}</span>
          </button>
          <div v-if="isLoggedIn && showUserMenu" class="admin-user-dropdown">
            <button type="button" @click="goToDashboard">进入后台</button>
            <button type="button" @click="handleLogout">退出登录</button>
          </div>
        </div>
      </div>
    </header>

    <section class="content-layout content-layout-home">
      <aside class="content-sidebar site-sidebar">
        <section class="panel sidebar-panel">
          <p class="sidebar-title">站点说明</p>
          <p class="sidebar-copy">整体改成更偏内容优先的博客布局，减少装饰，优先保证阅读、检索和进入后台操作的效率。</p>
        </section>
        <section class="panel sidebar-panel">
          <p class="sidebar-title">文章统计</p>
          <div class="sidebar-stats">
            <div>
              <span class="sidebar-stat-value">{{ articles.length }}</span>
              <span class="sidebar-stat-label">已发布</span>
            </div>
            <div>
              <span class="sidebar-stat-value">{{ isLoggedIn ? '已登录' : '游客' }}</span>
              <span class="sidebar-stat-label">当前状态</span>
            </div>
          </div>
        </section>
      </aside>

      <section class="content-main">
        <section class="panel list-panel">
          <div class="section-head">
            <div>
              <p class="panel-title">最新发布</p>
              <p class="muted">共 {{ articles.length }} 篇文章</p>
            </div>
          </div>

          <div v-if="loading" class="muted">正在加载文章...</div>
          <p v-else-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
          <div v-else-if="!articles.length" class="empty-state">还没有已发布文章。</div>

          <div v-else class="article-feed">
            <article
              v-for="item in articles"
              :key="item.id"
              class="article-list-item"
              role="button"
              tabindex="0"
              @click="openArticle(item.id)"
              @keydown.enter="openArticle(item.id)"
            >
              <div class="article-list-main">
                <h3>{{ item.title }}</h3>
                <p class="article-list-summary">{{ item.summary || brief(item.content) }}</p>
                <div class="article-list-meta">
                  <span>{{ formatDate(item.createTime) }}</span>
                </div>
              </div>
              <div class="article-list-side">
                <button class="ghost-btn preview-btn" type="button" @click.stop="openArticle(item.id)">阅读全文</button>
              </div>
            </article>
          </div>
        </section>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import { logoutApi } from "../api/auth";
import { publicListArticleApi } from "../api/article";
import { clearAuth, getCurrentHomePath, getToken, getUserInfo } from "../utils/auth";
import { extractPlainText } from "../utils/markdown";

const router = useRouter();
const articles = ref([]);
const loading = ref(false);
const errorMsg = ref("");
const showUserMenu = ref(false);

const cachedUser = getUserInfo();
const user = ref({
  username: cachedUser?.username || "",
  nickname: cachedUser?.nickname || "",
  avatar: cachedUser?.avatar || ""
});
const isLoggedIn = ref(Boolean(getToken() && cachedUser));
const displayName = computed(() => user.value.nickname || user.value.username || "已登录用户");
const avatarTitle = computed(() => (isLoggedIn.value ? displayName.value : "点击登录"));
const avatarFallback = computed(() => {
  const source = isLoggedIn.value ? user.value.nickname || user.value.username || "U" : "登";
  return source.slice(0, 1).toUpperCase();
});

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

function closeUserMenu() {
  showUserMenu.value = false;
}

function goToLogin() {
  closeUserMenu();
  router.push({
    path: "/login",
    query: {
      redirect: router.resolve({ name: "public-blog" }).fullPath
    }
  });
}

function toggleUserMenu() {
  if (!isLoggedIn.value) {
    goToLogin();
    return;
  }
  showUserMenu.value = !showUserMenu.value;
}

function goToDashboard() {
  closeUserMenu();
  router.push(getCurrentHomePath());
}

async function handleLogout() {
  closeUserMenu();
  try {
    await logoutApi();
  } catch {
    // 后端无状态登出，忽略异常
  } finally {
    clearAuth();
    isLoggedIn.value = false;
    user.value = {
      username: "",
      nickname: "",
      avatar: ""
    };
  }
}

onMounted(() => {
  document.addEventListener("click", closeUserMenu);
  loadList();
});

onUnmounted(() => {
  document.removeEventListener("click", closeUserMenu);
});
</script>
