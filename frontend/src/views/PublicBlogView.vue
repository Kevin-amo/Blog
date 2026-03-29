<template>
  <main class="home-page public-blog-page">
    <header class="topbar public-topbar">
      <div>
        <p class="eyebrow">Wavelog Blog</p>
        <h1>我的博客</h1>
      </div>
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
