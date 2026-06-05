<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="app">
    <header class="site-header">
      <div class="container header-inner">
        <RouterLink to="/" class="wordmark">
          <span class="crest" aria-hidden="true">✦</span>
          <span class="wordmark-text">유령선장 카이도쿠</span>
        </RouterLink>
        <nav aria-label="주요 메뉴">
          <RouterLink to="/">정박지</RouterLink>
        </nav>
      </div>
    </header>

    <main class="container site-main">
      <RouterView />
    </main>

    <footer class="site-footer">
      <div class="container">
        <p class="muted">안개 너머, 별을 좇는 유령선 · 씨미(CIME) 공식 팬카페</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.site-header {
  border-bottom: 1px solid var(--line);
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(10, 14, 39, 0.72);
  backdrop-filter: blur(8px);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 0;
}

.wordmark {
  display: inline-flex;
  align-items: center;
  gap: 0.6rem;
  color: var(--gold-bright);
}

.crest {
  color: var(--gold);
  font-size: 1.1rem;
}

.wordmark-text {
  font-family: var(--font-head);
  font-size: 1.4rem;
  font-weight: 600;
  letter-spacing: 0.04em;
}

nav a {
  font-size: 0.9rem;
  letter-spacing: 0.04em;
}

.site-main {
  flex: 1;
  padding-block: clamp(2rem, 1rem + 4vw, 4rem);
  width: min(1080px, 92vw);
}

.site-footer {
  border-top: 1px solid var(--line);
  padding-block: 1.5rem;
  text-align: center;
}
</style>
