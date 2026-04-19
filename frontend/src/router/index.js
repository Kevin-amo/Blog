import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import UserDashboardView from "../views/UserDashboardView.vue";
import AdminDashboardView from "../views/AdminDashboardView.vue";
import PublicBlogView from "../views/PublicBlogView.vue";
import PublicCategoryView from "../views/PublicCategoryView.vue";
import PublicAboutView from "../views/PublicAboutView.vue";
import PublicBlogDetailView from "../views/PublicBlogDetailView.vue";
import ArticleEditorView from "../views/ArticleEditorView.vue";
import { clearAuth, getCurrentHomePath, getToken, getUserRole } from "../utils/auth";

const routes = [
  {
    path: "/",
    name: "public-blog",
    component: PublicBlogView
  },
  {
    path: "/category",
    name: "public-category",
    component: PublicCategoryView
  },
  {
    path: "/about",
    name: "public-about",
    component: PublicAboutView
  },
  {
    path: "/blog/:id",
    name: "public-blog-detail",
    component: PublicBlogDetailView
  },
  {
    path: "/login",
    name: "login",
    component: LoginView,
    meta: { guestOnly: true }
  },
  {
    path: "/register",
    name: "register",
    component: RegisterView,
    meta: { guestOnly: true }
  },
  {
    path: "/home",
    redirect: () => {
      const token = getToken();
      return token ? getCurrentHomePath() : "/login";
    }
  },
  {
    path: "/user",
    name: "user",
    component: UserDashboardView,
    meta: { requiresAuth: true, requiredRole: 0 }
  },
  {
    path: "/user/articles/new",
    name: "article-create",
    component: ArticleEditorView,
    meta: { requiresAuth: true, requiredRole: 0 }
  },
  {
    path: "/user/articles/:id/edit",
    name: "article-edit",
    component: ArticleEditorView,
    meta: { requiresAuth: true, requiredRole: 0 }
  },
  {
    path: "/admin",
    name: "admin",
    component: AdminDashboardView,
    meta: { requiresAuth: true, requiredRole: 1 }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const token = getToken();
  const role = getUserRole();

  if (to.meta.requiresAuth && !token) {
    return "/login";
  }

  if (to.meta.guestOnly && token) {
    return getCurrentHomePath();
  }

  if (token && to.meta.requiredRole != null) {
    if (role == null) {
      clearAuth();
      return "/login";
    }
    if (Number(to.meta.requiredRole) !== Number(role)) {
      return getCurrentHomePath();
    }
  }

  return true;
});

export default router;
