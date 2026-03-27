<template>
  <main class="home-page article-home-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">Wavelog Blog</p>
        <h1>文章管理台</h1>
      </div>
      <button class="ghost-btn" @click="handleLogout">退出登录</button>
    </header>

    <section class="hero">
      <h2>{{ greeting }}</h2>
      <p>你好，{{ displayName }}。今天是 {{ today }}，开始管理你的内容吧。</p>
    </section>

    <section class="panel profile-panel">
      <div class="panel-title">当前登录信息</div>
      <div v-if="loadingProfile" class="muted">正在加载用户信息...</div>
      <div v-else class="profile-grid">
        <div>
          <p class="field">用户ID</p>
          <p class="value">{{ user.userId || "-" }}</p>
        </div>
        <div>
          <p class="field">用户名</p>
          <p class="value">{{ user.username || "-" }}</p>
        </div>
      </div>
      <p v-if="profileError" class="error-msg">{{ profileError }}</p>
    </section>

    <section class="panel workspace-switch">
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'article' }"
        type="button"
        @click="activeTab = 'article'"
      >
        文章管理
      </button>
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'category' }"
        type="button"
        @click="activeTab = 'category'"
      >
        分类管理
      </button>
    </section>

    <template v-if="activeTab === 'article'">
      <section class="panel article-filter-panel">
        <div class="panel-title">文章筛选</div>
        <form class="article-filter" @submit.prevent="handleArticleSearch">
          <label>
            标题
            <input v-model.trim="articleFilters.title" type="text" placeholder="输入标题关键字" />
          </label>

          <label>
            状态
            <div class="select-shell">
              <select v-model="articleFilters.status" class="select-control">
                <option value="">全部</option>
                <option value="0">草稿</option>
                <option value="1">已发布</option>
              </select>
            </div>
          </label>

          <label>
            分类
            <div class="select-shell">
              <select v-model="articleFilters.categoryId" class="select-control">
                <option value="">全部</option>
                <option v-for="item in categoryOptions" :key="item.id" :value="String(item.id)">
                  {{ item.name }}
                </option>
              </select>
            </div>
          </label>

          <div class="article-filter-actions">
            <button class="submit-btn" type="submit">查询</button>
            <button class="ghost-btn" type="button" @click="resetArticleFilters">重置</button>
          </div>
        </form>
      </section>

      <section class="panel article-panel">
        <div class="article-panel-head">
          <div>
            <div class="panel-title">我的文章</div>
            <p class="muted">共 {{ articlePager.total }} 篇</p>
          </div>
          <button class="submit-btn" type="button" @click="openCreateArticle">写新文章</button>
        </div>

        <div v-if="loadingArticles" class="muted">正在加载文章列表...</div>
        <p v-else-if="articleError" class="error-msg">{{ articleError }}</p>
        <div v-else-if="!articleRows.length" class="empty-state">暂无文章，点击右上角“写新文章”开始创作。</div>
        <div v-else class="article-table-wrap">
          <table class="article-table">
            <thead>
              <tr>
                <th>标题</th>
                <th>分类</th>
                <th>状态</th>
                <th>置顶</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="article in articleRows" :key="article.id">
                <td>
                  <p class="article-title">{{ article.title }}</p>
                  <p class="article-summary">{{ article.summary || "暂无摘要" }}</p>
                </td>
                <td>{{ article.categoryName || categoryNameMap[article.categoryId] || "-" }}</td>
                <td>
                  <span :class="['status-pill', article.status === 1 ? 'status-published' : 'status-draft']">
                    <i class="status-dot"></i>
                    {{ article.status === 1 ? "已发布" : "草稿" }}
                  </span>
                </td>
                <td>{{ article.isTop === 1 ? "是" : "否" }}</td>
                <td>{{ formatDate(article.createTime) }}</td>
                <td>
                  <div class="row-actions">
                    <button class="link-btn" type="button" @click="openEditArticle(article)">编辑</button>
                    <button class="danger-link-btn" type="button" @click="handleDeleteArticle(article)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination" v-if="articlePager.total > 0">
          <div class="pagination-meta">
            <span>第 {{ articlePager.pageNum }} / {{ articleTotalPages }} 页</span>
            <span>每页</span>
            <div class="select-shell select-shell-mini">
              <select v-model.number="articlePager.pageSize" class="select-control" @change="changeArticlePageSize">
                <option :value="5">5</option>
                <option :value="10">10</option>
                <option :value="20">20</option>
              </select>
            </div>
          </div>
          <div class="pagination-actions">
            <button class="ghost-btn" type="button" :disabled="articlePager.pageNum <= 1" @click="goArticlePage(articlePager.pageNum - 1)">
              上一页
            </button>
            <button class="ghost-btn" type="button" :disabled="articlePager.pageNum >= articleTotalPages" @click="goArticlePage(articlePager.pageNum + 1)">
              下一页
            </button>
          </div>
        </div>
      </section>
    </template>

    <template v-else>
      <section class="panel article-filter-panel">
        <div class="panel-title">分类筛选</div>
        <form class="article-filter" @submit.prevent="handleCategorySearch">
          <label>
            分类名称
            <input v-model.trim="categoryFilters.name" type="text" placeholder="输入分类名称" />
          </label>

          <label>
            状态
            <div class="select-shell">
              <select v-model="categoryFilters.status" class="select-control">
                <option value="">全部</option>
                <option value="1">启用</option>
                <option value="0">禁用</option>
              </select>
            </div>
          </label>

          <div class="article-filter-actions">
            <button class="submit-btn" type="submit">查询</button>
            <button class="ghost-btn" type="button" @click="resetCategoryFilters">重置</button>
          </div>
        </form>
      </section>

      <section class="panel article-panel">
        <div class="article-panel-head">
          <div>
            <div class="panel-title">分类列表</div>
            <p class="muted">共 {{ categoryPager.total }} 条</p>
          </div>
          <button class="submit-btn" type="button" @click="openCreateCategory">新增分类</button>
        </div>

        <div v-if="loadingCategories" class="muted">正在加载分类列表...</div>
        <p v-else-if="categoryError" class="error-msg">{{ categoryError }}</p>
        <div v-else-if="!categoryRows.length" class="empty-state">暂无分类，点击右上角“新增分类”创建。</div>
        <div v-else class="article-table-wrap">
          <table class="article-table category-table">
            <thead>
              <tr>
                <th>名称</th>
                <th>排序</th>
                <th>状态</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="category in categoryRows" :key="category.id">
                <td>{{ category.name }}</td>
                <td>{{ category.sort }}</td>
                <td>
                  <span :class="['status-pill', category.status === 1 ? 'status-published' : 'status-draft']">
                    <i class="status-dot"></i>
                    {{ category.status === 1 ? "启用" : "禁用" }}
                  </span>
                </td>
                <td>{{ formatDate(category.createTime) }}</td>
                <td>
                  <div class="row-actions">
                    <button class="link-btn" type="button" @click="openEditCategory(category)">编辑</button>
                    <button class="danger-link-btn" type="button" @click="handleDeleteCategory(category)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination" v-if="categoryPager.total > 0">
          <div class="pagination-meta">
            <span>第 {{ categoryPager.pageNum }} / {{ categoryTotalPages }} 页</span>
            <span>每页</span>
            <div class="select-shell select-shell-mini">
              <select v-model.number="categoryPager.pageSize" class="select-control" @change="changeCategoryPageSize">
                <option :value="5">5</option>
                <option :value="10">10</option>
                <option :value="20">20</option>
              </select>
            </div>
          </div>
          <div class="pagination-actions">
            <button class="ghost-btn" type="button" :disabled="categoryPager.pageNum <= 1" @click="goCategoryPage(categoryPager.pageNum - 1)">
              上一页
            </button>
            <button class="ghost-btn" type="button" :disabled="categoryPager.pageNum >= categoryTotalPages" @click="goCategoryPage(categoryPager.pageNum + 1)">
              下一页
            </button>
          </div>
        </div>
      </section>
    </template>

    <div v-if="showCategoryEditor" class="modal-mask" @click.self="closeCategoryEditor">
      <section class="modal-card category-editor-modal">
        <div class="modal-head">
          <h3>{{ editingCategoryId ? "编辑分类" : "新增分类" }}</h3>
          <button class="link-btn" type="button" @click="closeCategoryEditor">关闭</button>
        </div>

        <form class="editor-form" @submit.prevent="submitCategory">
          <label>
            分类名称
            <input v-model.trim="categoryEditor.name" type="text" maxlength="30" placeholder="请输入分类名称" />
          </label>

          <div class="editor-grid two-col">
            <label>
              排序值
              <input v-model.number="categoryEditor.sort" type="number" min="0" placeholder="默认 100, 越小排名越靠前" />
            </label>

            <label>
              状态
              <div class="select-shell">
                <select v-model.number="categoryEditor.status" class="select-control">
                  <option :value="1">启用</option>
                  <option :value="0">禁用</option>
                </select>
              </div>
            </label>
          </div>

          <p v-if="categoryEditorError" class="error-msg">{{ categoryEditorError }}</p>

          <div class="editor-actions">
            <button class="ghost-btn" type="button" @click="closeCategoryEditor">取消</button>
            <button class="submit-btn" type="submit" :disabled="savingCategory">
              {{ savingCategory ? "提交中..." : editingCategoryId ? "保存修改" : "创建分类" }}
            </button>
          </div>
        </form>
      </section>
    </div>

    <div v-if="showLogoutConfirm" class="modal-mask" @click.self="closeLogoutConfirm">
      <section class="modal-card logout-modal">
        <div class="modal-head">
          <h3>确认退出登录</h3>
          <button class="link-btn" type="button" :disabled="loggingOut" @click="closeLogoutConfirm">
            关闭
          </button>
        </div>
        <p class="logout-hint">退出后将返回登录页，需要重新输入账号密码。</p>
        <div class="logout-actions">
          <button class="ghost-btn" type="button" :disabled="loggingOut" @click="closeLogoutConfirm">
            取消
          </button>
          <button class="danger-btn" type="button" :disabled="loggingOut" @click="confirmLogout">
            {{ loggingOut ? "退出中..." : "确认退出" }}
          </button>
        </div>
      </section>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { getMyProfileApi, logoutApi } from "../api/auth";
import { deleteArticleApi, pageArticleApi } from "../api/article";
import {
  addCategoryApi,
  deleteCategoryApi,
  detailCategoryApi,
  optionsCategoryApi,
  pageCategoryApi,
  updateCategoryApi
} from "../api/category";
import { clearAuth, getUserInfo } from "../utils/auth";

const router = useRouter();

const activeTab = ref("article");

const localUser = getUserInfo();
const user = ref({
  userId: localUser?.userId || "",
  username: localUser?.username || ""
});

const loadingProfile = ref(false);
const profileError = ref("");

const categoryOptions = ref([]);
const categoryNameMap = computed(() => {
  const map = {};
  for (const item of categoryOptions.value) {
    map[item.id] = item.name;
  }
  return map;
});

const articleFilters = reactive({
  title: "",
  status: "",
  categoryId: ""
});

const articlePager = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

const articleRows = ref([]);
const loadingArticles = ref(false);
const articleError = ref("");

const categoryFilters = reactive({
  name: "",
  status: ""
});

const categoryPager = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

const categoryRows = ref([]);
const loadingCategories = ref(false);
const categoryError = ref("");

const showCategoryEditor = ref(false);
const editingCategoryId = ref(null);
const savingCategory = ref(false);
const categoryEditorError = ref("");

const categoryEditor = reactive({
  name: "",
  sort: 100,
  status: 1
});

const showLogoutConfirm = ref(false);
const loggingOut = ref(false);

const displayName = computed(() => user.value.username || "朋友");
const greeting = computed(() => `欢迎回来，${displayName.value}`);
const today = computed(() => {
  return new Date().toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "long"
  });
});

const articleTotalPages = computed(() => {
  return Math.max(1, Math.ceil(articlePager.total / articlePager.pageSize) || 1);
});

const categoryTotalPages = computed(() => {
  return Math.max(1, Math.ceil(categoryPager.total / categoryPager.pageSize) || 1);
});

function formatDate(raw) {
  if (!raw) {
    return "-";
  }
  return new Date(raw).toLocaleString("zh-CN", {
    hour12: false
  });
}

function normalizePageData(data) {
  return {
    total: Number(data?.total || 0),
    records: Array.isArray(data?.records) ? data.records : []
  };
}

async function loadProfile() {
  loadingProfile.value = true;
  profileError.value = "";

  try {
    const res = await getMyProfileApi();
    if (res.code !== 200) {
      profileError.value = res.message || "获取用户信息失败";
      return;
    }

    user.value = {
      userId: res.data?.userId || "",
      username: res.data?.username || ""
    };
  } catch (error) {
    profileError.value = error.response?.data?.message || "请求失败，请检查后端服务是否已启动";
  } finally {
    loadingProfile.value = false;
  }
}

async function loadCategoryOptions() {
  try {
    const res = await optionsCategoryApi();
    if (res.code !== 200) {
      return;
    }
    categoryOptions.value = Array.isArray(res.data) ? res.data : [];
  } catch {
    categoryOptions.value = [];
  }
}

function buildArticleParams() {
  const params = {
    pageNum: articlePager.pageNum,
    pageSize: articlePager.pageSize
  };

  if (articleFilters.title) {
    params.title = articleFilters.title;
  }
  if (articleFilters.status !== "") {
    params.status = Number(articleFilters.status);
  }
  if (articleFilters.categoryId !== "") {
    params.categoryId = Number(articleFilters.categoryId);
  }
  return params;
}

async function loadArticlesPage() {
  loadingArticles.value = true;
  articleError.value = "";

  try {
    const res = await pageArticleApi(buildArticleParams());
    if (res.code !== 200) {
      articleError.value = res.message || "获取文章分页失败";
      articleRows.value = [];
      articlePager.total = 0;
      return;
    }

    const pageData = normalizePageData(res.data);
    articleRows.value = pageData.records;
    articlePager.total = pageData.total;

    const maxPage = Math.max(1, Math.ceil(pageData.total / articlePager.pageSize) || 1);
    if (articlePager.pageNum > maxPage) {
      articlePager.pageNum = maxPage;
      await loadArticlesPage();
    }
  } catch (error) {
    articleError.value = error.response?.data?.message || error.message || "获取文章分页失败";
  } finally {
    loadingArticles.value = false;
  }
}

function handleArticleSearch() {
  articlePager.pageNum = 1;
  loadArticlesPage();
}

function resetArticleFilters() {
  articleFilters.title = "";
  articleFilters.status = "";
  articleFilters.categoryId = "";
  articlePager.pageNum = 1;
  loadArticlesPage();
}

function goArticlePage(pageNum) {
  if (pageNum < 1 || pageNum > articleTotalPages.value || loadingArticles.value) {
    return;
  }
  articlePager.pageNum = pageNum;
  loadArticlesPage();
}

function changeArticlePageSize() {
  articlePager.pageNum = 1;
  loadArticlesPage();
}

function openCreateArticle() {
  router.push("/admin/articles/new");
}

function openEditArticle(article) {
  router.push(`/admin/articles/${article.id}/edit`);
}

async function handleDeleteArticle(article) {
  const confirmed = window.confirm(`确认删除文章《${article.title}》吗？`);
  if (!confirmed) {
    return;
  }

  try {
    const res = await deleteArticleApi(article.id);
    if (res.code !== 200) {
      window.alert(res.message || "删除失败");
      return;
    }

    if (articleRows.value.length === 1 && articlePager.pageNum > 1) {
      articlePager.pageNum -= 1;
    }
    await loadArticlesPage();
  } catch (error) {
    window.alert(error.response?.data?.message || error.message || "删除失败");
  }
}

function buildCategoryParams() {
  const params = {
    pageNum: categoryPager.pageNum,
    pageSize: categoryPager.pageSize
  };

  if (categoryFilters.name) {
    params.name = categoryFilters.name;
  }
  if (categoryFilters.status !== "") {
    params.status = Number(categoryFilters.status);
  }

  return params;
}

async function loadCategoryPage() {
  loadingCategories.value = true;
  categoryError.value = "";

  try {
    const res = await pageCategoryApi(buildCategoryParams());
    if (res.code !== 200) {
      categoryError.value = res.message || "获取分类分页失败";
      categoryRows.value = [];
      categoryPager.total = 0;
      return;
    }

    const pageData = normalizePageData(res.data);
    categoryRows.value = pageData.records;
    categoryPager.total = pageData.total;

    const maxPage = Math.max(1, Math.ceil(pageData.total / categoryPager.pageSize) || 1);
    if (categoryPager.pageNum > maxPage) {
      categoryPager.pageNum = maxPage;
      await loadCategoryPage();
    }
  } catch (error) {
    categoryError.value = error.response?.data?.message || error.message || "获取分类分页失败";
  } finally {
    loadingCategories.value = false;
  }
}

function handleCategorySearch() {
  categoryPager.pageNum = 1;
  loadCategoryPage();
}

function resetCategoryFilters() {
  categoryFilters.name = "";
  categoryFilters.status = "";
  categoryPager.pageNum = 1;
  loadCategoryPage();
}

function goCategoryPage(pageNum) {
  if (pageNum < 1 || pageNum > categoryTotalPages.value || loadingCategories.value) {
    return;
  }
  categoryPager.pageNum = pageNum;
  loadCategoryPage();
}

function changeCategoryPageSize() {
  categoryPager.pageNum = 1;
  loadCategoryPage();
}

function resetCategoryEditor() {
  categoryEditor.name = "";
  categoryEditor.sort = 100;
  categoryEditor.status = 1;
  categoryEditorError.value = "";
}

function openCreateCategory() {
  editingCategoryId.value = null;
  resetCategoryEditor();
  showCategoryEditor.value = true;
}

async function openEditCategory(category) {
  editingCategoryId.value = category.id;
  resetCategoryEditor();
  showCategoryEditor.value = true;
  savingCategory.value = true;

  try {
    const res = await detailCategoryApi(category.id);
    if (res.code !== 200) {
      categoryEditorError.value = res.message || "获取分类详情失败";
      return;
    }

    const detail = res.data || {};
    categoryEditor.name = detail.name || "";
    categoryEditor.sort = detail.sort ?? 100;
    categoryEditor.status = detail.status ?? 1;
  } catch (error) {
    categoryEditorError.value = error.response?.data?.message || error.message || "获取分类详情失败";
  } finally {
    savingCategory.value = false;
  }
}

function closeCategoryEditor() {
  if (savingCategory.value) {
    return;
  }
  showCategoryEditor.value = false;
}

function validateCategoryEditor() {
  if (!categoryEditor.name) {
    return "请输入分类名称";
  }
  if (categoryEditor.sort == null || Number.isNaN(Number(categoryEditor.sort))) {
    return "请输入有效排序值";
  }
  return "";
}

function buildCategoryPayload() {
  const payload = {
    name: categoryEditor.name,
    sort: Number(categoryEditor.sort),
    status: Number(categoryEditor.status)
  };

  if (editingCategoryId.value) {
    payload.id = editingCategoryId.value;
  }

  return payload;
}

async function submitCategory() {
  categoryEditorError.value = validateCategoryEditor();
  if (categoryEditorError.value) {
    return;
  }

  savingCategory.value = true;
  try {
    const payload = buildCategoryPayload();
    const res = editingCategoryId.value
      ? await updateCategoryApi(payload)
      : await addCategoryApi(payload);

    if (res.code !== 200) {
      categoryEditorError.value = res.message || "提交失败";
      return;
    }

    showCategoryEditor.value = false;
    categoryPager.pageNum = 1;
    await Promise.all([loadCategoryPage(), loadCategoryOptions(), loadArticlesPage()]);
  } catch (error) {
    categoryEditorError.value = error.response?.data?.message || error.message || "提交失败";
  } finally {
    savingCategory.value = false;
  }
}

async function handleDeleteCategory(category) {
  const confirmed = window.confirm(`确认删除分类“${category.name}”吗？`);
  if (!confirmed) {
    return;
  }

  try {
    const res = await deleteCategoryApi(category.id);
    if (res.code !== 200) {
      window.alert(res.message || "删除失败");
      return;
    }

    if (categoryRows.value.length === 1 && categoryPager.pageNum > 1) {
      categoryPager.pageNum -= 1;
    }

    await Promise.all([loadCategoryPage(), loadCategoryOptions(), loadArticlesPage()]);
  } catch (error) {
    window.alert(error.response?.data?.message || error.message || "删除失败");
  }
}

function handleLogout() {
  showLogoutConfirm.value = true;
}

function closeLogoutConfirm() {
  if (loggingOut.value) {
    return;
  }
  showLogoutConfirm.value = false;
}

async function confirmLogout() {
  if (loggingOut.value) {
    return;
  }

  loggingOut.value = true;
  try {
    await logoutApi();
  } catch {
    // 后端无状态登出，忽略异常
  } finally {
    clearAuth();
    showLogoutConfirm.value = false;
    loggingOut.value = false;
    router.push("/login");
  }
}

onMounted(async () => {
  await Promise.all([loadProfile(), loadCategoryOptions(), loadArticlesPage(), loadCategoryPage()]);
});
</script>
