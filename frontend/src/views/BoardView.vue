<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { fetchBoards } from '@/api/boards'
import { fetchPosts } from '@/api/posts'
import type { BoardType, PostSummary } from '@/api/types'
import { formatDateTime } from '@/lib/format'

const props = defineProps<{ code: string }>()

const posts = ref<PostSummary[]>([])
const boardName = ref<string | null>(null)
const boardType = ref<BoardType>('GENERAL')
const loading = ref(true)
const error = ref<string | null>(null)

// RANK 형식만 좋아요순으로 보여준다(고추밭 동일). 나머지는 서버 정렬(고정글·최신순) 유지.
const displayPosts = computed(() => {
  if (boardType.value !== 'RANK') return posts.value
  return [...posts.value].sort((a, b) => b.likeCount - a.likeCount)
})
// 랭킹 막대 그래프 기준값(최대 좋아요).
const maxLikes = computed(() => Math.max(1, ...posts.value.map((p) => p.likeCount)))

// 작성 48시간 이내면 NEW 뱃지.
const NEW_MS = 1000 * 60 * 60 * 48
function isNew(createdAt: string): boolean {
  return Date.now() - new Date(createdAt).getTime() < NEW_MS
}

async function load(code: string): Promise<void> {
  loading.value = true
  error.value = null
  posts.value = []
  try {
    const [postList, boards] = await Promise.all([fetchPosts(code), fetchBoards()])
    posts.value = postList
    const board = boards.find((b) => b.code === code)
    boardName.value = board?.nameKr ?? null
    boardType.value = board?.type ?? 'GENERAL'
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '글을 불러오지 못했습니다'
  } finally {
    loading.value = false
  }
}

// onMounted만 쓰면 게시판 간 이동(라우트 파라미터만 변경) 시 컴포넌트가 재사용돼 갱신이 안 된다.
watch(() => props.code, load, { immediate: true })
</script>

<template>
  <div class="board-head">
    <div>
      <p class="eyebrow">/{{ code }}</p>
      <h1 class="board-title">{{ boardName ?? '게시판' }}</h1>
    </div>
    <RouterLink :to="{ name: 'post-write', params: { code } }" class="btn">글쓰기</RouterLink>
  </div>

  <p v-if="loading" class="muted">불러오는 중…</p>
  <p v-else-if="error" class="error">{{ error }}</p>
  <p v-else-if="posts.length === 0" class="muted empty">아직 정박한 글이 없습니다. 첫 글을 띄워보세요.</p>

  <!-- 갤러리: 정사각 썸네일 그리드 -->
  <ul v-else-if="boardType === 'GALLERY'" class="gallery-grid">
    <li v-for="post in displayPosts" :key="post.id">
      <RouterLink :to="{ name: 'post-detail', params: { id: post.id } }" class="gal-cell">
        <img v-if="post.thumbnailUrl" :src="post.thumbnailUrl" :alt="post.title" />
        <span v-else class="gal-blank" aria-hidden="true">✦</span>
        <span class="gal-cap">
          <span v-if="post.pinned" class="pin">✦</span>{{ post.title }}
          <span v-if="isNew(post.createdAt)" class="new-badge">NEW</span>
        </span>
      </RouterLink>
    </li>
  </ul>

  <!-- 카드 / 영상: 썸네일 + 요약 카드 -->
  <ul
    v-else-if="boardType === 'CARD' || boardType === 'VIDEO'"
    class="card-grid"
    :class="{ 'is-video': boardType === 'VIDEO' }"
  >
    <li v-for="post in displayPosts" :key="post.id" class="card">
      <RouterLink :to="{ name: 'post-detail', params: { id: post.id } }" class="card-link">
        <span class="card-thumb">
          <img v-if="post.thumbnailUrl" :src="post.thumbnailUrl" :alt="post.title" />
          <span v-else class="card-blank" aria-hidden="true">✦</span>
          <span v-if="boardType === 'VIDEO'" class="play" aria-hidden="true">▶</span>
        </span>
        <span class="card-body">
          <span class="card-title"><span v-if="post.pinned" class="pin">✦</span>{{ post.title }}<span v-if="isNew(post.createdAt)" class="new-badge">NEW</span></span>
          <span class="card-meta muted">
            {{ post.authorNickname }} · {{ formatDateTime(post.createdAt) }} · ♥ {{ post.likeCount }}
          </span>
        </span>
      </RouterLink>
    </li>
  </ul>

  <!-- 편지: 편지지 카드 -->
  <ul v-else-if="boardType === 'LETTER'" class="letter-grid">
    <li v-for="post in displayPosts" :key="post.id" class="letter">
      <RouterLink :to="{ name: 'post-detail', params: { id: post.id } }" class="letter-link">
        <span class="letter-pin" aria-hidden="true">✦</span>
        <span class="letter-title">{{ post.title }}<span v-if="isNew(post.createdAt)" class="new-badge">NEW</span></span>
        <span class="letter-meta muted">{{ post.authorNickname }}</span>
        <span class="letter-date muted">{{ formatDateTime(post.createdAt) }}</span>
      </RouterLink>
    </li>
  </ul>

  <!-- 랭킹: 좋아요순 + 막대 -->
  <ol v-else-if="boardType === 'RANK'" class="rank-list panel">
    <li v-for="(post, i) in displayPosts" :key="post.id" class="rank-row">
      <span class="rank-no" :class="{ top: i < 3 }">{{ i + 1 }}</span>
      <RouterLink :to="{ name: 'post-detail', params: { id: post.id } }" class="rank-main">
        <span class="rank-title">{{ post.title }}<span v-if="isNew(post.createdAt)" class="new-badge">NEW</span></span>
        <span class="rank-bar">
          <span class="rank-bar-fill" :style="{ width: (post.likeCount / maxLikes) * 100 + '%' }"></span>
        </span>
      </RouterLink>
      <span class="rank-likes">♥ {{ post.likeCount }}</span>
    </li>
  </ol>

  <!-- 일반(목록형) -->
  <ul v-else class="post-list panel">
    <li v-for="post in displayPosts" :key="post.id" class="post-row">
      <RouterLink :to="{ name: 'post-detail', params: { id: post.id } }" class="post-link">
        <span v-if="post.pinned" class="pin" aria-label="고정됨">✦</span>
        <img v-if="post.thumbnailUrl" :src="post.thumbnailUrl" alt="" class="row-thumb" />
        <span class="post-title">{{ post.title }}</span>
        <span v-if="isNew(post.createdAt)" class="new-badge">NEW</span>
      </RouterLink>
      <span class="post-meta muted">
        {{ post.authorNickname }} · {{ formatDateTime(post.createdAt) }} · 조회 {{ post.viewCount }}
      </span>
    </li>
  </ul>
</template>

<style scoped>
.board-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.4rem;
}
.board-title {
  font-size: 2rem;
  margin: 0.2rem 0 0;
}
.pin {
  color: var(--gold);
  margin-right: 0.35rem;
}
.new-badge {
  display: inline-block;
  margin-left: 0.4rem;
  vertical-align: middle;
  font-size: 0.6rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  line-height: 1;
  padding: 0.18rem 0.34rem;
  border-radius: 3px;
  color: var(--bg-night, #0a0e27);
  background: var(--grad-gold, var(--gold));
}
.error {
  color: #e8a0a0;
}
.empty {
  padding: 2rem 0;
}

/* ── 일반 목록 ── */
.post-list {
  list-style: none;
  margin: 0;
  padding: 0.4rem 0;
}
.post-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.85rem 1.3rem;
  border-bottom: 1px solid rgba(212, 175, 106, 0.12);
}
.post-row:last-child {
  border-bottom: none;
}
.post-link {
  color: var(--text);
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}
.post-link:hover {
  color: var(--gold-bright);
}
.row-thumb {
  width: 34px;
  height: 34px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--line);
}
.post-title {
  font-size: 1.02rem;
}
.post-meta {
  white-space: nowrap;
}

/* ── 갤러리 ── */
.gallery-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 0.9rem;
}
.gal-cell {
  position: relative;
  display: block;
  aspect-ratio: 1 / 1;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--line);
  background: rgba(13, 27, 62, 0.4);
}
.gal-cell img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.4s var(--ease);
}
.gal-cell:hover img {
  transform: scale(1.05);
}
.gal-blank {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  color: var(--gold-dim);
  font-size: 1.6rem;
}
.gal-cap {
  position: absolute;
  inset: auto 0 0 0;
  padding: 1.4rem 0.7rem 0.5rem;
  font-size: 0.82rem;
  color: #fff;
  background: linear-gradient(transparent, rgba(6, 9, 18, 0.85));
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ── 카드 / 영상 ── */
.card-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 1.1rem;
}
.card {
  border: 1px solid var(--line);
  border-radius: 12px;
  overflow: hidden;
  background: rgba(13, 27, 62, 0.35);
  transition: border-color var(--dur) var(--ease), transform var(--dur) var(--ease);
}
.card:hover {
  border-color: var(--gold);
  transform: translateY(-3px);
}
.card-link {
  display: block;
  color: var(--text);
}
.card-thumb {
  position: relative;
  display: block;
  aspect-ratio: 4 / 3;
  background: rgba(6, 9, 18, 0.5);
}
.is-video .card-thumb {
  aspect-ratio: 16 / 9;
}
.card-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.card-blank {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  color: var(--gold-dim);
  font-size: 1.6rem;
}
.play {
  position: absolute;
  inset: 0;
  margin: auto;
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: rgba(6, 9, 18, 0.6);
  color: #fff;
  font-size: 1.1rem;
}
.card-body {
  display: block;
  padding: 0.8rem 0.95rem 1rem;
}
.card-title {
  display: block;
  font-size: 1rem;
  margin-bottom: 0.4rem;
}
.card-meta {
  font-size: 0.78rem;
}

/* ── 편지 ── */
.letter-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.1rem;
}
.letter-link {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
  padding: 1.6rem 1.2rem 1.2rem;
  min-height: 140px;
  border-radius: 4px;
  color: var(--text);
  background: linear-gradient(160deg, rgba(36, 59, 94, 0.55), rgba(13, 27, 62, 0.55));
  border: 1px solid var(--line);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.25);
  transition: transform var(--dur) var(--ease);
}
.letter-link:hover {
  transform: translateY(-3px) rotate(-0.6deg);
}
.letter-pin {
  position: absolute;
  top: 0.5rem;
  left: 50%;
  transform: translateX(-50%);
  color: var(--gold);
}
.letter-title {
  font-family: var(--kr-serif, serif);
  font-size: 1.05rem;
  line-height: 1.5;
}
.letter-meta {
  font-size: 0.82rem;
}
.letter-date {
  font-size: 0.72rem;
  margin-top: auto;
}

/* ── 랭킹 ── */
.rank-list {
  list-style: none;
  margin: 0;
  padding: 0.5rem 0;
  counter-reset: rank;
}
.rank-row {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  padding: 0.7rem 1.3rem;
  border-bottom: 1px solid rgba(212, 175, 106, 0.12);
}
.rank-row:last-child {
  border-bottom: none;
}
.rank-no {
  flex: none;
  width: 1.9rem;
  text-align: center;
  font-family: var(--serif, serif);
  font-size: 1.1rem;
  color: var(--gold-dim);
}
.rank-no.top {
  color: var(--gold-bright);
  font-weight: 700;
}
.rank-main {
  flex: 1;
  min-width: 0;
  color: var(--text);
}
.rank-title {
  display: block;
  margin-bottom: 0.35rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rank-main:hover .rank-title {
  color: var(--gold-bright);
}
.rank-bar {
  display: block;
  height: 5px;
  border-radius: 999px;
  background: rgba(212, 175, 106, 0.14);
  overflow: hidden;
}
.rank-bar-fill {
  display: block;
  height: 100%;
  background: var(--grad-gold, var(--gold));
}
.rank-likes {
  flex: none;
  font-size: 0.85rem;
  color: var(--gold-dim);
  white-space: nowrap;
}

@media (max-width: 640px) {
  .post-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.3rem;
  }
}
</style>
