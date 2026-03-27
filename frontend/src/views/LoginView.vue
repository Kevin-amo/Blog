<template>
  <main class="auth-page">
    <div class="bg-orb orb-a"></div>
    <div class="bg-orb orb-b"></div>

    <section class="auth-card">
      <aside class="auth-intro">
        <p class="eyebrow">Wavelog Blog</p>
        <h1>写下你的灵感，分享你的世界。</h1>
        <p class="intro-text">
          这里是你的创作港湾。登录后，你可以管理博客、查看个人信息，并继续扩展文章发布与评论功能。
        </p>
      </aside>

      <section class="auth-form-wrap">
        <h2>欢迎回来</h2>
        <p class="sub">登录后进入你的博客控制台</p>

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

          <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

          <button :disabled="loading" type="submit" class="submit-btn">
            {{ loading ? "登录中..." : "立即登录" }}
          </button>
        </form>
      </section>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { loginApi } from "../api/auth";
import { setToken, setUserInfo } from "../utils/auth";

const router = useRouter();

const form = reactive({
  username: "",
  password: ""
});

const loading = ref(false);
const errorMsg = ref("");

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
      nickname: res.data.nickname
    });

    router.push("/admin");
  } catch (error) {
    errorMsg.value =
      error.response?.data?.message || error.message || "网络异常，请稍后重试";
  } finally {
    loading.value = false;
  }
}
</script>
