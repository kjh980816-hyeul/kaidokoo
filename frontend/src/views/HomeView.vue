<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { fetchBoards } from '@/api/boards'
import { fetchLiveStatus } from '@/api/live'
import { fetchAttendance, checkInAttendance } from '@/api/attendance'
import type { Attendance, Board, LiveStatus } from '@/api/types'

const boards = ref<Board[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const live = ref<LiveStatus | null>(null)
const attendance = ref<Attendance | null>(null)
const attendanceBusy = ref(false)

onMounted(async () => {
  try {
    boards.value = await fetchBoards()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '게시판을 불러오지 못했습니다'
  } finally {
    loading.value = false
  }
  // 라이브 배너·출석은 부가 정보 → 실패해도 페이지를 막지 않는다(폴백).
  fetchLiveStatus().then((s) => (live.value = s)).catch(() => (live.value = null))
  fetchAttendance().then((a) => (attendance.value = a)).catch(() => (attendance.value = null))
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
  <a
    v-if="live?.live"
    class="live-banner"
    :href="live.streamUrl || '#'"
    :target="live.streamUrl ? '_blank' : undefined"
    rel="noopener"
  >
    <span class="live-dot" aria-hidden="true"></span>
    <span class="live-label">LIVE</span>
    <span class="live-title">{{ live.title || '카이도쿠 선장 방송 중' }}</span>
    <span v-if="live.streamUrl" class="live-cta">보러 가기 →</span>
  </a>

  <section class="hero">
    <p class="eyebrow">Celestial Navigation × Ghost Ship</p>
    <h1>안개 너머, 별을 좇는 유령선</h1>
    <p class="muted hero-sub">유령선장 카이도쿠의 정박지에 오신 것을 환영합니다.</p>
  </section>

  <section v-if="attendance" class="attendance panel">
    <div class="attendance-info">
      <p class="eyebrow">출석부</p>
      <p class="attendance-streak">
        연속 <strong>{{ attendance.streak }}</strong>일 · 누적 {{ attendance.totalDays }}일
      </p>
    </div>
    <button class="btn" :disabled="attendance.checkedToday || attendanceBusy" @click="onCheckIn">
      {{ attendance.checkedToday ? '오늘 출석 완료 ✦' : '오늘 출석하기' }}
    </button>
  </section>

  <section aria-labelledby="boards-heading">
    <h2 id="boards-heading" class="section-title">항해 게시판</h2>

    <p v-if="loading" class="muted">불러오는 중…</p>
    <p v-else-if="error" class="error">{{ error }}</p>

    <div v-else class="board-grid">
      <RouterLink
        v-for="board in boards"
        :key="board.id"
        :to="{ name: 'board', params: { code: board.code } }"
        class="board-card panel"
      >
        <span class="board-type">{{ board.type === 'GALLERY' ? '갤러리' : '게시판' }}</span>
        <h3 class="board-name">{{ board.nameKr }}</h3>
        <p v-if="board.nameEn" class="board-en">{{ board.nameEn }}</p>
        <p v-if="board.description" class="muted board-desc">{{ board.description }}</p>
      </RouterLink>
    </div>
  </section>
</template>

<style scoped>
.live-banner {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.7rem 1.2rem;
  margin-bottom: 1.6rem;
  border: 1px solid var(--gold);
  border-radius: 12px;
  background: linear-gradient(100deg, rgba(212, 175, 106, 0.14), rgba(13, 27, 62, 0.4));
  color: var(--text);
  transition: box-shadow var(--dur) var(--ease), transform var(--dur) var(--ease);
}
.live-banner:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(212, 175, 106, 0.18);
}
.live-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #ff5d5d;
  box-shadow: 0 0 0 0 rgba(255, 93, 93, 0.6);
  animation: live-pulse 1.8s infinite;
}
@media (prefers-reduced-motion: reduce) {
  .live-dot {
    animation: none;
  }
}
@keyframes live-pulse {
  0% { box-shadow: 0 0 0 0 rgba(255, 93, 93, 0.55); }
  70% { box-shadow: 0 0 0 8px rgba(255, 93, 93, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 93, 93, 0); }
}
.live-label {
  font-family: var(--font-head);
  font-weight: 700;
  letter-spacing: 0.12em;
  color: #ff8a8a;
  font-size: 0.8rem;
}
.live-title {
  font-size: 0.98rem;
  flex: 1;
}
.live-cta {
  color: var(--gold-bright);
  font-size: 0.85rem;
  white-space: nowrap;
}

.attendance {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.4rem;
  margin-bottom: 1.8rem;
}
.attendance-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}
.attendance-streak {
  margin: 0;
  font-size: 1.05rem;
}
.attendance-streak strong {
  color: var(--gold-bright);
  font-size: 1.3rem;
}

.hero {
  text-align: center;
  padding-block: clamp(1rem, 2vw, 3rem) clamp(2rem, 4vw, 4rem);
}
.hero h1 {
  font-size: clamp(2.2rem, 1rem + 5vw, 4rem);
  font-style: italic;
}
.hero-sub {
  font-size: 1rem;
}
.section-title {
  font-size: 1.6rem;
  border-left: 2px solid var(--gold);
  padding-left: 0.6rem;
  margin-bottom: 1.4rem;
}
.board-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1.1rem;
}
.board-card {
  display: block;
  padding: 1.4rem 1.3rem;
  color: var(--text);
  transition: transform var(--dur) var(--ease), border-color var(--dur) var(--ease), box-shadow var(--dur) var(--ease);
}
.board-card:hover {
  transform: translateY(-3px);
  border-color: var(--gold);
  box-shadow: 0 10px 30px rgba(212, 175, 106, 0.16);
}
.board-type {
  font-size: 0.68rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--gold-dim);
}
.board-name {
  font-size: 1.5rem;
  margin: 0.4rem 0 0.1rem;
}
.board-en {
  font-family: var(--font-head);
  font-style: italic;
  color: var(--gold-dim);
  margin: 0 0 0.5rem;
}
.board-desc {
  margin: 0;
}
.error {
  color: #e8a0a0;
}
</style>
