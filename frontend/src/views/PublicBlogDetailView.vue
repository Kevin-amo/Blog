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
      <div v-if="articleLoading" class="muted">正在加载正文...</div>
      <p v-else-if="articleErrorMsg" class="error-msg">{{ articleErrorMsg }}</p>

      <article v-else-if="article" class="public-article-detail">
        <h2>{{ article.title }}</h2>
        <p class="public-meta">
          发布时间：{{ formatDate(article.createTime) }}
          <span v-if="article.updateTime"> · 更新时间：{{ formatDate(article.updateTime) }}</span>
        </p>
        <div class="markdown-body" v-html="detailHtml"></div>

        <div class="article-ai-actions">
          <div v-if="!isLoggedIn" class="article-ai-hint">登录后可使用 AI 总结</div>
          <button
            class="ghost-btn article-ai-btn"
            type="button"
            :disabled="summaryGenerating || !canGenerateSummary"
            @click="generateSummary"
          >
            {{ summaryGenerating ? "生成中..." : "AI总结" }}
          </button>
        </div>

        <p v-if="summaryErrorMsg" class="error-msg article-ai-feedback">{{ summaryErrorMsg }}</p>

        <section
          v-if="hasRequestedSummary && (aiSummary || summaryGenerating)"
          class="article-ai-summary-card"
          aria-live="polite"
        >
          <p class="article-ai-summary-eyebrow">AI总结</p>
          <p v-if="aiSummary" class="article-ai-summary-content">{{ aiSummary }}</p>
          <p v-else class="muted">AI 正在整理这篇文章的重点...</p>
        </section>
      </article>

      <div v-else class="empty-state">文章不存在或暂未发布。</div>
    </section>

    <section class="panel comment-panel">
      <div class="comment-panel-head">
        <div>
          <p class="panel-title">评论区</p>
          <p class="muted">共 {{ commentCount }} 条评论</p>
        </div>
      </div>

      <form v-if="isLoggedIn" class="comment-editor" @submit.prevent="submitComment">
        <div class="comment-editor-head">
          <p class="comment-editor-title">发表评论</p>
          <span class="comment-counter">{{ commentForm.content.trim().length }}/1000</span>
        </div>
        <label class="comment-textarea-wrap">
          <span class="sr-only">评论内容</span>
          <textarea
            v-model="commentForm.content"
            maxlength="1000"
            rows="4"
            placeholder="写下你的想法..."
          ></textarea>
        </label>
        <p v-if="commentSubmitError" class="error-msg">{{ commentSubmitError }}</p>
        <div class="comment-editor-actions">
          <button class="submit-btn" type="submit" :disabled="commentSubmitting">
            {{ commentSubmitting ? "提交中..." : "发表评论" }}
          </button>
        </div>
      </form>

      <div v-else class="comment-login-card">
        <div>
          <p class="comment-editor-title">登录后参与评论</p>
        </div>
        <button class="submit-btn" type="button" @click="goToLogin">前往登录</button>
      </div>

      <div v-if="commentLoading" class="muted comment-feedback">正在加载评论...</div>
      <p v-else-if="commentErrorMsg" class="error-msg comment-feedback">{{ commentErrorMsg }}</p>
      <div v-else-if="!commentTree.length" class="empty-state">还没有评论，来留下第一条评论吧。</div>

      <div v-else class="comment-list">
        <article v-for="parent in commentTree" :key="parent.id" class="comment-card">
          <div class="comment-card-head">
            <div>
              <p class="comment-author">{{ parent.nickname || "游客" }}</p>
              <p class="comment-time">{{ formatDate(parent.createTime) }}</p>
            </div>
            <div class="comment-card-actions">
              <button
                v-if="!isDeletedComment(parent)"
                class="link-btn"
                type="button"
                :disabled="!isLoggedIn"
                @click="openReply(parent, parent)"
              >
                回复
              </button>
              <button
                v-if="canDeleteComment(parent)"
                class="danger-link-btn"
                type="button"
                :disabled="deletingCommentId === parent.id"
                @click="handleDeleteComment(parent)"
              >
                {{ deletingCommentId === parent.id ? "删除中..." : "删除" }}
              </button>
            </div>
          </div>
          <p class="comment-content" :class="{ 'comment-content-deleted': isDeletedComment(parent) }">
            {{ getCommentContent(parent) }}
          </p>

          <form
            v-if="replyForm.targetId === parent.id"
            class="comment-reply-form"
            @submit.prevent="submitReply"
          >
            <div class="comment-editor-head">
              <div>
                <p class="comment-editor-title">回复 @{{ replyForm.targetNickname }}</p>
              </div>
              <span class="comment-counter">{{ replyForm.content.trim().length }}/1000</span>
            </div>
            <label class="comment-textarea-wrap">
              <span class="sr-only">回复内容</span>
              <textarea
                v-model="replyForm.content"
                maxlength="1000"
                rows="3"
                placeholder="写下你的回复..."
              ></textarea>
            </label>
            <p v-if="replySubmitError" class="error-msg">{{ replySubmitError }}</p>
            <div class="comment-editor-actions">
              <button class="ghost-btn" type="button" @click="cancelReply">取消</button>
              <button class="submit-btn" type="submit" :disabled="replySubmitting">
                {{ replySubmitting ? "提交中..." : "回复" }}
              </button>
            </div>
          </form>

          <div v-if="parent.replies.length" class="comment-reply-list">
            <article v-for="reply in parent.replies" :key="reply.id" class="comment-reply-card">
              <div class="comment-card-head">
                <div>
                  <p class="comment-author">{{ reply.nickname || "游客" }}</p>
                  <p class="comment-time">{{ formatDate(reply.createTime) }}</p>
                </div>
                <div class="comment-card-actions">
                  <button
                    class="link-btn"
                    type="button"
                    :disabled="!isLoggedIn"
                    @click="openReply(reply, parent)"
                  >
                    回复
                  </button>
                  <button
                    v-if="canDeleteComment(reply)"
                    class="danger-link-btn"
                    type="button"
                    :disabled="deletingCommentId === reply.id"
                    @click="handleDeleteComment(reply)"
                  >
                    {{ deletingCommentId === reply.id ? "删除中..." : "删除" }}
                  </button>
                </div>
              </div>
              <p class="comment-content">{{ reply.content }}</p>

              <form
                v-if="replyForm.targetId === reply.id"
                class="comment-reply-form nested-reply-form"
                @submit.prevent="submitReply"
              >
                <div class="comment-editor-head">
                  <div>
                    <p class="comment-editor-title">回复 @{{ replyForm.targetNickname }}</p>
                  </div>
                  <span class="comment-counter">{{ replyForm.content.trim().length }}/1000</span>
                </div>
                <label class="comment-textarea-wrap">
                  <span class="sr-only">回复内容</span>
                  <textarea
                    v-model="replyForm.content"
                    maxlength="1000"
                    rows="3"
                    placeholder="写下你的回复..."
                  ></textarea>
                </label>
                <p v-if="replySubmitError" class="error-msg">{{ replySubmitError }}</p>
                <div class="comment-editor-actions">
                  <button class="ghost-btn" type="button" @click="cancelReply">取消</button>
                  <button class="submit-btn" type="submit" :disabled="replySubmitting">
                    {{ replySubmitting ? "提交中..." : "回复" }}
                  </button>
                </div>
              </form>
            </article>
          </div>
        </article>
      </div>
    </section>

    <Teleport to="body">
      <div v-if="deleteConfirm.visible" class="modal-mask" @click.self="resolveDeleteConfirm(false)">
        <section class="modal-card confirm-modal-card">
          <header class="modal-head">
            <h3>{{ deleteConfirm.title }}</h3>
          </header>
          <p class="confirm-message">{{ deleteConfirm.message }}</p>
          <div class="editor-actions confirm-actions">
            <button class="ghost-btn" type="button" @click="resolveDeleteConfirm(false)">
              {{ deleteConfirm.cancelText }}
            </button>
            <button class="danger-btn" type="button" @click="resolveDeleteConfirm(true)">
              {{ deleteConfirm.confirmText }}
            </button>
          </div>
        </section>
      </div>
    </Teleport>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { generateArticleSummaryStreamApi, publicDetailArticleApi } from "../api/article";
import { addCommentApi, deleteCommentApi, listCommentApi } from "../api/comment";
import { getToken, getUserInfo } from "../utils/auth";
import { parseMarkdownToHtml } from "../utils/markdown";
import { finalizeArticleSummary } from "../utils/articleSummary";

const route = useRoute();
const router = useRouter();

const ARTICLE_SUMMARY_MAX_LENGTH = 120;

const article = ref(null);
const articleLoading = ref(false);
const articleErrorMsg = ref("");
const summaryGenerating = ref(false);
const summaryErrorMsg = ref("");
const aiSummary = ref("");
const hasRequestedSummary = ref(false);
const summaryAbortController = ref(null);

const commentTree = ref([]);
const commentCount = ref(0);
const commentLoading = ref(false);
const commentErrorMsg = ref("");
const commentSubmitting = ref(false);
const commentSubmitError = ref("");
const replySubmitting = ref(false);
const replySubmitError = ref("");
const deletingCommentId = ref(null);

const currentUser = ref(getUserInfo());

const commentForm = reactive({
  content: ""
});

const replyForm = reactive({
  targetId: null,
  targetNickname: "",
  parentId: 0,
  content: ""
});
const deleteConfirm = reactive({
  visible: false,
  title: "确认删除评论",
  message: "",
  confirmText: "确认删除",
  cancelText: "取消",
  resolver: null
});

const articleId = computed(() => {
  const raw = Number(route.params.id);
  return Number.isFinite(raw) && raw > 0 ? raw : null;
});

const isLoggedIn = computed(() => Boolean(getToken() && currentUser.value));
const currentUserId = computed(() => {
  const raw = Number(currentUser.value?.userId);
  return Number.isFinite(raw) && raw > 0 ? raw : null;
});
const detailHtml = computed(() => {
  if (!article.value?.content) {
    return "<p class='muted'>暂无正文</p>";
  }
  return parseMarkdownToHtml(article.value.content);
});
const canGenerateSummary = computed(() =>
  Boolean(
    isLoggedIn.value &&
      article.value &&
      !articleLoading.value &&
      article.value.title?.trim() &&
      article.value.content?.trim()
  )
);

function resetSummaryState() {
  abortSummaryStream();
  summaryGenerating.value = false;
  summaryErrorMsg.value = "";
  aiSummary.value = "";
  hasRequestedSummary.value = false;
}

function abortSummaryStream() {
  if (!summaryAbortController.value) {
    return;
  }
  summaryAbortController.value.abort();
  summaryAbortController.value = null;
}

function formatDate(raw) {
  if (!raw) {
    return "-";
  }
  return new Date(raw).toLocaleString("zh-CN", { hour12: false });
}

function getSortTime(raw) {
  const timestamp = raw ? new Date(raw).getTime() : 0;
  return Number.isFinite(timestamp) ? timestamp : 0;
}

function buildCommentTree(rows) {
  const normalized = Array.isArray(rows)
    ? rows.map((item) => ({
        ...item,
        createBy: Number.isFinite(Number(item?.createBy)) ? Number(item.createBy) : null,
        isDeleted: Number(item?.isDeleted || 0),
        replies: [],
        sortTime: getSortTime(item.createTime)
      }))
    : [];

  const commentMap = new Map(normalized.map((item) => [Number(item.id), item]));
  const parents = [];
  const orphans = [];

  normalized.forEach((item) => {
    const parentId = Number(item.parentId || 0);
    if (parentId === 0) {
      parents.push(item);
      return;
    }

    const parent = commentMap.get(parentId);
    if (!parent) {
      orphans.push({ ...item, parentId: 0 });
      return;
    }

    if (Number(parent.parentId || 0) === 0) {
      parent.replies.push(item);
      return;
    }

    const topParent = commentMap.get(Number(parent.parentId));
    if (topParent && Number(topParent.parentId || 0) === 0) {
      topParent.replies.push(item);
      return;
    }

    orphans.push({ ...item, parentId: 0 });
  });

  const mergedParents = [...parents, ...orphans];
  mergedParents.sort((left, right) => right.sortTime - left.sortTime);
  mergedParents.forEach((item) => {
    item.replies.sort((left, right) => left.sortTime - right.sortTime);
  });

  return mergedParents;
}

function validateCommentContent(content) {
  const normalized = content.trim();
  if (!normalized) {
    return "评论内容不能为空";
  }
  if (normalized.length > 1000) {
    return "评论内容不能超过 1000 字";
  }
  return "";
}

function syncCurrentUser() {
  currentUser.value = getUserInfo();
}

function isDeletedComment(comment) {
  return Number(comment?.isDeleted || 0) === 1;
}

function canDeleteComment(comment) {
  return Boolean(
    !isDeletedComment(comment) &&
      isLoggedIn.value &&
      currentUserId.value != null &&
      comment?.createBy != null &&
      Number(comment.createBy) === currentUserId.value
  );
}

function getCommentContent(comment) {
  return isDeletedComment(comment) ? "该评论已删除" : comment?.content || "";
}

function askDeleteConfirm(message) {
  return new Promise((resolve) => {
    deleteConfirm.title = "确认删除评论";
    deleteConfirm.message = message || "确认删除这条评论吗？";
    deleteConfirm.confirmText = "确认删除";
    deleteConfirm.cancelText = "取消";
    deleteConfirm.resolver = resolve;
    deleteConfirm.visible = true;
  });
}

function resolveDeleteConfirm(result) {
  const resolver = deleteConfirm.resolver;
  deleteConfirm.visible = false;
  deleteConfirm.message = "";
  deleteConfirm.resolver = null;
  if (typeof resolver === "function") {
    resolver(Boolean(result));
  }
}

function resetReplyState() {
  replyForm.targetId = null;
  replyForm.targetNickname = "";
  replyForm.parentId = 0;
  replyForm.content = "";
  replySubmitError.value = "";
}

function resolveCommentNickname() {
  return currentUser.value?.nickname || currentUser.value?.username || "";
}

function goToLogin() {
  if (!articleId.value) {
    router.push("/login");
    return;
  }

  router.push({
    path: "/login",
    query: {
      redirect: router.resolve({ name: "public-blog-detail", params: { id: articleId.value } }).fullPath
    }
  });
}

function openReply(targetComment, parentComment) {
  syncCurrentUser();
  if (!isLoggedIn.value) {
    goToLogin();
    return;
  }

  replyForm.targetId = targetComment.id;
  replyForm.targetNickname = targetComment.nickname || "读者";
  replyForm.parentId = Number(parentComment.id || 0);
  replyForm.content = "";
  replySubmitError.value = "";
}

function cancelReply() {
  resetReplyState();
}

async function generateSummary() {
  syncCurrentUser();
  if (!canGenerateSummary.value || summaryGenerating.value) {
    return;
  }

  abortSummaryStream();
  const controller = new AbortController();
  summaryAbortController.value = controller;
  summaryGenerating.value = true;
  summaryErrorMsg.value = "";
  aiSummary.value = "";
  hasRequestedSummary.value = true;
  let streamFailed = false;

  try {
    await generateArticleSummaryStreamApi(
      {
        title: article.value.title.trim(),
        content: article.value.content.trim(),
        maxLength: ARTICLE_SUMMARY_MAX_LENGTH
      },
      {
        signal: controller.signal,
        onChunk: (chunk) => {
          aiSummary.value += chunk;
        },
        onDone: () => {
          aiSummary.value = finalizeArticleSummary(aiSummary.value, ARTICLE_SUMMARY_MAX_LENGTH);
        },
        onError: (message) => {
          streamFailed = true;
          summaryErrorMsg.value = message || "AI 总结生成失败，请稍后重试";
        }
      }
    );

    aiSummary.value = finalizeArticleSummary(aiSummary.value, ARTICLE_SUMMARY_MAX_LENGTH);
    if (!streamFailed && !aiSummary.value) {
      summaryErrorMsg.value = "AI 总结生成失败，请稍后重试";
    }
  } catch (error) {
    if (error.name === "AbortError") {
      return;
    }
    summaryErrorMsg.value = error.response?.data?.message || error.message || "AI 总结生成失败，请稍后重试";
  } finally {
    if (summaryAbortController.value === controller) {
      summaryAbortController.value = null;
    }
    summaryGenerating.value = false;
  }
}

async function loadDetail() {
  if (!articleId.value) {
    articleErrorMsg.value = "文章参数不合法";
    article.value = null;
    return;
  }

  articleLoading.value = true;
  articleErrorMsg.value = "";

  try {
    const res = await publicDetailArticleApi(articleId.value);
    if (res.code !== 200) {
      articleErrorMsg.value = res.message || "获取文章详情失败";
      article.value = null;
      return;
    }
    article.value = res.data || null;
  } catch (error) {
    articleErrorMsg.value = error.response?.data?.message || error.message || "获取文章详情失败";
    article.value = null;
  } finally {
    articleLoading.value = false;
  }
}

async function loadComments() {
  if (!articleId.value) {
    commentErrorMsg.value = "文章参数不合法";
    commentTree.value = [];
    commentCount.value = 0;
    return;
  }

  commentLoading.value = true;
  commentErrorMsg.value = "";

  try {
    const res = await listCommentApi(articleId.value);
    if (res.code !== 200) {
      commentErrorMsg.value = res.message || "获取评论失败";
      commentTree.value = [];
      commentCount.value = 0;
      return;
    }

    const rows = Array.isArray(res.data) ? res.data : [];
    commentCount.value = rows.filter((item) => Number(item?.isDeleted || 0) === 0).length;
    commentTree.value = buildCommentTree(rows);
  } catch (error) {
    commentErrorMsg.value = error.response?.data?.message || error.message || "获取评论失败";
    commentTree.value = [];
    commentCount.value = 0;
  } finally {
    commentLoading.value = false;
  }
}

async function submitComment() {
  syncCurrentUser();
  if (!isLoggedIn.value) {
    goToLogin();
    return;
  }

  const validationMessage = validateCommentContent(commentForm.content);
  if (validationMessage) {
    commentSubmitError.value = validationMessage;
    return;
  }

  const nickname = resolveCommentNickname();
  if (!nickname) {
    commentSubmitError.value = "登录信息已失效，请重新登录后再评论";
    goToLogin();
    return;
  }

  commentSubmitting.value = true;
  commentSubmitError.value = "";

  try {
    const res = await addCommentApi({
      articleId: articleId.value,
      parentId: 0,
      nickname,
      content: commentForm.content.trim()
    });

    if (res.code !== 200) {
      commentSubmitError.value = res.message || "发表评论失败";
      return;
    }

    commentForm.content = "";
    resetReplyState();
    await loadComments();
  } catch (error) {
    commentSubmitError.value = error.response?.data?.message || error.message || "发表评论失败";
  } finally {
    commentSubmitting.value = false;
  }
}

async function submitReply() {
  syncCurrentUser();
  if (!isLoggedIn.value) {
    goToLogin();
    return;
  }

  const validationMessage = validateCommentContent(replyForm.content);
  if (validationMessage) {
    replySubmitError.value = validationMessage;
    return;
  }

  const nickname = resolveCommentNickname();
  if (!nickname) {
    replySubmitError.value = "登录信息已失效，请重新登录后再回复";
    goToLogin();
    return;
  }

  replySubmitting.value = true;
  replySubmitError.value = "";

  try {
    const res = await addCommentApi({
      articleId: articleId.value,
      parentId: replyForm.parentId,
      nickname,
      content: replyForm.content.trim()
    });

    if (res.code !== 200) {
      replySubmitError.value = res.message || "提交回复失败";
      return;
    }

    resetReplyState();
    await loadComments();
  } catch (error) {
    replySubmitError.value = error.response?.data?.message || error.message || "提交回复失败";
  } finally {
    replySubmitting.value = false;
  }
}

async function handleDeleteComment(comment) {
  syncCurrentUser();
  if (!canDeleteComment(comment)) {
    return;
  }

  const confirmed = await askDeleteConfirm("确认删除这条评论吗？删除后将无法恢复。");
  if (!confirmed) {
    return;
  }

  deletingCommentId.value = comment.id;
  commentErrorMsg.value = "";

  try {
    const res = await deleteCommentApi(comment.id);
    if (res.code !== 200) {
      window.alert(res.message || "删除评论失败");
      return;
    }

    resetReplyState();
    await loadComments();
  } catch (error) {
    window.alert(error.response?.data?.message || error.message || "删除评论失败");
  } finally {
    deletingCommentId.value = null;
  }
}

async function loadPage() {
  syncCurrentUser();
  resetSummaryState();
  commentForm.content = "";
  commentSubmitError.value = "";
  resetReplyState();
  await Promise.allSettled([loadDetail(), loadComments()]);
}

watch(
  articleId,
  () => {
    loadPage();
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  abortSummaryStream();
});
</script>
