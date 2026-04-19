<template>
  <main class="auth-page auth-page-plain">
    <section class="auth-card auth-card-plain">
      <aside class="auth-intro auth-intro-plain">
        <p class="eyebrow">Wavelog Blog</p>
        <h1>账号登录</h1>
        <p class="intro-text">
          登录后进入后台，进行文章管理、资料维护和内容发布。
        </p>
      </aside>

      <section class="auth-form-wrap auth-form-wrap-plain">
        <h2>登录</h2>
        <p class="sub">输入账号信息继续</p>

        <form class="auth-form" @submit.prevent="handleLogin">
          <label>
            用户名
            <input
              v-model.trim="form.username"
              type="text"
              placeholder="请输入用户名"
              autocomplete="username"
            />
          </label>

          <label>
            密码
            <input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
          </label>

          <p v-if="successMsg" class="success-msg">{{ successMsg }}</p>
          <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

          <button :disabled="loading" type="submit" class="submit-btn">
            {{ loading ? "登录中..." : "登录" }}
          </button>

          <p class="auth-switch">
            还没有账号？
            <RouterLink to="/register">去注册</RouterLink>
          </p>
        </form>
      </section>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { loginApi } from "../api/auth";
import { getHomePathByRole, setToken, setUserInfo } from "../utils/auth";

const router = useRouter();
const route = useRoute();

const form = reactive({
  username: "",
  password: ""
});

const loading = ref(false);
const errorMsg = ref("");
const successMsg = ref("");

if (route.query.registered === "1") {
  successMsg.value = "注册成功，请登录";
  if (typeof route.query.username === "string" && route.query.username) {
    form.username = route.query.username;
  }
}

function resolvePostLoginPath(role) {
  const redirect = route.query.redirect;
  if (typeof redirect === "string" && redirect.startsWith("/") && !redirect.startsWith("//")) {
    return redirect;
  }
  return getHomePathByRole(role);
}

async function handleLogin() {
  if (!form.username || !form.password) {
    errorMsg.value = "请输入用户名和密码";
    return;
  }

  loading.value = true;
  errorMsg.value = "";

  try {
    const res = await loginApi({
      username: form.username,
      password: form.password
    });

    if (res.code !== 200) {
      errorMsg.value = res.message || "登录失败，请稍后重试";
      return;
    }

    const token = res.data?.token;
    if (!token) {
      errorMsg.value = "未获取到 token，请检查后端返回";
      return;
    }

    setToken(token);
    setUserInfo({
      userId: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname,
      role: Number(res.data.role ?? 0),
      // 登录成功后缓存头像，首页可直接回显。
      avatar: res.data.avatar || ""
    });

    router.push(resolvePostLoginPath(res.data.role));
  } catch (error) {
    errorMsg.value =
      error.response?.data?.message || error.message || "网络异常，请稍后重试";
  } finally {
    loading.value = false;
  }
}
</script>
