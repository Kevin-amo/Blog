<template>
  <main class="home-page article-home-page">
    <header class="topbar dashboard-topbar">
      <div>
        <p class="eyebrow">Wavelog Blog</p>
        <h1>用户工作台</h1>
      </div>
      <div class="admin-user-menu" @click.stop>
        <button class="admin-avatar-btn" type="button" :title="displayName" @click="toggleUserMenu">
          <img v-if="user.avatar" :src="user.avatar" alt="用户头像" />
          <span v-else class="admin-avatar-fallback">{{ avatarFallback }}</span>
        </button>
        <div v-if="showUserMenu" class="admin-user-dropdown">
          <button type="button" @click="openProfileEditor">个人资料</button>
          <button type="button" @click="handleLogout">退出登录</button>
        </div>
      </div>
    </header>

    <section class="hero hero-plain">
      <h2>{{ greeting }}</h2>
      <p>在这里管理文章、查看审核状态和维护个人资料。</p>
    </section>

    <section class="panel article-filter-panel">
      <div class="panel-title">文章筛选</div>
      <form class="article-filter" @submit.prevent="handleArticleSearch">
        <label>
          标题
          <input v-model.trim="articleFilters.title" type="text" placeholder="输入标题关键字" />
        </label>
        <label>
          发布状态
          <div class="select-shell">
            <select v-model="articleFilters.status" class="select-control">
              <option value="">全部</option>
              <option value="0">草稿</option>
              <option value="1">已提交发布</option>
            </select>
          </div>
        </label>
        <label>
          审核状态
          <div class="select-shell">
            <select v-model="articleFilters.auditStatus" class="select-control">
              <option value="">全部</option>
              <option value="0">待审核</option>
              <option value="1">审核通过</option>
              <option value="2">审核驳回</option>
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
              <th>发布状态</th>
              <th>审核状态</th>
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
              <td>{{ article.status === 1 ? "已提交发布" : "草稿" }}</td>
              <td><span v-html="getAuditLabel(article.auditStatus)"></span></td>
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

    <Teleport to="body">
      <div v-if="showProfileEditor" class="modal-mask" @click.self="closeProfileEditor">
        <section class="modal-card admin-profile-modal">
          <header class="modal-head">
            <h3>个人资料</h3>
            <button class="ghost-btn" type="button" @click="closeProfileEditor">关闭</button>
          </header>

          <div v-if="loadingProfile" class="muted">正在加载用户信息...</div>
          <div v-else class="admin-profile-layout">
            <div class="avatar-panel admin-avatar-panel">
              <button
                class="avatar-preview avatar-preview-picker admin-avatar-preview"
                type="button"
                :disabled="uploadingAvatar"
                @click="triggerAvatarPicker"
              >
                <img v-if="avatarPreviewSrc" :src="avatarPreviewSrc" alt="用户头像" />
                <div v-else class="avatar-fallback">{{ avatarFallback }}</div>
              </button>

              <div class="avatar-actions">
                <input
                  ref="avatarInputRef"
                  class="avatar-file-input"
                  type="file"
                  accept="image/jpeg,image/png"
                  @change="handleAvatarFileChange"
                />
                <span class="avatar-file-name avatar-file-name-alone">{{ selectedAvatarFileName }}</span>
                <button class="submit-btn avatar-upload-btn" type="button" :disabled="uploadingAvatar" @click="submitAvatar">
                  {{ uploadingAvatar ? "上传中..." : "上传头像" }}
                </button>
              </div>
              <p class="muted avatar-tips">仅支持 jpg/jpeg/png，大小不超过 5MB</p>
              <p v-if="avatarError" class="error-msg">{{ avatarError }}</p>
              <p v-if="profileError" class="error-msg">{{ profileError }}</p>
            </div>

            <div class="admin-profile-main">
              <div class="admin-profile-meta">
                <p class="field">用户名</p>
                <p class="value">{{ user.username || "-" }}</p>
              </div>

              <form class="admin-profile-form" @submit.prevent="submitNickname">
                <h4>修改昵称</h4>
                <label>
                  昵称
                  <input v-model.trim="nicknameForm.nickname" type="text" maxlength="50" placeholder="请输入新昵称" />
                </label>
                <p v-if="profileUpdateError" class="error-msg">{{ profileUpdateError }}</p>
                <p v-if="profileUpdateSuccess" class="muted">{{ profileUpdateSuccess }}</p>
                <div class="article-filter-actions">
                  <button class="submit-btn" type="submit" :disabled="savingProfile">保存资料</button>
                </div>
              </form>

              <form class="admin-profile-form" @submit.prevent="submitPassword">
                <h4>修改密码</h4>
                <label>
                  旧密码
                  <input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" />
                </label>
                <label>
                  新密码
                  <input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
                </label>
                <label>
                  确认新密码
                  <input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
                </label>
                <p v-if="passwordError" class="error-msg">{{ passwordError }}</p>
                <p v-if="passwordSuccess" class="muted">{{ passwordSuccess }}</p>
                <div class="article-filter-actions">
                  <button class="submit-btn" type="submit" :disabled="savingPassword">修改密码</button>
                </div>
              </form>
            </div>
          </div>
        </section>
      </div>
    </Teleport>
  </main>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { getMyProfileApi, logoutApi, uploadAvatarApi } from "../api/auth";
import { deleteArticleApi, pageArticleApi } from "../api/article";
import { optionsCategoryApi } from "../api/category";
import { updateMyPasswordApi, updateMyProfileApi } from "../api/user";
import {clearAuth, clearToken, getUserInfo, setUserInfo} from "../utils/auth";

const router = useRouter();

const localUser = getUserInfo();
const user = ref({
  userId: localUser?.userId || "",
  username: localUser?.username || "",
  nickname: localUser?.nickname || "",
  avatar: localUser?.avatar || "",
  role: localUser?.role ?? 0
});

const loadingProfile = ref(false);
const profileError = ref("");
const showProfileEditor = ref(false);
const showUserMenu = ref(false);
const uploadingAvatar = ref(false);
const avatarError = ref("");
const selectedAvatarFile = ref(null);
const selectedAvatarPreviewUrl = ref("");
const avatarInputRef = ref(null);

const savingProfile = ref(false);
const profileUpdateError = ref("");
const profileUpdateSuccess = ref("");
const nicknameForm = reactive({
  nickname: user.value.nickname || ""
});

const savingPassword = ref(false);
const passwordError = ref("");
const passwordSuccess = ref("");
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

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
  auditStatus: "",
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

const displayName = computed(() => user.value.nickname || user.value.username || "朋友");
const greeting = computed(() => `欢迎回来，${displayName.value}`);
const avatarFallback = computed(() => {
  const source = user.value.nickname || user.value.username || "U";
  return source.slice(0, 1).toUpperCase();
});
const selectedAvatarFileName = computed(() => selectedAvatarFile.value?.name || "未选择文件");
const avatarPreviewSrc = computed(() => selectedAvatarPreviewUrl.value || user.value.avatar || "");
const articleTotalPages = computed(() => Math.max(1, Math.ceil(articlePager.total / articlePager.pageSize) || 1));

function getAuditLabel(auditStatus) {
  const status = Number(auditStatus);
  if (status === 1) {
    return '<span class="audit-badge audit-pass"><span class="audit-dot"></span>审核通过</span>';
  }
  if (status === 2) {
    return '<span class="audit-badge audit-reject"><span class="audit-dot"></span>审核驳回</span>';
  }
  return '<span class="audit-badge audit-pending"><span class="audit-dot"></span>待审核</span>';
}

function formatDate(raw) {
  if (!raw) {
    return "-";
  }
  return new Date(raw).toLocaleString("zh-CN", { hour12: false });
}

function normalizePageData(data) {
  return {
    total: Number(data?.total || 0),
    records: Array.isArray(data?.records) ? data.records : []
  };
}

function withAvatarCacheBust(url) {
  if (!url) {
    return "";
  }
  const hashIndex = url.indexOf("#");
  const hash = hashIndex >= 0 ? url.slice(hashIndex) : "";
  const base = hashIndex >= 0 ? url.slice(0, hashIndex) : url;
  const separator = base.includes("?") ? "&" : "?";
  return `${base}${separator}_t=${Date.now()}${hash}`;
}

function clearSelectedAvatarPreview() {
  if (!selectedAvatarPreviewUrl.value) {
    return;
  }
  URL.revokeObjectURL(selectedAvatarPreviewUrl.value);
  selectedAvatarPreviewUrl.value = "";
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value;
}

function closeUserMenu() {
  showUserMenu.value = false;
}

function openProfileEditor() {
  closeUserMenu();
  showProfileEditor.value = true;
  profileError.value = "";
  avatarError.value = "";
  profileUpdateError.value = "";
  passwordError.value = "";
}

function closeProfileEditor() {
  if (uploadingAvatar.value || savingProfile.value || savingPassword.value) {
    return;
  }
  showProfileEditor.value = false;
  resetAvatarInput();
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
      username: res.data?.username || "",
      nickname: res.data?.nickname || "",
      avatar: withAvatarCacheBust(res.data?.avatar || ""),
      role: Number(res.data?.role ?? 0)
    };
    nicknameForm.nickname = user.value.nickname;
    setUserInfo(user.value);
  } catch (error) {
    profileError.value = error.response?.data?.message || error.message || "获取用户信息失败";
  } finally {
    loadingProfile.value = false;
  }
}

function triggerAvatarPicker() {
  if (uploadingAvatar.value) {
    return;
  }
  avatarInputRef.value?.click();
}

function handleAvatarFileChange(event) {
  const file = event.target?.files?.[0] || null;
  avatarError.value = validateSelectedAvatar(file);
  clearSelectedAvatarPreview();
  if (avatarError.value || !file) {
    selectedAvatarFile.value = null;
    return;
  }
  selectedAvatarFile.value = file;
  selectedAvatarPreviewUrl.value = URL.createObjectURL(file);
}

function resetAvatarInput() {
  clearSelectedAvatarPreview();
  selectedAvatarFile.value = null;
  if (avatarInputRef.value) {
    avatarInputRef.value.value = "";
  }
}

function validateSelectedAvatar(file) {
  if (!file) {
    return "请先选择头像文件";
  }
  const acceptedTypes = new Set(["image/jpeg", "image/jpg", "image/png"]);
  if (!acceptedTypes.has((file.type || "").toLowerCase())) {
    return "仅支持 jpg/jpeg/png 格式";
  }
  const maxSize = 5 * 1024 * 1024;
  if (file.size > maxSize) {
    return "头像大小不能超过5MB";
  }
  return "";
}

async function submitAvatar() {
  if (uploadingAvatar.value) {
    return;
  }
  avatarError.value = validateSelectedAvatar(selectedAvatarFile.value);
  if (avatarError.value) {
    return;
  }
  const formData = new FormData();
  formData.append("file", selectedAvatarFile.value);
  uploadingAvatar.value = true;
  try {
    const res = await uploadAvatarApi(formData);
    if (res.code !== 200) {
      avatarError.value = res.message || "头像上传失败";
      return;
    }
    const uploadedAvatar = res.data?.avatarUrl || "";
    if (uploadedAvatar) {
      user.value = {
        ...user.value,
        avatar: withAvatarCacheBust(uploadedAvatar)
      };
      setUserInfo(user.value);
    }
    resetAvatarInput();
    if (!uploadedAvatar) {
      await loadProfile();
    }
  } catch (error) {
    avatarError.value = error.response?.data?.message || error.message || "头像上传失败";
  } finally {
    uploadingAvatar.value = false;
  }
}

async function submitNickname() {
  profileUpdateError.value = "";
  profileUpdateSuccess.value = "";
  if (!nicknameForm.nickname) {
    profileUpdateError.value = "请输入昵称";
    return;
  }
  savingProfile.value = true;
  try {
    const res = await updateMyProfileApi({ nickname: nicknameForm.nickname });
    if (res.code !== 200) {
      profileUpdateError.value = res.message || "修改昵称失败";
      return;
    }
    profileUpdateSuccess.value = "昵称已更新";
    await loadProfile();
  } catch (error) {
    profileUpdateError.value = error.response?.data?.message || error.message || "修改昵称失败";
  } finally {
    savingProfile.value = false;
  }
}

async function submitPassword() {
  passwordError.value = "";
  passwordSuccess.value = "";
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    passwordError.value = "请完整填写密码信息";
    return;
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordError.value = "两次新密码输入不一致";
    return;
  }
  savingPassword.value = true;
  try {
    const res = await updateMyPasswordApi({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    });
    if (res.code !== 200) {
      passwordError.value = res.message || "修改密码失败";
      return;
    }
    passwordSuccess.value = "密码已更新，正在返回登录页...";
    passwordForm.oldPassword = "";
    passwordForm.newPassword = "";
    passwordForm.confirmPassword = "";
    setTimeout(() => {
      window.location.href = '/login'
      clearToken()
    }, 1000);
  } catch (error) {
    passwordError.value = error.response?.data?.message || error.message || "修改密码失败";
  } finally {
    savingPassword.value = false;
  }
}

async function loadCategoryOptions() {
  try {
    const res = await optionsCategoryApi();
    if (res.code === 200) {
      categoryOptions.value = Array.isArray(res.data) ? res.data : [];
    } else {
      categoryOptions.value = [];
    }
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
  if (articleFilters.auditStatus !== "") {
    params.auditStatus = Number(articleFilters.auditStatus);
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
  articleFilters.auditStatus = "";
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
  router.push("/user/articles/new");
}

function openEditArticle(article) {
  router.push(`/user/articles/${article.id}/edit`);
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

async function handleLogout() {
  closeUserMenu();
  try {
    await logoutApi();
  } catch {
    // 后端无状态登出，忽略异常
  } finally {
    clearAuth();
    router.push("/login");
  }
}

onMounted(async () => {
  document.addEventListener("click", closeUserMenu);
  await Promise.all([loadProfile(), loadCategoryOptions(), loadArticlesPage()]);
});

onUnmounted(() => {
  document.removeEventListener("click", closeUserMenu);
  resetAvatarInput();
});
</script>
