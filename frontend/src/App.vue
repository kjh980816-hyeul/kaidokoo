<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { devMemberId, setDevMemberId } from '@/lib/devSession'

// 임시 dev 신원 전환(ADR-0003). 시드: 1=개발선원(MEMBER), 2=선장(ADMIN).
// ⚠️ 로그인 도입 시 이 전환 UI와 devSession을 제거한다.
function onDevSwitch(e: Event): void {
  setDevMemberId(Number((e.target as HTMLSelectElement).value))
}
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
          <RouterLink to="/admin">운영</RouterLink>
          <label class="dev-switch" title="임시 dev 로그인 (소셜 로그인 도입 전)">
            <span aria-hidden="true">⚓</span>
            <select :value="devMemberId" @change="onDevSwitch">
              <option :value="1">개발선원 (MEMBER)</option>
              <option :value="2">선장 (ADMIN)</option>
            </select>
          </label>
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

nav {
  display: inline-flex;
  align-items: center;
  gap: 1.1rem;
}

nav a {
  font-size: 0.9rem;
  letter-spacing: 0.04em;
}

.dev-switch {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.78rem;
  color: var(--gold-dim);
}

.dev-switch select {
  padding: 0.22rem 0.4rem;
  font-size: 0.78rem;
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
