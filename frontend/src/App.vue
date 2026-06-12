<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import CelestialBackdrop from '@/components/CelestialBackdrop.vue'
import Emblem from '@/components/Emblem.vue'
import { fetchMe, logout, loginUrl, type Me } from '@/api/auth'

const LOADER_MS = 2200 // 로딩 인트로 노출 시간(시안 톤)
const FADE_MS = 1000 // #loader.gone opacity 트랜지션 길이와 일치

// 로딩 인트로: 별을 읽어 항로를 정하는 연출. reduced-motion이면 건너뛴다.
const reduceMotion =
  typeof window !== 'undefined' &&
  window.matchMedia('(prefers-reduced-motion: reduce)').matches
const loaderGone = ref(reduceMotion) // true면 #loader DOM 제거
const booted = ref(reduceMotion) // true면 페이지 페이드인 + 로더 .gone
let showTimer = 0
let fadeTimer = 0

function runLoader(): void {
  window.clearTimeout(showTimer)
  window.clearTimeout(fadeTimer)
  if (reduceMotion) {
    loaderGone.value = true
    booted.value = true
    return
  }
  loaderGone.value = false
  booted.value = false
  showTimer = window.setTimeout(() => {
    booted.value = true // .gone → opacity 페이드 시작
    fadeTimer = window.setTimeout(() => (loaderGone.value = true), FADE_MS)
  }, LOADER_MS)
}

function replayLoader(): void {
  runLoader()
}

// 로그인 상태(세션 쿠키 기반, ADR-0004).
const router = useRouter()
const me = ref<Me | null>(null)

async function loadMe(): Promise<void> {
  try {
    me.value = await fetchMe()
  } catch {
    me.value = { authenticated: false }
  }
}

async function onLogout(): Promise<void> {
  try {
    await logout()
  } finally {
    me.value = { authenticated: false }
    await router.push('/')
  }
}

onMounted(() => {
  runLoader()
  void loadMe()
})
</script>

<template>
  <CelestialBackdrop />

  <div v-if="!loaderGone" id="loader" :class="{ gone: booted }">
    <div class="loader-inner">
      <div class="emblem-wrap">
        <span class="emblem-spin"><Emblem /></span>
        <svg class="loader-stars" viewBox="0 0 100 100" aria-hidden="true">
          <path class="st s1" transform="translate(86 16) scale(1.1)" d="M0 -5 L1 -1 L5 0 L1 1 L0 5 L-1 1 L-5 0 L-1 -1 Z" />
          <path class="st s2" transform="translate(15 22) scale(0.8)" d="M0 -5 L1 -1 L5 0 L1 1 L0 5 L-1 1 L-5 0 L-1 -1 Z" />
          <path class="st s3" transform="translate(88 84) scale(0.9)" d="M0 -5 L1 -1 L5 0 L1 1 L0 5 L-1 1 L-5 0 L-1 -1 Z" />
          <path class="st s4" transform="translate(14 80) scale(1)" d="M0 -5 L1 -1 L5 0 L1 1 L0 5 L-1 1 L-5 0 L-1 -1 Z" />
        </svg>
      </div>
      <div class="loading-text gold-text">Now Loading</div>
      <div class="loading-sub">별을 읽어 항로를 정하는 중…</div>
      <div class="load-bar"></div>
    </div>
  </div>

  <div class="page" :class="{ show: booted }">
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
          <RouterLink v-if="me?.role === 'ADMIN'" to="/admin">관리자</RouterLink>
          <span class="auth">
            <template v-if="me?.authenticated">
              <span class="auth-name">{{ me.nickname }}</span>
              <button type="button" class="auth-btn" @click="onLogout">로그아웃</button>
            </template>
            <template v-else-if="me">
              <a class="auth-btn" :href="loginUrl('naver')">네이버 로그인</a>
              <a class="auth-btn" :href="loginUrl('google')">구글 로그인</a>
            </template>
          </span>
        </nav>
      </header>

      <main class="site-main">
        <RouterView />
      </main>

      <footer class="foot">
        <div class="f-emblem"><Emblem /></div>
        <div class="f-name">CAPTAIN'S STARCHART</div>
        <div class="f-sub">별을 보고 항해하는 선장과 선원들의 밤하늘 · 씨미(CIME) 공식 팬카페</div>
        <button v-if="!reduceMotion" class="f-replay" type="button" @click="replayLoader">
          로딩 화면 다시 보기
        </button>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  opacity: 0;
  transition: opacity 1.1s ease 0.2s;
}
.page.show {
  opacity: 1;
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
.auth {
  display: inline-flex;
  align-items: center;
  gap: 0.7rem;
  text-transform: none;
  letter-spacing: 0.04em;
}
.auth-name {
  color: var(--gold-2);
  font-size: 0.82rem;
  max-width: 9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.auth-btn {
  background: transparent;
  border: 1px solid var(--line);
  border-radius: 999px;
  color: var(--ink-body);
  font-family: var(--serif);
  font-size: 0.72rem;
  letter-spacing: 0.12em;
  padding: 0.3rem 0.85rem;
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.3s, border-color 0.3s;
}
.auth-btn:hover {
  color: var(--gold-2);
  border-color: var(--gold-1);
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
.foot .f-replay {
  margin-top: 22px;
  background: none;
  border: none;
  cursor: pointer;
  white-space: nowrap;
  font-family: var(--serif);
  font-size: 11px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: var(--ink-faint);
  border-bottom: 1px solid rgba(201, 165, 92, 0.3);
  padding-bottom: 3px;
  transition: color 0.3s;
}
.foot .f-replay:hover {
  color: var(--gold-2);
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
