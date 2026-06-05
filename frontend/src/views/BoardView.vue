<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { fetchPosts } from '@/api/posts'
import type { PostSummary } from '@/api/types'
import { formatDateTime } from '@/lib/format'

const props = defineProps<{ code: string }>()

const posts = ref<PostSummary[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    posts.value = await fetchPosts(props.code)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '글을 불러오지 못했습니다'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="board-head">
    <div>
      <p class="eyebrow">/{{ code }}</p>
      <h1 class="board-title">게시판</h1>
    </div>
    <RouterLink :to="{ name: 'post-write', params: { code } }" class="btn">글쓰기</RouterLink>
  </div>

  <p v-if="loading" class="muted">불러오는 중…</p>
  <p v-else-if="error" class="error">{{ error }}</p>
  <p v-else-if="posts.length === 0" class="muted empty">아직 정박한 글이 없습니다. 첫 글을 띄워보세요.</p>

  <ul v-else class="post-list panel">
    <li v-for="post in posts" :key="post.id" class="post-row">
      <RouterLink :to="{ name: 'post-detail', params: { id: post.id } }" class="post-link">
        <span v-if="post.pinned" class="pin" aria-label="고정됨">✦</span>
        <span class="post-title">{{ post.title }}</span>
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
.pin {
  color: var(--gold);
}
.post-title {
  font-size: 1.02rem;
}
.post-meta {
  white-space: nowrap;
}
.error {
  color: #e8a0a0;
}
.empty {
  padding: 2rem 0;
}
@media (max-width: 640px) {
  .post-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.3rem;
  }
}
</style>
