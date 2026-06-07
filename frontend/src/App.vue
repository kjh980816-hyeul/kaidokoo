<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import CelestialBackdrop from '@/components/CelestialBackdrop.vue'
import Emblem from '@/components/Emblem.vue'
import { devMemberId, setDevMemberId } from '@/lib/devSession'

// 임시 dev 신원 전환(ADR-0003). 시드: 1=개발선원(MEMBER), 2=선장(ADMIN).
// ⚠️ 로그인 도입 시 이 전환 UI와 devSession을 제거한다.
function onDevSwitch(e: Event): void {
  setDevMemberId(Number((e.target as HTMLSelectElement).value))
}
</script>

<template>
  <CelestialBackdrop />

  <div class="page">
    <div class="wrap">
      <header class="topbar">
        <RouterLink to="/" class="brandmark" aria-label="홈으로">
          <span class="bm-emblem"><Emblem /></span>
          <span class="bm-name gold-text">CAPTAIN'S STARCHART</span>
        </RouterLink>
        <nav class="nav" aria-label="주요 메뉴">
          <RouterLink to="/">홈</RouterLink>
          <RouterLink :to="{ path: '/', hash: '#boards' }">게시판</RouterLink>
          <RouterLink :to="{ path: '/', hash: '#attend' }">출석</RouterLink>
          <RouterLink to="/admin">관리자</RouterLink>
          <label class="dev-switch" title="임시 dev 로그인 (소셜 로그인 도입 전)">
            <span aria-hidden="true">⚓</span>
            <select :value="devMemberId" @change="onDevSwitch">
              <option :value="1">개발선원</option>
              <option :value="2">선장(ADMIN)</option>
            </select>
          </label>
        </nav>
      </header>

      <main class="site-main">
        <RouterView />
      </main>

      <footer class="foot">
        <div class="f-emblem"><Emblem /></div>
        <div class="f-name">CAPTAIN'S STARCHART</div>
        <div class="f-sub">별을 보고 항해하는 선장과 선원들의 밤하늘 · 씨미(CIME) 공식 팬카페</div>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.page > .wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* ── 상단 바 ── */
.topbar {
  position: relative;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: clamp(26px, 4vh, 42px) 0 0;
}
.brandmark {
  display: flex;
  align-items: center;
  gap: 14px;
  color: inherit;
}
.brandmark .bm-emblem {
  width: 38px;
  height: 38px;
  color: var(--gold-2);
  flex: none;
}
.brandmark .bm-name {
  font-family: var(--serif);
  font-weight: 600;
  letter-spacing: 0.34em;
  font-size: 16px;
  text-transform: uppercase;
  white-space: nowrap;
}
.nav {
  display: flex;
  align-items: center;
  gap: clamp(18px, 2.4vw, 34px);
  font-size: 12.5px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  font-family: var(--serif);
  font-weight: 500;
}
.nav a {
  color: var(--ink-body);
  position: relative;
  padding-bottom: 4px;
  white-space: nowrap;
  transition: color 0.35s;
}
.nav a::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  height: 1px;
  width: 0;
  background: var(--grad-gold);
  transition: width 0.4s var(--ease);
}
.nav a:hover,
.nav a.router-link-active {
  color: var(--ink-bright);
}
.nav a:hover::after {
  width: 100%;
}
.dev-switch {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.74rem;
  color: var(--gold-1);
  text-transform: none;
  letter-spacing: 0;
}
.dev-switch select {
  padding: 0.22rem 0.4rem;
  font-size: 0.74rem;
}

.site-main {
  flex: 1;
  padding-block: clamp(1.5rem, 1rem + 3vw, 3.5rem);
}

/* ── 푸터 ── */
.foot {
  text-align: center;
  padding: clamp(50px, 8vh, 90px) 0 clamp(40px, 6vh, 70px);
  margin-top: clamp(30px, 5vh, 50px);
}
.foot .f-emblem {
  width: 40px;
  height: 40px;
  color: var(--gold-1);
  margin: 0 auto 18px;
  opacity: 0.8;
}
.foot .f-name {
  font-family: var(--serif);
  letter-spacing: 0.4em;
  text-transform: uppercase;
  font-size: 13px;
  color: var(--ink-body);
}
.foot .f-sub {
  font-family: var(--kr-serif);
  font-size: 11.5px;
  color: var(--ink-faint);
  margin-top: 12px;
  letter-spacing: 0.2em;
}

@media (max-width: 760px) {
  .nav {
    gap: 14px;
    font-size: 11px;
  }
  .brandmark .bm-name {
    font-size: 13px;
    letter-spacing: 0.22em;
  }
}
@media (max-width: 560px) {
  .topbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.8rem;
  }
}
</style>
