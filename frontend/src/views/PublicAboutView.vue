<template>
  <main class="home-page site-shell">
    <header class="blog-navbar panel">
      <div class="blog-navbar-left">
        <button class="blog-logo" type="button" @click="goHome">WAVELOG BLOG</button>
        <nav class="blog-nav" aria-label="博客主导航">
          <button class="blog-nav-link" type="button" @click="goHome">首页</button>
          <button class="blog-nav-link" type="button" @click="goCategory">分类</button>
          <button class="blog-nav-link active" type="button">关于我</button>
        </nav>
      </div>
    </header>

    <section class="panel public-about-panel">
      <div class="section-head">
        <div>
          <p class="panel-title">关于我</p>
          <p class="muted">这里是博客作者的简短介绍。</p>
        </div>
      </div>
      <div class="about-copy">
        <p>你好，这里是 WAVELOG BLOG。</p>
        <p>我会在这里记录开发实践、学习笔记和阶段性总结。</p>
      </div>
    </section>
  </main>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

function goHome() {
  router.push({ name: "public-blog" });
}

function goCategory() {
  router.push({ name: "public-category" });
}
</script>