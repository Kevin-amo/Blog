<template>
  <main class="home-page article-home-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">Wavelog Blog</p>
        <h1>管理员后台</h1>
      </div>
      <div class="admin-user-menu" @click.stop>
        <button class="admin-avatar-btn" type="button" :title="adminDisplayName" @click="toggleUserMenu">
          <img v-if="adminProfile.avatar" :src="adminProfile.avatar" alt="用户头像" />
          <span v-else class="admin-avatar-fallback">{{ adminAvatarFallback }}</span>
        </button>
        <div v-if="showUserMenu" class="admin-user-dropdown">
          <button type="button" @click="openProfileEditor">个人资料</button>
          <button type="button" @click="handleLogout">退出登录</button>
        </div>
      </div>
    </header>

    <Transition name="admin-notice">
      <div v-if="notice.visible" class="admin-notice" :class="`notice-${notice.type}`">
        {{ notice.message }}
      </div>
    </Transition>

    <section class="hero">
      <h2>欢迎，{{ adminDisplayName }}</h2>
      <p>这里用于管理普通用户、审核文章与维护分类。</p>
    </section>

    <section class="panel workspace-switch">
      <button class="tab-btn" :class="{ active: activeTab === 'users' }" type="button" @click="activeTab = 'users'">
        用户管理
      </button>
      <button class="tab-btn" :class="{ active: activeTab === 'review' }" type="button" @click="activeTab = 'review'">
        文章审核
      </button>
      <button class="tab-btn" :class="{ active: activeTab === 'category' }" type="button" @click="activeTab = 'category'">
        分类管理
      </button>
    </section>

    <template v-if="activeTab === 'users'">
      <section class="panel article-filter-panel">
        <div class="panel-title">用户筛选</div>
        <form class="article-filter" @submit.prevent="searchUsers">
          <label>
            用户名
            <input v-model.trim="userFilters.username" type="text" placeholder="输入用户名关键字" />
          </label>
          <label>
            昵称
            <input v-model.trim="userFilters.nickname" type="text" placeholder="输入昵称关键字" />
          </label>
          <label>
            状态
            <div class="select-shell">
              <select v-model="userFilters.status" class="select-control">
                <option value="">全部</option>
                <option value="1">启用</option>
                <option value="0">禁用</option>
              </select>
            </div>
          </label>
          <div class="article-filter-actions">
            <button class="submit-btn" type="submit">查询</button>
            <button class="ghost-btn" type="button" @click="resetUsers">重置</button>
          </div>
        </form>
      </section>

      <section class="panel article-panel">
        <div class="article-panel-head">
          <div>
            <div class="panel-title">普通用户列表</div>
            <p class="muted">共 {{ userPager.total }} 位</p>
          </div>
        </div>
        <div v-if="loadingUsers" class="muted">正在加载用户列表...</div>
        <p v-else-if="userError" class="error-msg">{{ userError }}</p>
        <div v-else-if="!userRows.length" class="empty-state">暂无普通用户。</div>
        <div v-else class="article-table-wrap">
          <table class="article-table">
            <thead>
              <tr>
                <th>用户ID</th>
                <th>用户名</th>
                <th>昵称</th>
                <th>状态</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in userRows" :key="item.id">
                <td>{{ item.id }}</td>
                <td>{{ item.username }}</td>
                <td>{{ item.nickname || "-" }}</td>
                <td>{{ item.status === 1 ? "启用" : "禁用" }}</td>
                <td>{{ formatDate(item.createTime) }}</td>
                <td>
                  <div class="row-actions">
                    <button class="link-btn" type="button" @click="handleToggleUserStatus(item)">
                      {{ item.status === 1 ? "禁用" : "启用" }}
                    </button>
                    <button class="link-btn" type="button" @click="handleResetUserPassword(item)">重置密码</button>
                    <button class="danger-link-btn" type="button" @click="handleDeleteUser(item)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pagination" v-if="userPager.total > 0">
          <div class="pagination-meta">
            <span>第 {{ userPager.pageNum }} / {{ userTotalPages }} 页</span>
            <span>每页</span>
            <div class="select-shell select-shell-mini">
              <select v-model.number="userPager.pageSize" class="select-control" @change="changeUserPageSize">
                <option :value="5">5</option>
                <option :value="10">10</option>
                <option :value="20">20</option>
              </select>
            </div>
          </div>
          <div class="pagination-actions">
            <button class="ghost-btn" type="button" :disabled="userPager.pageNum <= 1" @click="goUserPage(userPager.pageNum - 1)">
              上一页
            </button>
            <button class="ghost-btn" type="button" :disabled="userPager.pageNum >= userTotalPages" @click="goUserPage(userPager.pageNum + 1)">
              下一页
            </button>
          </div>
        </div>
      </section>
    </template>

    <template v-else-if="activeTab === 'review'">
      <section class="panel article-filter-panel">
        <div class="panel-title">审核筛选</div>
        <form class="article-filter" @submit.prevent="searchReviews">
          <label>
            标题
            <input v-model.trim="reviewFilters.title" type="text" placeholder="输入标题关键字" />
          </label>
          <label>
            审核状态
            <div class="select-shell">
              <select v-model="reviewFilters.auditStatus" class="select-control">
                <option value="">全部</option>
                <option value="0">待审核</option>
                <option value="1">审核通过</option>
                <option value="2">审核驳回</option>
              </select>
            </div>
          </label>
          <div class="article-filter-actions">
            <button class="submit-btn" type="submit">查询</button>
            <button class="ghost-btn" type="button" @click="resetReviews">重置</button>
          </div>
        </form>
      </section>

      <section class="panel article-panel">
        <div class="article-panel-head">
          <div>
            <div class="panel-title">文章审核列表</div>
            <p class="muted">共 {{ reviewPager.total }} 篇</p>
          </div>
        </div>
        <div v-if="loadingReviews" class="muted">正在加载审核列表...</div>
        <p v-else-if="reviewError" class="error-msg">{{ reviewError }}</p>
        <div v-else-if="!reviewRows.length" class="empty-state">暂无待审核文章。</div>
        <div v-else class="article-table-wrap">
          <table class="article-table">
            <thead>
              <tr>
                <th>标题</th>
                <th>作者</th>
                <th>发布状态</th>
                <th>审核状态</th>
                <th>提交时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in reviewRows" :key="item.id">
                <td>
                  <p class="article-title">{{ item.title }}</p>
                  <p class="article-summary">{{ item.summary || "暂无摘要" }}</p>
                </td>
                <td>{{ item.authorNickname || item.authorUsername || "-" }}</td>
                <td>{{ item.status === 1 ? "已提交发布" : "草稿" }}</td>
                <td>{{ getAuditLabel(item.auditStatus) }}</td>
                <td>{{ formatDate(item.createTime) }}</td>
                <td>
                  <div class="row-actions">
                    <button class="link-btn" type="button" @click="openReviewDetail(item)">查看全文</button>
                    <button class="link-btn" type="button" @click="handleAuditArticle(item, 1)">通过</button>
                    <button class="danger-link-btn" type="button" @click="handleAuditArticle(item, 2)">驳回</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pagination" v-if="reviewPager.total > 0">
          <div class="pagination-meta">
            <span>第 {{ reviewPager.pageNum }} / {{ reviewTotalPages }} 页</span>
            <span>每页</span>
            <div class="select-shell select-shell-mini">
              <select v-model.number="reviewPager.pageSize" class="select-control" @change="changeReviewPageSize">
                <option :value="5">5</option>
                <option :value="10">10</option>
                <option :value="20">20</option>
              </select>
            </div>
          </div>
          <div class="pagination-actions">
            <button class="ghost-btn" type="button" :disabled="reviewPager.pageNum <= 1" @click="goReviewPage(reviewPager.pageNum - 1)">
              上一页
            </button>
            <button class="ghost-btn" type="button" :disabled="reviewPager.pageNum >= reviewTotalPages" @click="goReviewPage(reviewPager.pageNum + 1)">
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
                <td>{{ category.status === 1 ? "启用" : "禁用" }}</td>
                <td>{{ formatDate(category.createTime) }}</td>
                <td>
                  <div class="row-actions">
                    <button class="link-btn" type="button" @click="openEditCategory(category)">编辑</button>
                    <button class="danger-link-btn" type="button" @click="handleDeleteCategoryAction(category)">删除</button>
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

    <Teleport to="body">
      <div v-if="confirmDialog.visible" class="modal-mask" @click.self="resolveConfirm(false)">
        <section class="modal-card confirm-modal-card">
          <header class="modal-head">
            <h3>{{ confirmDialog.title }}</h3>
            <button class="ghost-btn" type="button" @click="resolveConfirm(false)">
              {{ confirmDialog.cancelText }}
            </button>
          </header>
          <p class="confirm-message">{{ confirmDialog.message }}</p>
          <div class="editor-actions confirm-actions">
            <button class="ghost-btn" type="button" @click="resolveConfirm(false)">
              {{ confirmDialog.cancelText }}
            </button>
            <button
              :class="confirmDialog.danger ? 'danger-btn' : 'submit-btn'"
              type="button"
              @click="resolveConfirm(true)"
            >
              {{ confirmDialog.confirmText }}
            </button>
          </div>
        </section>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="showReviewDetail && reviewDetail" class="modal-mask" @click.self="closeReviewDetail">
        <section class="modal-card article-review-modal">
          <header class="modal-head">
            <h3>文章详情</h3>
            <button class="ghost-btn" type="button" @click="closeReviewDetail">关闭</button>
          </header>

          <article class="review-detail">
            <h4 class="review-title">{{ reviewDetail.title }}</h4>
            <p class="review-meta">
              作者：{{ reviewDetail.authorNickname || reviewDetail.authorUsername || "-" }}
              <span> · 提交时间：{{ formatDate(reviewDetail.createTime) }}</span>
            </p>
            <p v-if="reviewDetail.summary" class="review-summary">摘要：{{ reviewDetail.summary }}</p>
            <div v-if="reviewDetailLoading" class="review-content muted">正在加载正文...</div>
            <p v-else-if="reviewDetailError" class="error-msg">{{ reviewDetailError }}</p>
            <div v-else class="markdown-body review-content" v-html="reviewDetailHtml"></div>
          </article>

          <div class="editor-actions">
            <button class="ghost-btn" type="button" @click="closeReviewDetail">关闭</button>
            <button class="danger-btn" type="button" @click="handleAuditArticle(reviewDetail, 2)">驳回</button>
            <button class="submit-btn" type="button" @click="handleAuditArticle(reviewDetail, 1)">通过</button>
          </div>
        </section>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="showCategoryEditor" class="modal-mask" @click.self="closeCategoryEditor">
        <section class="modal-card category-editor-modal">
          <header class="modal-head">
            <h3>{{ editingCategoryId ? "编辑分类" : "新增分类" }}</h3>
            <button class="ghost-btn" type="button" @click="closeCategoryEditor">关闭</button>
          </header>

          <form class="article-filter category-editor-form" @submit.prevent="submitCategory">
            <label>
              分类名称
              <input v-model.trim="categoryEditor.name" type="text" maxlength="30" placeholder="请输入分类名称" />
            </label>

            <label>
              排序
              <input v-model.number="categoryEditor.sort" type="number" min="0" placeholder="默认100，越小越靠前" />
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

            <p v-if="categoryEditorError" class="error-msg">{{ categoryEditorError }}</p>

            <div class="article-filter-actions">
              <button class="ghost-btn" type="button" @click="closeCategoryEditor">取消</button>
              <button class="submit-btn" type="submit" :disabled="savingCategory">
                {{ savingCategory ? "提交中..." : "确认保存" }}
              </button>
            </div>
          </form>
        </section>
      </div>
    </Teleport>

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
                :disabled="uploadingProfileAvatar"
                @click="triggerProfileAvatarPicker"
              >
                <img v-if="adminProfile.avatar" :src="adminProfile.avatar" alt="用户头像" />
                <div v-else class="avatar-fallback">{{ adminAvatarFallback }}</div>
              </button>

              <div class="avatar-actions">
                <input
                  ref="profileAvatarInputRef"
                  class="avatar-file-input"
                  type="file"
                  accept="image/jpeg,image/png"
                  @change="handleProfileAvatarFileChange"
                />
                <span class="avatar-file-name avatar-file-name-alone">{{ selectedProfileAvatarFileName }}</span>
                <button
                  class="submit-btn avatar-upload-btn"
                  type="button"
                  :disabled="uploadingProfileAvatar"
                  @click="submitProfileAvatar"
                >
                  {{ uploadingProfileAvatar ? "上传中..." : "上传头像" }}
                </button>
              </div>
              <p class="muted avatar-tips">仅支持 jpg/jpeg/png，大小不超过 5MB</p>
              <p v-if="profileAvatarError" class="error-msg">{{ profileAvatarError }}</p>
            </div>

            <div class="admin-profile-main">
              <div class="admin-profile-meta">
                <p class="field">用户名</p>
                <p class="value">{{ adminProfile.username || "-" }}</p>
              </div>

              <form class="admin-profile-form" @submit.prevent="submitProfileNickname">
                <h4>修改昵称</h4>
                <label>
                  昵称
                  <input v-model.trim="profileForm.nickname" type="text" maxlength="50" placeholder="请输入新的昵称" />
                </label>
                <p v-if="profileUpdateError" class="error-msg">{{ profileUpdateError }}</p>
                <div class="article-filter-actions">
                  <button class="submit-btn" type="submit" :disabled="savingProfile">保存资料</button>
                </div>
              </form>

              <form class="admin-profile-form" @submit.prevent="submitProfilePassword">
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
import {
  auditReviewArticleApi,
  deleteAdminUserApi,
  detailReviewArticleApi,
  pageAdminUsersApi,
  pageReviewArticlesApi,
  resetAdminUserPasswordApi,
  updateAdminUserStatusApi
} from "../api/admin";
import {
  addCategoryApi,
  deleteCategoryApi,
  detailCategoryApi,
  pageCategoryApi,
  updateCategoryApi
} from "../api/category";
import { updateMyPasswordApi, updateMyProfileApi } from "../api/user";
import { clearAuth, getUserInfo, setUserInfo } from "../utils/auth";
import { parseMarkdownToHtml } from "../utils/markdown";

const router = useRouter();
const activeTab = ref("users");
const notice = reactive({
  visible: false,
  type: "info",
  message: "",
  timer: null
});
const confirmDialog = reactive({
  visible: false,
  title: "请确认操作",
  message: "",
  confirmText: "确认",
  cancelText: "取消",
  danger: false,
  resolver: null
});
const localUser = getUserInfo();
const adminProfile = ref({
  userId: localUser?.userId || "",
  username: localUser?.username || "",
  nickname: localUser?.nickname || "",
  avatar: localUser?.avatar || "",
  role: Number(localUser?.role ?? 1)
});
const loadingProfile = ref(false);
const showProfileEditor = ref(false);
const showUserMenu = ref(false);
const uploadingProfileAvatar = ref(false);
const profileAvatarError = ref("");
const selectedProfileAvatarFile = ref(null);
const profileAvatarInputRef = ref(null);
const savingProfile = ref(false);
const profileUpdateError = ref("");
const savingPassword = ref(false);
const passwordError = ref("");
const profileForm = reactive({
  nickname: adminProfile.value.nickname || ""
});
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

const userFilters = reactive({
  username: "",
  nickname: "",
  status: ""
});
const userPager = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});
const userRows = ref([]);
const loadingUsers = ref(false);
const userError = ref("");

const reviewFilters = reactive({
  title: "",
  auditStatus: "0"
});
const reviewPager = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});
const reviewRows = ref([]);
const loadingReviews = ref(false);
const reviewError = ref("");
const showReviewDetail = ref(false);
const reviewDetail = ref(null);
const reviewDetailLoading = ref(false);
const reviewDetailError = ref("");

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

const userTotalPages = computed(() => Math.max(1, Math.ceil(userPager.total / userPager.pageSize) || 1));
const reviewTotalPages = computed(() => Math.max(1, Math.ceil(reviewPager.total / reviewPager.pageSize) || 1));
const categoryTotalPages = computed(() => Math.max(1, Math.ceil(categoryPager.total / categoryPager.pageSize) || 1));
const adminDisplayName = computed(() => adminProfile.value.nickname || adminProfile.value.username || "管理员");
const adminAvatarFallback = computed(() => {
  const source = adminProfile.value.nickname || adminProfile.value.username || "A";
  return source.slice(0, 1).toUpperCase();
});
const selectedProfileAvatarFileName = computed(() => selectedProfileAvatarFile.value?.name || "未选择文件");
const reviewDetailHtml = computed(() => {
  if (!reviewDetail.value?.content) {
    return "<p class='muted'>暂无正文</p>";
  }
  return parseMarkdownToHtml(reviewDetail.value.content);
});

function getAuditLabel(auditStatus) {
  if (Number(auditStatus) === 1) {
    return "审核通过";
  }
  if (Number(auditStatus) === 2) {
    return "审核驳回";
  }
  return "待审核";
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

function notify(type, message) {
  if (!message) {
    return;
  }
  if (notice.timer) {
    clearTimeout(notice.timer);
    notice.timer = null;
  }
  notice.type = type || "info";
  notice.message = message;
  notice.visible = true;
  notice.timer = setTimeout(() => {
    notice.visible = false;
    notice.timer = null;
  }, 2600);
}

function resetPasswordForm() {
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
}

function resetProfileAvatarInput() {
  selectedProfileAvatarFile.value = null;
  if (profileAvatarInputRef.value) {
    profileAvatarInputRef.value.value = "";
  }
}

function validateAvatarFile(file) {
  if (!file) {
    return "请先选择头像文件";
  }
  const acceptedTypes = new Set(["image/jpeg", "image/jpg", "image/png"]);
  if (!acceptedTypes.has((file.type || "").toLowerCase())) {
    return "仅支持 jpg/jpeg/png 格式";
  }
  const maxSize = 5 * 1024 * 1024;
  if (file.size > maxSize) {
    return "头像大小不能超过 5MB";
  }
  return "";
}

async function loadMyProfile({ silent = false } = {}) {
  loadingProfile.value = true;
  try {
    const res = await getMyProfileApi();
    if (res.code !== 200) {
      if (!silent) {
        notify("error", res.message || "获取个人信息失败");
      }
      return;
    }
    adminProfile.value = {
      userId: res.data?.userId || "",
      username: res.data?.username || "",
      nickname: res.data?.nickname || "",
      avatar: res.data?.avatar || "",
      role: Number(res.data?.role ?? 1)
    };
    profileForm.nickname = adminProfile.value.nickname || "";
    setUserInfo(adminProfile.value);
  } catch (error) {
    if (!silent) {
      notify("error", error.response?.data?.message || error.message || "获取个人信息失败");
    }
  } finally {
    loadingProfile.value = false;
  }
}

function openProfileEditor() {
  closeUserMenu();
  showProfileEditor.value = true;
  profileAvatarError.value = "";
  profileUpdateError.value = "";
  passwordError.value = "";
  profileForm.nickname = adminProfile.value.nickname || "";
  resetProfileAvatarInput();
  resetPasswordForm();
}

function closeProfileEditor() {
  if (uploadingProfileAvatar.value || savingProfile.value || savingPassword.value) {
    return;
  }
  showProfileEditor.value = false;
  profileAvatarError.value = "";
  profileUpdateError.value = "";
  passwordError.value = "";
  resetProfileAvatarInput();
  resetPasswordForm();
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value;
}

function closeUserMenu() {
  showUserMenu.value = false;
}

function triggerProfileAvatarPicker() {
  if (uploadingProfileAvatar.value) {
    return;
  }
  profileAvatarInputRef.value?.click();
}

function handleProfileAvatarFileChange(event) {
  profileAvatarError.value = "";
  selectedProfileAvatarFile.value = event.target?.files?.[0] || null;
}

async function submitProfileAvatar() {
  if (uploadingProfileAvatar.value) {
    return;
  }
  profileAvatarError.value = validateAvatarFile(selectedProfileAvatarFile.value);
  if (profileAvatarError.value) {
    return;
  }
  const formData = new FormData();
  formData.append("file", selectedProfileAvatarFile.value);
  uploadingProfileAvatar.value = true;
  try {
    const res = await uploadAvatarApi(formData);
    if (res.code !== 200) {
      profileAvatarError.value = res.message || "头像上传失败";
      return;
    }
    resetProfileAvatarInput();
    await loadMyProfile({ silent: true });
    notify("success", "头像已更新");
  } catch (error) {
    profileAvatarError.value = error.response?.data?.message || error.message || "头像上传失败";
  } finally {
    uploadingProfileAvatar.value = false;
  }
}

async function submitProfileNickname() {
  profileUpdateError.value = "";
  const nickname = (profileForm.nickname || "").trim();
  if (!nickname) {
    profileUpdateError.value = "请输入昵称";
    return;
  }
  savingProfile.value = true;
  try {
    const res = await updateMyProfileApi({ nickname });
    if (res.code !== 200) {
      profileUpdateError.value = res.message || "更新昵称失败";
      return;
    }
    await loadMyProfile({ silent: true });
    notify("success", "个人资料已更新");
  } catch (error) {
    profileUpdateError.value = error.response?.data?.message || error.message || "更新昵称失败";
  } finally {
    savingProfile.value = false;
  }
}

async function submitProfilePassword() {
  passwordError.value = "";
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
    resetPasswordForm();
    notify("success", "密码已更新");
  } catch (error) {
    passwordError.value = error.response?.data?.message || error.message || "修改密码失败";
  } finally {
    savingPassword.value = false;
  }
}

function askConfirm({ title, message, confirmText = "确认", cancelText = "取消", danger = false } = {}) {
  return new Promise((resolve) => {
    confirmDialog.title = title || "请确认操作";
    confirmDialog.message = message || "";
    confirmDialog.confirmText = confirmText;
    confirmDialog.cancelText = cancelText;
    confirmDialog.danger = Boolean(danger);
    confirmDialog.resolver = resolve;
    confirmDialog.visible = true;
  });
}

function resolveConfirm(result) {
  const resolver = confirmDialog.resolver;
  confirmDialog.visible = false;
  confirmDialog.resolver = null;
  confirmDialog.title = "请确认操作";
  confirmDialog.message = "";
  confirmDialog.confirmText = "确认";
  confirmDialog.cancelText = "取消";
  confirmDialog.danger = false;
  if (typeof resolver === "function") {
    resolver(Boolean(result));
  }
}

function buildUserParams() {
  const params = {
    pageNum: userPager.pageNum,
    pageSize: userPager.pageSize
  };
  if (userFilters.username) {
    params.username = userFilters.username;
  }
  if (userFilters.nickname) {
    params.nickname = userFilters.nickname;
  }
  if (userFilters.status !== "") {
    params.status = Number(userFilters.status);
  }
  return params;
}

async function loadUsers() {
  loadingUsers.value = true;
  userError.value = "";
  try {
    const res = await pageAdminUsersApi(buildUserParams());
    if (res.code !== 200) {
      userError.value = res.message || "获取用户列表失败";
      userRows.value = [];
      userPager.total = 0;
      return;
    }
    const pageData = normalizePageData(res.data);
    userRows.value = pageData.records;
    userPager.total = pageData.total;
  } catch (error) {
    userError.value = error.response?.data?.message || error.message || "获取用户列表失败";
  } finally {
    loadingUsers.value = false;
  }
}

function searchUsers() {
  userPager.pageNum = 1;
  loadUsers();
}

function resetUsers() {
  userFilters.username = "";
  userFilters.nickname = "";
  userFilters.status = "";
  userPager.pageNum = 1;
  loadUsers();
}

function goUserPage(pageNum) {
  if (pageNum < 1 || pageNum > userTotalPages.value || loadingUsers.value) {
    return;
  }
  userPager.pageNum = pageNum;
  loadUsers();
}

function changeUserPageSize() {
  userPager.pageNum = 1;
  loadUsers();
}

async function toggleUserStatus(item) {
  return handleToggleUserStatus(item);
}

async function resetUserPassword(item) {
  return handleResetUserPassword(item);
}

async function deleteUser(item) {
  return handleDeleteUser(item);
}

function buildReviewParams() {
  const params = {
    pageNum: reviewPager.pageNum,
    pageSize: reviewPager.pageSize
  };
  if (reviewFilters.title) {
    params.title = reviewFilters.title;
  }
  if (reviewFilters.auditStatus !== "") {
    params.auditStatus = Number(reviewFilters.auditStatus);
  }
  return params;
}

async function loadReviews() {
  loadingReviews.value = true;
  reviewError.value = "";
  try {
    const res = await pageReviewArticlesApi(buildReviewParams());
    if (res.code !== 200) {
      reviewError.value = res.message || "获取审核列表失败";
      reviewRows.value = [];
      reviewPager.total = 0;
      return;
    }
    const pageData = normalizePageData(res.data);
    reviewRows.value = pageData.records;
    reviewPager.total = pageData.total;
    if (showReviewDetail.value && reviewDetail.value?.id) {
      const latest = pageData.records.find((record) => record.id === reviewDetail.value.id);
      if (latest) {
        reviewDetail.value = {
          ...latest,
          content: reviewDetail.value.content || latest.content || ""
        };
      } else {
        closeReviewDetail();
      }
    }
  } catch (error) {
    reviewError.value = error.response?.data?.message || error.message || "获取审核列表失败";
  } finally {
    loadingReviews.value = false;
  }
}

function searchReviews() {
  reviewPager.pageNum = 1;
  loadReviews();
}

function resetReviews() {
  reviewFilters.title = "";
  reviewFilters.auditStatus = "0";
  reviewPager.pageNum = 1;
  loadReviews();
}

function goReviewPage(pageNum) {
  if (pageNum < 1 || pageNum > reviewTotalPages.value || loadingReviews.value) {
    return;
  }
  reviewPager.pageNum = pageNum;
  loadReviews();
}

function changeReviewPageSize() {
  reviewPager.pageNum = 1;
  loadReviews();
}

async function openReviewDetail(item) {
  showReviewDetail.value = true;
  reviewDetailError.value = "";
  reviewDetailLoading.value = true;
  reviewDetail.value = {
    ...item,
    content: item?.content || ""
  };
  const currentId = item.id;
  try {
    const res = await detailReviewArticleApi(currentId);
    if (res.code !== 200) {
      reviewDetailError.value = res.message || "获取正文失败";
      return;
    }
    if (!showReviewDetail.value || reviewDetail.value?.id !== currentId) {
      return;
    }
    reviewDetail.value = {
      ...reviewDetail.value,
      ...(res.data || {})
    };
  } catch (error) {
    reviewDetailError.value = error.response?.data?.message || error.message || "获取正文失败";
  } finally {
    if (showReviewDetail.value && reviewDetail.value?.id === currentId) {
      reviewDetailLoading.value = false;
    }
  }
}

function closeReviewDetail() {
  showReviewDetail.value = false;
  reviewDetail.value = null;
  reviewDetailLoading.value = false;
  reviewDetailError.value = "";
}

async function auditArticle(item, auditStatus) {
  return handleAuditArticle(item, auditStatus);
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

async function loadCategories() {
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
      await loadCategories();
      return;
    }
  } catch (error) {
    categoryError.value = error.response?.data?.message || error.message || "获取分类分页失败";
  } finally {
    loadingCategories.value = false;
  }
}

function handleCategorySearch() {
  categoryPager.pageNum = 1;
  loadCategories();
}

function resetCategoryFilters() {
  categoryFilters.name = "";
  categoryFilters.status = "";
  categoryPager.pageNum = 1;
  loadCategories();
}

function goCategoryPage(pageNum) {
  if (pageNum < 1 || pageNum > categoryTotalPages.value || loadingCategories.value) {
    return;
  }
  categoryPager.pageNum = pageNum;
  loadCategories();
}

function changeCategoryPageSize() {
  categoryPager.pageNum = 1;
  loadCategories();
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
  editingCategoryId.value = null;
  resetCategoryEditor();
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
    const isEdit = Boolean(editingCategoryId.value);
    const res = isEdit
      ? await updateCategoryApi(payload)
      : await addCategoryApi(payload);
    if (res.code !== 200) {
      categoryEditorError.value = res.message || "提交失败";
      return;
    }
    showCategoryEditor.value = false;
    editingCategoryId.value = null;
    resetCategoryEditor();
    categoryPager.pageNum = 1;
    await loadCategories();
    notify("success", isEdit ? `已更新分类 ${payload.name}` : `已新增分类 ${payload.name}`);
  } catch (error) {
    categoryEditorError.value = error.response?.data?.message || error.message || "提交失败";
  } finally {
    savingCategory.value = false;
  }
}

async function handleDeleteCategory(category) {
  return handleDeleteCategoryAction(category);
}

// Override browser-native popups with custom modal + notice UI
async function handleToggleUserStatus(item) {
  const nextStatus = item.status === 1 ? 0 : 1;
  const confirmed = await askConfirm({
    title: nextStatus === 1 ? "确认启用用户" : "确认禁用用户",
    message: `确认${nextStatus === 1 ? "启用" : "禁用"}用户 ${item.username} 吗？`,
    confirmText: nextStatus === 1 ? "确认启用" : "确认禁用",
    danger: nextStatus !== 1
  });
  if (!confirmed) {
    return;
  }
  try {
    const res = await updateAdminUserStatusApi(item.id, nextStatus);
    if (res.code !== 200) {
      notify("error", res.message || "修改状态失败");
      return;
    }
    await loadUsers();
    notify("success", `已${nextStatus === 1 ? "启用" : "禁用"}用户 ${item.username}`);
  } catch (error) {
    notify("error", error.response?.data?.message || error.message || "修改状态失败");
  }
}

async function handleResetUserPassword(item) {
  const confirmed = await askConfirm({
    title: "确认重置密码",
    message: `确认将用户 ${item.username} 的密码重置为默认密码 Blog@123456 吗？`,
    confirmText: "确认重置"
  });
  if (!confirmed) {
    return;
  }
  try {
    const res = await resetAdminUserPasswordApi(item.id);
    if (res.code !== 200) {
      notify("error", res.message || "重置密码失败");
      return;
    }
    notify("success", "重置成功，默认密码为 Blog@123456");
  } catch (error) {
    notify("error", error.response?.data?.message || error.message || "重置密码失败");
  }
}

async function handleDeleteUser(item) {
  const confirmed = await askConfirm({
    title: "确认删除用户",
    message: `确认删除用户 ${item.username} 吗？该用户文章也会被删除。`,
    confirmText: "确认删除",
    danger: true
  });
  if (!confirmed) {
    return;
  }
  try {
    const res = await deleteAdminUserApi(item.id);
    if (res.code !== 200) {
      notify("error", res.message || "删除用户失败");
      return;
    }
    await Promise.all([loadUsers(), loadReviews()]);
    notify("success", `已删除用户 ${item.username}`);
  } catch (error) {
    notify("error", error.response?.data?.message || error.message || "删除用户失败");
  }
}

async function handleAuditArticle(item, auditStatus) {
  const approved = auditStatus === 1;
  const confirmed = await askConfirm({
    title: approved ? "确认通过文章" : "确认驳回文章",
    message: `确认${approved ? "通过" : "驳回"}文章《${item.title}》吗？`,
    confirmText: approved ? "确认通过" : "确认驳回",
    danger: !approved
  });
  if (!confirmed) {
    return;
  }
  try {
    const res = await auditReviewArticleApi(item.id, auditStatus);
    if (res.code !== 200) {
      notify("error", res.message || "审核失败");
      return;
    }
    await loadReviews();
    notify("success", `文章已${approved ? "通过" : "驳回"}`);
  } catch (error) {
    notify("error", error.response?.data?.message || error.message || "审核失败");
  }
}

async function handleDeleteCategoryAction(category) {
  const confirmed = await askConfirm({
    title: "确认删除分类",
    message: `确认删除分类“${category.name}”吗？`,
    confirmText: "确认删除",
    danger: true
  });
  if (!confirmed) {
    return;
  }
  try {
    const res = await deleteCategoryApi(category.id);
    if (res.code !== 200) {
      notify("error", res.message || "删除失败");
      return;
    }
    if (categoryRows.value.length === 1 && categoryPager.pageNum > 1) {
      categoryPager.pageNum -= 1;
    }
    await loadCategories();
    notify("success", `已删除分类 ${category.name}`);
  } catch (error) {
    notify("error", error.response?.data?.message || error.message || "删除失败");
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
  await Promise.all([loadMyProfile({ silent: true }), loadUsers(), loadReviews(), loadCategories()]);
});

onUnmounted(() => {
  document.removeEventListener("click", closeUserMenu);
  if (notice.timer) {
    clearTimeout(notice.timer);
    notice.timer = null;
  }
  if (confirmDialog.visible) {
    resolveConfirm(false);
  }
  resetProfileAvatarInput();
});
</script>
