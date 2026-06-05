<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { fetchPost } from '@/api/posts'
import type { PostDetail } from '@/api/types'
import { formatDateTime } from '@/lib/format'

const props = defineProps<{ id: string }>()

const post = ref<PostDetail | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    post.value = await fetchPost(Number(props.id))
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '글을 불러오지 못했습니다'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <p v-if="loading" class="muted">불러오는 중…</p>
  <p v-else-if="error" class="error">{{ error }}</p>

  <article v-else-if="post" class="post panel">
    <header class="post-header">
      <RouterLink :to="{ name: 'board', params: { code: post.boardCode } }" class="eyebrow back">
        ← {{ post.boardNameKr }}
      </RouterLink>
      <h1 class="post-title">{{ post.title }}</h1>
      <p class="post-meta muted">
        {{ post.authorNickname }} · {{ formatDateTime(post.createdAt) }} · 조회 {{ post.viewCount }}
      </p>
    </header>
    <div class="post-body">{{ post.content }}</div>
  </article>
</template>

<style scoped>
.post {
  padding: clamp(1.5rem, 1rem + 2vw, 2.6rem);
}
.post-header {
  border-bottom: 1px solid var(--line);
  padding-bottom: 1.2rem;
  margin-bottom: 1.5rem;
}
.back {
  display: inline-block;
  margin-bottom: 0.8rem;
}
.post-title {
  font-size: clamp(1.8rem, 1rem + 3vw, 2.6rem);
  font-style: italic;
}
.post-body {
  white-space: pre-wrap;
  line-height: 1.9;
  font-size: 1.05rem;
}
.error {
  color: #e8a0a0;
}
</style>
