<template>
  <main class="auth-page">
    <div class="bg-orb orb-a"></div>
    <div class="bg-orb orb-b"></div>

    <section class="auth-card">
      <aside class="auth-intro">
        <p class="eyebrow">Wavelog Blog</p>
        <h1>创建你的账号，开始写作旅程。</h1>
        <p class="intro-text">
          注册后你就可以登录管理后台，发布文章、维护内容，构建属于你的博客空间。
        </p>
      </aside>

      <section class="auth-form-wrap">
        <h2>账号注册</h2>
        <p class="sub">仅需几步，马上开始创作</p>

        <form class="auth-form" @submit.prevent="handleRegister">
          <label>
            用户名
            <input
              v-model.trim="form.username"
              type="text"
              placeholder="3-20 位用户名"
              autocomplete="username"
            />
          </label>

          <label>
            昵称（可选）
            <input
              v-model.trim="form.nickname"
              type="text"
              placeholder="展示昵称，不填默认同用户名"
              autocomplete="nickname"
            />
          </label>

          <label>
            密码
            <input
              v-model="form.password"
              type="password"
              placeholder="6-20 位密码"
              autocomplete="new-password"
            />
          </label>

          <label>
            确认密码
            <input
              v-model="form.rePassword"
              type="password"
              placeholder="请再次输入密码"
              autocomplete="new-password"
            />
          </label>

          <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
          <p v-if="successMsg" class="success-msg">{{ successMsg }}</p>

          <button :disabled="loading" type="submit" class="submit-btn">
            {{ loading ? "注册中..." : "立即注册" }}
          </button>

          <p class="auth-switch">
            已有账号？
            <RouterLink to="/login">去登录</RouterLink>
          </p>
        </form>
      </section>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { registerApi } from "../api/auth";

const router = useRouter();

const form = reactive({
  username: "",
  nickname: "",
  password: "",
  rePassword: ""
});

const loading = ref(false);
const errorMsg = ref("");
const successMsg = ref("");

function validateForm() {
  if (!form.username || !form.password || !form.rePassword) {
    return "请填写用户名、密码和确认密码";
  }
  if (form.username.length < 3 || form.username.length > 20) {
    return "用户名长度需在 3-20 位之间";
  }
  if (form.password.length < 6 || form.password.length > 20) {
    return "密码长度需在 6-20 位之间";
  }
  if (form.password !== form.rePassword) {
    return "两次输入的密码不一致";
  }
  return "";
}

async function handleRegister() {
  const validateMsg = validateForm();
  if (validateMsg) {
    errorMsg.value = validateMsg;
    return;
  }

  loading.value = true;
  errorMsg.value = "";
  successMsg.value = "";

  try {
    const res = await registerApi({
      username: form.username,
      nickname: form.nickname,
      password: form.password,
      rePassword: form.rePassword
    });

    if (res.code !== 200) {
      errorMsg.value = res.message || "注册失败，请稍后重试";
      return;
    }

    successMsg.value = "注册成功，正在跳转登录页...";
    setTimeout(() => {
      router.replace({
        path: "/login",
        query: {
          registered: "1",
          username: form.username
        }
      });
    }, 800);
  } catch (error) {
    errorMsg.value =
      error.response?.data?.message || error.message || "网络异常，请稍后重试";
  } finally {
    loading.value = false;
  }
}
</script>
