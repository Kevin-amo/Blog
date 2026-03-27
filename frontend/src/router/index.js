import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../views/LoginView.vue";
import HomeView from "../views/HomeView.vue";
import PublicBlogView from "../views/PublicBlogView.vue";
import { getToken } from "../utils/auth";

const routes = [
  {
    path: "/",
    name: "blog",
    component: PublicBlogView
  },
  {
    path: "/login",
    name: "login",
    component: LoginView,
    meta: { guestOnly: true }
  },
  {
    path: "/home",
    redirect: "/admin"
  },
  {
    path: "/admin",
    name: "admin",
    component: HomeView,
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const token = getToken();
  if (to.meta.requiresAuth && !token) {
    return "/login";
  }
  if (to.meta.guestOnly && token) {
    return "/admin";
  }
  return true;
});

export default router;
