<template>
  <main class="home-page public-blog-page site-shell">
    <header class="blog-navbar panel">
      <div class="blog-navbar-left">
        <button class="blog-logo" type="button" @click="goHome">WAVELOG BLOG</button>
        <nav class="blog-nav" aria-label="博客主导航">
          <button class="blog-nav-link" :class="{ active: activeNav === 'home' }" type="button" @click="goHome">
            首页
          </button>
          <div ref="categoryDropdownRef" class="blog-nav-dropdown" @mouseenter="openCategoryDropdown">
            <button
              class="blog-nav-link"
              :class="{ active: activeNav === 'category' || categoryDropdownOpen }"
              type="button"
              :aria-expanded="categoryDropdownOpen"
              @click="toggleCategoryDropdown"
            >
              分类
            </button>
            <transition name="dropdown-fade">
              <div
                v-if="categoryDropdownOpen"
                class="blog-nav-dropdown-menu"
                role="menu"
                aria-label="分类筛选"
              >
                <p v-if="categoryLoading" class="blog-nav-dropdown-state muted">正在加载分类...</p>
                <p v-else-if="categoryErrorMsg" class="blog-nav-dropdown-state error-msg">{{ categoryErrorMsg }}</p>
                <template v-else>
                  <button
                    class="blog-nav-dropdown-item"
                    :class="{ active: selectedCategoryId === '' }"
                    type="button"
                    @click="selectCategory('')"
                  >
                    全部分类
                  </button>
                  <button
                    v-for="item in categories"
                    :key="item.id"
                    class="blog-nav-dropdown-item"
                    :class="{ active: selectedCategoryId === String(item.id) }"
                    type="button"
                    @click="selectCategory(String(item.id))"
                  >
                    {{ item.name }}
                  </button>
                </template>
              </div>
            </transition>
          </div>
          <button class="blog-nav-link" :class="{ active: activeNav === 'about' }" type="button" @click="goAbout">
            关于我
          </button>
        </nav>
      </div>

      <form class="blog-search" @submit.prevent="handleSearch">
        <input v-model.trim="searchKeyword" type="text" maxlength="50" placeholder="搜索文章标题" />
        <button class="submit-btn" type="submit">搜索</button>
      </form>
    </header>

    <section v-if="currentView === 'about'" class="panel public-about-panel">
      <div class="section-head">
        <div>
          <p class="panel-title">关于我</p>
          <p class="muted">一个简洁的个人博客，用来记录技术、思考和日常输出。</p>
        </div>
      </div>
      <div class="about-copy">
        <p>这里主要发布技术文章、学习笔记和阶段性总结。</p>
        <p>博客保持内容优先，支持按分类浏览和按标题搜索。</p>
      </div>
    </section>

    <section v-else class="content-layout public-blog-layout-full">
      <section class="content-main">
        <section class="panel list-panel">
          <div class="section-head blog-list-head">
            <div>
              <p class="panel-title">{{ listTitle }}</p>
              <p class="muted">共 {{ pager.total }} 篇文章，每页 5 篇</p>
            </div>
            <button
              v-if="selectedCategoryId !== ''"
              class="ghost-btn"
              type="button"
              @click="resetCategoryFilter"
            >
              查看全部
            </button>
          </div>

          <div v-if="loading" class="muted">正在加载文章...</div>
          <p v-else-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
          <div v-else-if="!articles.length" class="empty-state">没有匹配的已发布文章。</div>

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
                  <span v-if="categoryNameMap[item.categoryId]">分类：{{ categoryNameMap[item.categoryId] }}</span>
                  <span>{{ formatDate(item.createTime) }}</span>
                </div>
              </div>
              <div class="article-list-side">
                <button class="ghost-btn preview-btn" type="button" @click.stop="openArticle(item.id)">阅读全文</button>
              </div>
            </article>
          </div>

          <div class="pagination" v-if="pager.total > 0">
            <div class="pagination-meta">
              <span>第 {{ pager.pageNum }} / {{ totalPages }} 页</span>
              <span>共 {{ pager.total }} 条</span>
            </div>
            <div class="pagination-actions">
              <button class="ghost-btn" type="button" :disabled="pager.pageNum <= 1 || loading" @click="goPage(pager.pageNum - 1)">
                上一页
              </button>
              <button class="ghost-btn" type="button" :disabled="pager.pageNum >= totalPages || loading" @click="goPage(pager.pageNum + 1)">
                下一页
              </button>
            </div>
          </div>
        </section>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { publicPageArticleApi } from "../api/article";
import { optionsCategoryApi } from "../api/category";
import { extractPlainText } from "../utils/markdown";

const router = useRouter();
const articles = ref([]);
const categories = ref([]);
const loading = ref(false);
const categoryLoading = ref(false);
const errorMsg = ref("");
const categoryErrorMsg = ref("");
const currentView = ref("home");
const searchKeyword = ref("");
const selectedCategoryId = ref("");
const categoryDropdownOpen = ref(false);
const categoryDropdownRef = ref(null);
const pager = ref({
  pageNum: 1,
  pageSize: 5,
  total: 0
});

const activeNav = computed(() => {
  if (currentView.value === "about") {
    return "about";
  }
  if (selectedCategoryId.value) {
    return "category";
  }
  return "home";
});
const categoryNameMap = computed(() => {
  const map = {};
  for (const item of categories.value) {
    map[item.id] = item.name;
  }
  return map;
});
const currentCategoryName = computed(() => {
  if (!selectedCategoryId.value) {
    return "";
  }
  return categoryNameMap.value[Number(selectedCategoryId.value)] || "当前分类";
});
const listTitle = computed(() => {
  if (currentCategoryName.value) {
    return `${currentCategoryName.value} · 分类文章`;
  }
  if (searchKeyword.value) {
    return `搜索结果：${searchKeyword.value}`;
  }
  return "最新发布";
});
const totalPages = computed(() => Math.max(1, Math.ceil(pager.value.total / pager.value.pageSize) || 1));

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

function buildParams() {
  const params = {
    pageNum: pager.value.pageNum,
    pageSize: pager.value.pageSize
  };
  if (searchKeyword.value) {
    params.title = searchKeyword.value;
  }
  if (selectedCategoryId.value) {
    params.categoryId = Number(selectedCategoryId.value);
  }
  return params;
}

async function loadCategories() {
  categoryLoading.value = true;
  categoryErrorMsg.value = "";
  try {
    const res = await optionsCategoryApi();
    if (res.code !== 200) {
      categoryErrorMsg.value = res.message || "获取分类失败";
      categories.value = [];
      return;
    }
    categories.value = Array.isArray(res.data) ? res.data : [];
  } catch (error) {
    categoryErrorMsg.value = error.response?.data?.message || error.message || "获取分类失败";
    categories.value = [];
  } finally {
    categoryLoading.value = false;
  }
}

async function loadArticles() {
  loading.value = true;
  errorMsg.value = "";
  try {
    const res = await publicPageArticleApi(buildParams());
    if (res.code !== 200) {
      errorMsg.value = res.message || "获取文章列表失败";
      articles.value = [];
      pager.value.total = 0;
      return;
    }
    const pageData = res.data || {};
    articles.value = Array.isArray(pageData.records) ? pageData.records : [];
    pager.value.total = Number(pageData.total) || 0;
    const maxPage = Math.max(1, Math.ceil(pager.value.total / pager.value.pageSize) || 1);
    if (pager.value.pageNum > maxPage) {
      pager.value.pageNum = maxPage;
      await loadArticles();
    }
  } catch (error) {
    errorMsg.value = error.response?.data?.message || error.message || "获取文章列表失败";
    articles.value = [];
    pager.value.total = 0;
  } finally {
    loading.value = false;
  }
}

function closeCategoryDropdown() {
  categoryDropdownOpen.value = false;
}

function openCategoryDropdown() {
  currentView.value = "home";
  categoryDropdownOpen.value = true;
}

function toggleCategoryDropdown() {
  currentView.value = "home";
  categoryDropdownOpen.value = !categoryDropdownOpen.value;
}

function handleDocumentClick(event) {
  if (!categoryDropdownRef.value?.contains(event.target)) {
    closeCategoryDropdown();
  }
}

function goHome() {
  currentView.value = "home";
  selectedCategoryId.value = "";
  pager.value.pageNum = 1;
  closeCategoryDropdown();
  loadArticles();
}

function goCategory() {
  toggleCategoryDropdown();
}

function goAbout() {
  currentView.value = "about";
  closeCategoryDropdown();
}

function handleSearch() {
  currentView.value = "home";
  pager.value.pageNum = 1;
  closeCategoryDropdown();
  loadArticles();
}

function selectCategory(categoryId) {
  currentView.value = "home";
  selectedCategoryId.value = categoryId;
  pager.value.pageNum = 1;
  closeCategoryDropdown();
  loadArticles();
}

function resetCategoryFilter() {
  currentView.value = "home";
  selectedCategoryId.value = "";
  pager.value.pageNum = 1;
  closeCategoryDropdown();
  loadArticles();
}

function goPage(pageNum) {
  if (pageNum < 1 || pageNum > totalPages.value || loading.value) {
    return;
  }
  pager.value.pageNum = pageNum;
  loadArticles();
}

function openArticle(id) {
  closeCategoryDropdown();
  router.push({ name: "public-blog-detail", params: { id } });
}

onMounted(async () => {
  document.addEventListener("click", handleDocumentClick);
  await Promise.all([loadCategories(), loadArticles()]);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleDocumentClick);
});
</script>