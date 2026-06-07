<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import Emblem from '@/components/Emblem.vue'
import { fetchBoards } from '@/api/boards'
import { fetchLiveStatus } from '@/api/live'
import { fetchAttendance, checkInAttendance } from '@/api/attendance'
import type { Attendance, Board, LiveStatus } from '@/api/types'

const STAMP_TRACK = 14 // 출석 도장 트랙 칸 수 (2주)

const boards = ref<Board[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const live = ref<LiveStatus | null>(null)
const attendance = ref<Attendance | null>(null)
const attendanceBusy = ref(false)

// 연속 출석(streak)만큼 별을 점등. 트랙은 14칸 고정(시안의 도장 그리드 모티프).
const litCount = computed(() => Math.min(attendance.value?.streak ?? 0, STAMP_TRACK))
const stamps = computed(() =>
  Array.from({ length: STAMP_TRACK }, (_, i) => i < litCount.value),
)

onMounted(async () => {
  try {
    boards.value = await fetchBoards()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '게시판을 불러오지 못했습니다'
  } finally {
    loading.value = false
  }
  // 라이브 배너·출석은 부가 정보 → 실패해도 페이지를 막지 않는다(폴백).
  fetchLiveStatus()
    .then((s) => (live.value = s))
    .catch(() => (live.value = null))
  fetchAttendance()
    .then((a) => (attendance.value = a))
    .catch(() => (attendance.value = null))
})

async function onCheckIn(): Promise<void> {
  if (attendanceBusy.value) return
  attendanceBusy.value = true
  try {
    attendance.value = await checkInAttendance()
  } catch {
    // 비로그인 등 실패 시 조용히 무시(출석은 부가 기능)
  } finally {
    attendanceBusy.value = false
  }
}
</script>

<template>
  <!-- 라이브 배너 (beacon) — 항상 표시, 데이터 기반 ON/OFF -->
  <section class="live-banner" :class="{ on: live?.live }">
    <span class="beacon"><Emblem /></span>
    <div class="lb-text">
      <div class="lb-status">
        <span class="live-dot" aria-hidden="true"></span>
        <span>{{ live?.live ? 'ON AIR · 지금 항해 중' : 'OFF AIR · 정박 중' }}</span>
      </div>
      <div class="lb-sub">
        {{ live?.live ? live.title || '카이도쿠 선장 방송 중' : '다음 항해를 기다려 주세요' }}
      </div>
    </div>
    <a
      v-if="live?.live && live.streamUrl"
      class="lb-cta"
      :href="live.streamUrl"
      target="_blank"
      rel="noopener"
      >방송 보기</a
    >
  </section>

  <!-- 히어로 -->
  <section class="hero">
    <span class="floaty f1"><Emblem /></span>
    <span class="floaty f2"><Emblem /></span>
    <span class="floaty f3"><Emblem /></span>

    <div class="hero-emblem"><Emblem title="카이도쿠 팬카페 엠블럼" /></div>
    <p class="hero-eyebrow">Celestial Navigation Fan Club</p>
    <h1 class="hero-title gold-text">
      CAPTAIN'S<br />STARCHART<span class="kr">선장의 별바다 항해</span>
    </h1>
    <p class="hero-tagline">
      밤하늘을 나침반 삼는 선장의 항해 일지.<br />별빛 아래 모인 선원들의 기록을 남기는 곳.
    </p>
    <div class="hero-rule" aria-hidden="true">
      <span class="ln"></span>
      <span class="spark">✦</span>
      <span class="ln r"></span>
    </div>
  </section>

  <!-- 게시판 -->
  <section id="boards">
    <div class="sec-head">
      <div class="sh-emblem"><Emblem /></div>
      <h2 class="gold-text">항해 기록실</h2>
      <div class="sh-sub">The Boards · 선장이 운영하는 항로들</div>
    </div>

    <p v-if="loading" class="muted center">별을 읽어 항로를 정하는 중…</p>
    <p v-else-if="error" class="error center">{{ error }}</p>
    <p v-else-if="boards.length === 0" class="muted center">아직 개설된 항로가 없습니다.</p>

    <div v-else class="board-grid">
      <RouterLink
        v-for="(board, i) in boards"
        :key="board.id"
        :to="{ name: 'board', params: { code: board.code } }"
        class="board-card"
      >
        <div class="bc-top">
          <span class="bc-icon"><Emblem /></span>
          <span class="bc-no">No.{{ String(i + 1).padStart(2, '0') }}</span>
        </div>
        <h3 class="bc-name">
          {{ board.nameEn || board.nameKr }}
          <span class="kr">{{ board.nameKr }}</span>
        </h3>
        <p v-if="board.description" class="bc-desc">{{ board.description }}</p>
        <div class="bc-foot">
          <span>Enter ›</span>
          <span v-if="board.type === 'GALLERY'" class="bc-gallery-tag">Gallery</span>
        </div>
      </RouterLink>
    </div>
  </section>

  <!-- 출석 항해 도장 -->
  <section class="attend" id="attend">
    <div class="attend-head">
      <span class="ah-emblem"><Emblem /></span>
      <h3 class="gold-text">출석 항해 도장</h3>
      <div class="ah-sub">매일 별 하나를 점등해 이달의 항로를 완성하세요</div>
    </div>

    <div class="stamp-row">
      <span v-for="(lit, i) in stamps" :key="i" class="stamp" :class="{ lit }">
        <Emblem />
      </span>
    </div>

    <div class="attend-cta-row">
      <button
        class="stamp-btn"
        :disabled="!attendance || attendance.checkedToday || attendanceBusy"
        @click="onCheckIn"
      >
        {{ attendance?.checkedToday ? '오늘의 별 점등 완료 ✦' : '오늘의 별 점등하기' }}
      </button>
      <div v-if="attendance" class="attend-count">
        연속 {{ attendance.streak }}일 · 누적 {{ attendance.totalDays }}일
      </div>
    </div>
  </section>
</template>

<style scoped>
.center {
  text-align: center;
}
.error {
  color: #e8a0a0;
}

/* ── 라이브 배너 ── */
.live-banner {
  margin: clamp(20px, 3vh, 30px) 0 0;
  position: relative;
  z-index: 15;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 16px clamp(18px, 2.5vw, 26px);
  border: 1px solid rgba(201, 165, 92, 0.4);
  background: linear-gradient(90deg, rgba(13, 27, 62, 0.55), rgba(36, 59, 94, 0.25));
  backdrop-filter: blur(2px);
  color: var(--ink-body);
}
.live-banner .beacon {
  width: 30px;
  height: 30px;
  flex: none;
  color: var(--gold-2);
}
.live-banner .lb-text {
  flex: 1;
}
.live-banner .lb-status {
  font-family: var(--serif);
  letter-spacing: 0.34em;
  text-transform: uppercase;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--ink-bright);
}
.live-banner .lb-sub {
  font-size: 12.5px;
  color: var(--ink-faint);
  margin-top: 5px;
  letter-spacing: 0.04em;
}
.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--ink-faint);
}
.lb-cta {
  font-family: var(--serif);
  letter-spacing: 0.22em;
  text-transform: uppercase;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  flex: none;
  border: 1px solid rgba(201, 165, 92, 0.5);
  padding: 9px 18px;
  color: var(--gold-2);
  transition: all 0.35s;
}
.live-banner:hover .lb-cta {
  background: rgba(201, 165, 92, 0.12);
  color: var(--ink-bright);
}
.live-banner.on {
  border-color: rgba(232, 213, 160, 0.85);
  background: linear-gradient(90deg, rgba(36, 59, 94, 0.55), rgba(61, 90, 122, 0.35));
  box-shadow: 0 0 26px rgba(201, 165, 92, 0.22), 0 0 60px rgba(201, 165, 92, 0.1);
  animation: beaconPulse 2.6s ease-in-out infinite;
}
.live-banner.on .live-dot {
  background: var(--gold-2);
  box-shadow: 0 0 10px var(--gold-2), 0 0 18px var(--gold-1);
  animation: tw 1.4s ease-in-out infinite;
}
.live-banner.on .beacon {
  filter: drop-shadow(0 0 8px rgba(232, 213, 160, 0.6));
}
@keyframes beaconPulse {
  0%,
  100% {
    box-shadow: 0 0 22px rgba(201, 165, 92, 0.16), 0 0 50px rgba(201, 165, 92, 0.07);
  }
  50% {
    box-shadow: 0 0 34px rgba(232, 213, 160, 0.3), 0 0 80px rgba(201, 165, 92, 0.16);
  }
}
@keyframes tw {
  0%,
  100% {
    opacity: 0.4;
  }
  50% {
    opacity: 1;
  }
}

/* ── 히어로 ── */
.hero {
  position: relative;
  z-index: 10;
  text-align: center;
  padding: clamp(40px, 7vh, 84px) 0 clamp(34px, 5vh, 60px);
}
.hero-emblem {
  width: clamp(140px, 18vw, 188px);
  aspect-ratio: 1;
  margin: 0 auto clamp(22px, 3vh, 30px);
  color: var(--gold-2);
}
.hero-eyebrow {
  font-family: var(--serif);
  letter-spacing: 0.5em;
  text-transform: uppercase;
  font-size: clamp(10px, 1vw, 12px);
  color: var(--ink-faint);
  margin-bottom: 18px;
}
.hero-title {
  font-family: var(--serif);
  font-weight: 600;
  font-size: clamp(46px, 8vw, 104px);
  line-height: 0.98;
  letter-spacing: 0.06em;
  margin: 0;
}
.hero-title .kr {
  font-family: var(--kr-serif);
  display: block;
  font-size: clamp(20px, 3vw, 34px);
  letter-spacing: 0.5em;
  margin-top: 18px;
  color: var(--ink-body);
  font-weight: 400;
  text-indent: 0.5em;
  -webkit-text-fill-color: var(--ink-body);
}
.hero-tagline {
  margin: clamp(24px, 3.4vh, 34px) auto 0;
  max-width: 560px;
  font-family: var(--kr-serif);
  font-size: clamp(14px, 1.5vw, 17px);
  line-height: 2;
  letter-spacing: 0.12em;
  color: var(--ink-body);
}
.hero-rule {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin: clamp(26px, 3.6vh, 36px) auto 0;
  color: var(--gold-1);
}
.hero-rule .ln {
  width: clamp(40px, 8vw, 90px);
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--gold-1));
}
.hero-rule .ln.r {
  background: linear-gradient(90deg, var(--gold-1), transparent);
}
.hero-rule .spark {
  font-size: 14px;
  color: var(--gold-2);
}
.floaty {
  position: absolute;
  width: 22px;
  height: 22px;
  color: var(--gold-1);
  opacity: 0.45;
  animation: drift 12s ease-in-out infinite;
  pointer-events: none;
}
.floaty.f1 {
  top: 8%;
  left: 6%;
}
.floaty.f2 {
  top: 18%;
  right: 8%;
  animation-delay: -3s;
}
.floaty.f3 {
  bottom: 16%;
  left: 12%;
  animation-delay: -6s;
}
@keyframes drift {
  0%,
  100% {
    transform: translateY(0) rotate(0);
  }
  50% {
    transform: translateY(-16px) rotate(8deg);
  }
}

/* ── 게시판 그리드 ── */
.board-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: clamp(16px, 2vw, 24px);
}
.board-card {
  position: relative;
  display: block;
  color: var(--ink-body);
  padding: clamp(22px, 2.6vw, 30px);
  border: 1px solid rgba(201, 165, 92, 0.32);
  background: linear-gradient(160deg, rgba(13, 27, 62, 0.5), rgba(10, 14, 39, 0.35));
  overflow: hidden;
  transition: border-color 0.4s, transform 0.4s, box-shadow 0.4s;
}
.board-card::before {
  content: '';
  position: absolute;
  inset: 7px;
  border: 1px dashed rgba(201, 165, 92, 0.16);
  transition: border-color 0.4s;
  pointer-events: none;
}
.board-card:hover {
  border-color: rgba(232, 213, 160, 0.8);
  transform: translateY(-4px);
  box-shadow: 0 14px 40px rgba(0, 0, 0, 0.4), 0 0 24px rgba(201, 165, 92, 0.12);
}
.board-card:hover::before {
  border-color: rgba(201, 165, 92, 0.4);
}
.bc-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}
.bc-icon {
  width: 46px;
  height: 46px;
  color: var(--gold-2);
  flex: none;
}
.bc-no {
  font-family: var(--serif);
  font-size: 13px;
  letter-spacing: 0.2em;
  color: var(--ink-faint);
}
.bc-name {
  font-family: var(--serif);
  font-weight: 600;
  font-size: clamp(21px, 2.2vw, 26px);
  letter-spacing: 0.06em;
  color: var(--ink-bright);
  margin: 20px 0 0;
}
.bc-name .kr {
  font-family: var(--kr-serif);
  display: block;
  font-size: 15px;
  letter-spacing: 0.28em;
  color: var(--ink-body);
  margin-top: 7px;
  font-weight: 400;
}
.bc-desc {
  font-family: var(--kr-serif);
  font-size: 13px;
  line-height: 1.85;
  color: var(--ink-faint);
  margin: 14px 0 0;
  letter-spacing: 0.04em;
}
.bc-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(201, 165, 92, 0.18);
  font-size: 11.5px;
  letter-spacing: 0.14em;
  color: var(--ink-faint);
  font-family: var(--serif);
  text-transform: uppercase;
}
.bc-gallery-tag {
  font-family: var(--serif);
  font-size: 10px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: var(--bg-night);
  background: var(--grad-gold);
  padding: 3px 9px;
  border-radius: 1px;
}

/* ── 출석 위젯 ── */
.attend {
  margin-top: clamp(40px, 6vh, 70px);
  border: 1px solid rgba(201, 165, 92, 0.34);
  background: linear-gradient(160deg, rgba(13, 27, 62, 0.55), rgba(10, 14, 39, 0.4));
  padding: clamp(26px, 3.2vw, 40px);
  position: relative;
}
.attend::before {
  content: '';
  position: absolute;
  inset: 8px;
  border: 1px dashed rgba(201, 165, 92, 0.16);
  pointer-events: none;
}
.attend-head {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.attend-head .ah-emblem {
  width: 34px;
  height: 34px;
  color: var(--gold-2);
}
.attend-head h3 {
  font-family: var(--serif);
  font-weight: 600;
  font-size: clamp(22px, 2.6vw, 30px);
  letter-spacing: 0.16em;
  margin: 14px 0 0;
}
.attend-head .ah-sub {
  font-family: var(--kr-serif);
  font-size: 13px;
  letter-spacing: 0.2em;
  color: var(--ink-faint);
  margin-top: 10px;
}
.stamp-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: clamp(8px, 1.4vw, 16px);
  margin: clamp(24px, 3vh, 34px) auto 0;
  max-width: 760px;
}
.stamp {
  width: clamp(40px, 6vw, 54px);
  aspect-ratio: 1;
  flex: none;
  display: grid;
  place-items: center;
  position: relative;
  border: 1px solid rgba(201, 165, 92, 0.22);
  color: rgba(201, 165, 92, 0.28);
  transition: all 0.5s;
}
.stamp :deep(svg) {
  width: 56%;
  height: 56%;
}
.stamp.lit {
  color: var(--gold-2);
  border-color: rgba(232, 213, 160, 0.7);
  box-shadow: 0 0 14px rgba(201, 165, 92, 0.25);
}
.stamp.lit :deep(svg) {
  filter: drop-shadow(0 0 5px rgba(232, 213, 160, 0.6));
}
.attend-cta-row {
  text-align: center;
  margin-top: clamp(22px, 3vh, 30px);
}
.stamp-btn {
  font-family: var(--serif);
  letter-spacing: 0.26em;
  text-transform: uppercase;
  font-size: 12.5px;
  font-weight: 600;
  border: 1px solid rgba(201, 165, 92, 0.6);
  background: transparent;
  color: var(--gold-2);
  padding: 12px 30px;
  cursor: pointer;
  transition: all 0.35s;
  white-space: nowrap;
}
.stamp-btn:hover:not(:disabled) {
  background: rgba(201, 165, 92, 0.12);
  color: var(--ink-bright);
}
.stamp-btn:disabled {
  opacity: 0.45;
  cursor: default;
}
.attend-count {
  font-family: var(--kr-serif);
  font-size: 12.5px;
  color: var(--ink-faint);
  margin-top: 14px;
  letter-spacing: 0.16em;
}
</style>
