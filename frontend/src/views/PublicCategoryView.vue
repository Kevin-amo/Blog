<template>
  <main class="home-page site-shell">
    <header class="blog-navbar panel">
      <div class="blog-navbar-left">
        <button class="blog-logo" type="button" @click="goHome">WAVELOG BLOG</button>
        <nav class="blog-nav" aria-label="博客主导航">
          <button class="blog-nav-link" type="button" @click="goHome">首页</button>
          <button class="blog-nav-link active" type="button">分类</button>
          <button class="blog-nav-link" type="button" @click="goAbout">关于我</button>
        </nav>
      </div>
    </header>

    <section class="panel public-about-panel">
      <div class="section-head">
        <div>
          <p class="panel-title">分类说明</p>
          <p class="muted">分类浏览已集成在首页的“分类”栏目中。</p>
        </div>
      </div>
      <div class="about-copy">
        <p>点击顶部导航里的“分类”，即可在左侧分类列表中筛选文章。</p>
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

function goAbout() {
  router.push({ name: "public-about" });
}
</script>