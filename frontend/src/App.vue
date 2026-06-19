<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import CelestialBackdrop from '@/components/CelestialBackdrop.vue'
import Emblem from '@/components/Emblem.vue'
import { logout, loginUrl } from '@/api/auth'
import { fetchBannerLinks } from '@/api/banner'
import { useMe } from '@/composables/useMe'
import type { BannerLinks } from '@/api/types'

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

// 로그인 상태(세션 쿠키 기반, ADR-0004). 전역 공유 ref(헤더·마이페이지 공통).
const router = useRouter()
const { me, load: loadMe, clear: clearMe } = useMe()

// 사이드바 외부링크 배너. 실패해도 페이지를 막지 않는다(폴백 = 미노출).
const bannerLinks = ref<BannerLinks | null>(null)
const bannerItems = computed<{ label: string; url: string }[]>(() => {
  const b = bannerLinks.value
  if (!b) return []
  const defs: { label: string; url: string | null }[] = [
    { label: '유튜브', url: b.youtube },
    { label: 'X', url: b.x },
    { label: '씨미', url: b.seeme },
    { label: '팬심', url: b.fancim },
    { label: '팬심M', url: b.fancimM },
  ]
  return defs.filter((d): d is { label: string; url: string } => !!d.url && d.url.trim() !== '')
})

async function onLogout(): Promise<void> {
  try {
    await logout()
  } finally {
    clearMe()
    await router.push('/')
  }
}

onMounted(() => {
  runLoader()
  void loadMe()
  fetchBannerLinks()
    .then((links) => (bannerLinks.value = links))
    .catch(() => (bannerLinks.value = null))
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
        <span class="auth">
          <template v-if="me?.authenticated">
            <RouterLink to="/me" class="auth-me" title="마이페이지">
              <img v-if="me.avatarUrl" :src="me.avatarUrl" alt="" class="auth-avatar" />
              <span v-else class="auth-avatar auth-avatar--blank" aria-hidden="true"></span>
              <span class="auth-name">{{ me.nickname }}</span>
            </RouterLink>
            <button type="button" class="auth-btn" @click="onLogout">로그아웃</button>
          </template>
          <template v-else-if="me">
            <a class="auth-btn" :href="loginUrl('naver')">네이버 로그인</a>
            <a class="auth-btn" :href="loginUrl('google')">구글 로그인</a>
          </template>
        </span>
      </header>

      <div class="body">
        <aside class="sidenav" aria-label="주요 메뉴">
          <RouterLink to="/" class="sn-link">홈</RouterLink>
          <RouterLink :to="{ path: '/', hash: '#boards' }" class="sn-link">게시판</RouterLink>

          <div v-if="bannerItems.length > 0" class="sn-banner" aria-label="외부 링크">
            <a
              v-for="item in bannerItems"
              :key="item.label"
              :href="item.url"
              target="_blank"
              rel="noopener"
              class="sn-banner-link"
            >
              <span class="sn-banner-dot" aria-hidden="true">✦</span>{{ item.label }}
            </a>
          </div>

          <RouterLink :to="{ path: '/', hash: '#attend' }" class="sn-link">출석</RouterLink>
          <RouterLink v-if="me?.role === 'ADMIN'" to="/admin" class="sn-link">관리자</RouterLink>
        </aside>

        <main class="site-main">
          <RouterView />
        </main>
      </div>

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
  /* flex 컬럼 아이템 + margin:0 auto 조합은 stretch를 풀어 콘텐츠 폭으로
     줄어든다(페이지마다 폭이 달라짐). width:100%로 항상 가용 폭을 채워
     max-width(1320)가 상한이 되게 하고, margin auto는 중앙정렬만 담당. */
  width: 100%;
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
.auth-me {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  color: inherit;
}
.auth-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--gold-1);
  flex: none;
  background: var(--navy-900, #0a0e27);
}
.auth-avatar--blank {
  background: radial-gradient(circle at 50% 40%, var(--neb, #243b5e), var(--navy-900, #0a0e27));
}
.auth-name {
  color: var(--gold-2);
  font-size: 0.82rem;
  max-width: 9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s;
}
.auth-me:hover .auth-name {
  color: var(--ink-bright);
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

/* ── 본문 + 좌측 사이드바 ── */
.body {
  display: flex;
  gap: clamp(1.2rem, 3vw, 2.6rem);
  align-items: flex-start;
}
.sidenav {
  flex: none;
  width: 116px;
  /* 좌측 세로 중앙 고정: 스크롤해도 뷰포트 수직 중앙에 머문다.
     top:50vh로 붙고 translateY(-50%)로 자기 높이의 절반만큼 끌어올림. */
  position: sticky;
  top: 50vh;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  z-index: 20;
}
.sn-link {
  font-family: var(--serif);
  letter-spacing: 0.22em;
  text-transform: uppercase;
  font-size: 13px;
  color: var(--ink-body);
  padding: 0.6rem 0.7rem;
  border-left: 2px solid transparent;
  transition: color 0.3s, border-color 0.3s, background 0.3s;
}
.sn-link:hover,
.sn-link.router-link-active {
  color: var(--gold-2);
  border-left-color: var(--gold-1);
  background: rgba(201, 165, 92, 0.06);
}
/* 외부링크 배너(사이드바) */
.sn-banner {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
  margin: 0.3rem 0;
  padding: 0.4rem 0;
  border-top: 1px solid rgba(201, 165, 92, 0.18);
  border-bottom: 1px solid rgba(201, 165, 92, 0.18);
}
.sn-banner-link {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-family: var(--kr-serif, serif);
  font-size: 12px;
  letter-spacing: 0.08em;
  color: var(--ink-faint);
  padding: 0.34rem 0.7rem;
  transition: color 0.3s;
}
.sn-banner-link:hover {
  color: var(--gold-2);
}
.sn-banner-dot {
  font-size: 9px;
  color: var(--gold-1);
}
.site-main {
  flex: 1;
  min-width: 0;
  padding-block: clamp(1.5rem, 1rem + 3vw, 3.5rem);
}

@media (max-width: 760px) {
  .body {
    flex-direction: column;
    gap: 0;
  }
  .sidenav {
    flex-direction: row;
    flex-wrap: wrap;
    width: auto;
    position: static;
    /* 데스크톱 세로중앙 고정값 해제(가로 메뉴로 전환) */
    top: auto;
    transform: none;
    padding-top: 1rem;
    gap: 0.3rem;
    border-bottom: 1px solid var(--line);
    padding-bottom: 0.6rem;
  }
  .sn-link {
    border-left: none;
    border-bottom: 2px solid transparent;
  }
  .sn-link:hover,
  .sn-link.router-link-active {
    border-left-color: transparent;
    border-bottom-color: var(--gold-1);
  }
  .sn-banner {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 0.2rem 0.4rem;
    border: none;
    padding: 0;
    margin: 0;
  }
  .sn-banner-link {
    padding: 0.3rem 0.5rem;
  }
  .site-main {
    padding-block: clamp(1.2rem, 1rem + 2vw, 2.5rem);
  }
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
