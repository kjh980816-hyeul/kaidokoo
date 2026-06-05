<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { fetchBoards } from '@/api/boards'
import type { Board } from '@/api/types'

const boards = ref<Board[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    boards.value = await fetchBoards()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '게시판을 불러오지 못했습니다'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="hero">
    <p class="eyebrow">Celestial Navigation × Ghost Ship</p>
    <h1>안개 너머, 별을 좇는 유령선</h1>
    <p class="muted hero-sub">유령선장 카이도쿠의 정박지에 오신 것을 환영합니다.</p>
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
