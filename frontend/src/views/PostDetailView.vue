<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { fetchPost } from '@/api/posts'
import { fetchLikeStatus, toggleLike } from '@/api/likes'
import { fetchComments, createComment, deleteComment } from '@/api/comments'
import type { Comment, LikeStatus, PostDetail } from '@/api/types'
import { formatDateTime } from '@/lib/format'

const props = defineProps<{ id: string }>()
const postId = computed(() => Number(props.id))

const post = ref<PostDetail | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

const like = ref<LikeStatus>({ liked: false, likeCount: 0 })
const likeBusy = ref(false)

const comments = ref<Comment[]>([])
const newComment = ref('')
const replyTo = ref<number | null>(null)
const replyText = ref('')
const commentBusy = ref(false)
const commentError = ref<string | null>(null)

// 최상위 댓글 + 부모별 대댓글(1단계)로 그룹화.
const roots = computed(() => comments.value.filter((c) => c.parentId === null))
function repliesOf(parentId: number): Comment[] {
  return comments.value.filter((c) => c.parentId === parentId)
}

async function loadComments(): Promise<void> {
  comments.value = await fetchComments(postId.value)
}

onMounted(async () => {
  try {
    post.value = await fetchPost(postId.value)
    const [likeStatus] = await Promise.all([fetchLikeStatus(postId.value), loadComments()])
    like.value = likeStatus
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '글을 불러오지 못했습니다'
  } finally {
    loading.value = false
  }
})

async function onToggleLike(): Promise<void> {
  if (likeBusy.value) return
  likeBusy.value = true
  try {
    like.value = await toggleLike(postId.value)
  } catch (e: unknown) {
    commentError.value = e instanceof Error ? e.message : '좋아요 처리에 실패했습니다'
  } finally {
    likeBusy.value = false
  }
}

async function submitComment(parentId: number | null): Promise<void> {
  const text = parentId === null ? newComment.value : replyText.value
  if (!text.trim()) return
  commentBusy.value = true
  commentError.value = null
  try {
    await createComment(postId.value, { content: text.trim(), parentId })
    await loadComments()
    if (parentId === null) {
      newComment.value = ''
    } else {
      replyText.value = ''
      replyTo.value = null
    }
  } catch (e: unknown) {
    commentError.value = e instanceof Error ? e.message : '댓글을 저장하지 못했습니다'
  } finally {
    commentBusy.value = false
  }
}

async function onDeleteComment(commentId: number): Promise<void> {
  if (!confirm('이 댓글을 삭제할까요?')) return
  try {
    await deleteComment(commentId)
    await loadComments()
  } catch (e: unknown) {
    commentError.value = e instanceof Error ? e.message : '댓글을 삭제하지 못했습니다'
  }
}

function toggleReply(commentId: number): void {
  replyTo.value = replyTo.value === commentId ? null : commentId
  replyText.value = ''
}
</script>

<template>
  <p v-if="loading" class="muted">불러오는 중…</p>
  <p v-else-if="error" class="error">{{ error }}</p>

  <template v-else-if="post">
    <article class="post panel">
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

      <div class="like-bar">
        <button
          class="like-btn"
          :class="{ liked: like.liked }"
          :disabled="likeBusy"
          @click="onToggleLike"
        >
          <span aria-hidden="true">{{ like.liked ? '★' : '☆' }}</span>
          좋아요 <strong>{{ like.likeCount }}</strong>
        </button>
      </div>
    </article>

    <section class="comments panel" aria-labelledby="comments-heading">
      <h2 id="comments-heading" class="comments-title">
        댓글 <span class="count">{{ comments.length }}</span>
      </h2>

      <p v-if="commentError" class="error">{{ commentError }}</p>

      <ul v-if="roots.length" class="comment-list">
        <li v-for="c in roots" :key="c.id" class="comment">
          <div class="comment-head">
            <span class="comment-author">{{ c.authorNickname }}</span>
            <span
              v-if="c.authorGradeName"
              class="grade-badge"
              :style="{ '--badge': c.authorGradeColor || 'var(--gold)' }"
            >{{ c.authorGradeName }}</span>
            <span class="comment-date muted">{{ formatDateTime(c.createdAt) }}</span>
          </div>
          <p class="comment-body" :class="{ deleted: c.deleted }">{{ c.content }}</p>
          <div v-if="!c.deleted" class="comment-actions">
            <button class="link-btn" @click="toggleReply(c.id)">답글</button>
            <button class="link-btn danger" @click="onDeleteComment(c.id)">삭제</button>
          </div>

          <form v-if="replyTo === c.id" class="reply-form" @submit.prevent="submitComment(c.id)">
            <input v-model="replyText" type="text" maxlength="1000" placeholder="답글을 남겨주세요" />
            <button type="submit" class="btn small" :disabled="commentBusy">등록</button>
          </form>

          <ul v-if="repliesOf(c.id).length" class="reply-list">
            <li v-for="r in repliesOf(c.id)" :key="r.id" class="comment reply">
              <div class="comment-head">
                <span class="reply-arrow" aria-hidden="true">↳</span>
                <span class="comment-author">{{ r.authorNickname }}</span>
                <span
                  v-if="r.authorGradeName"
                  class="grade-badge"
                  :style="{ '--badge': r.authorGradeColor || 'var(--gold)' }"
                >{{ r.authorGradeName }}</span>
                <span class="comment-date muted">{{ formatDateTime(r.createdAt) }}</span>
              </div>
              <p class="comment-body" :class="{ deleted: r.deleted }">{{ r.content }}</p>
              <div v-if="!r.deleted" class="comment-actions">
                <button class="link-btn danger" @click="onDeleteComment(r.id)">삭제</button>
              </div>
            </li>
          </ul>
        </li>
      </ul>
      <p v-else class="muted empty">첫 댓글을 남겨보세요.</p>

      <form class="comment-form" @submit.prevent="submitComment(null)">
        <textarea v-model="newComment" rows="3" maxlength="1000" placeholder="안개 너머로 한마디 남기기" />
        <div class="comment-form-actions">
          <button type="submit" class="btn" :disabled="commentBusy">댓글 등록</button>
        </div>
      </form>
    </section>
  </template>
</template>

<style scoped>
.post {
  padding: clamp(1.5rem, 1rem + 2vw, 2.6rem);
  margin-bottom: 1.4rem;
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
.like-bar {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
}
.like-btn {
  background: transparent;
  border: 1px solid var(--line);
  color: var(--text);
  border-radius: 999px;
  padding: 0.55rem 1.4rem;
  cursor: pointer;
  font-size: 0.95rem;
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  transition: border-color var(--dur) var(--ease), color var(--dur) var(--ease), background var(--dur) var(--ease);
}
.like-btn:hover {
  border-color: var(--gold);
}
.like-btn.liked {
  border-color: var(--gold);
  color: var(--gold-bright);
  background: rgba(212, 175, 106, 0.1);
}
.like-btn:disabled {
  opacity: 0.6;
  cursor: default;
}

.comments {
  padding: clamp(1.3rem, 1rem + 1.5vw, 2.2rem);
}
.comments-title {
  font-size: 1.3rem;
  margin: 0 0 1.2rem;
}
.count {
  color: var(--gold);
}
.comment-list,
.reply-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.comment {
  padding: 0.9rem 0;
  border-bottom: 1px solid rgba(212, 175, 106, 0.1);
}
.comment:last-child {
  border-bottom: none;
}
.comment-head {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.35rem;
  flex-wrap: wrap;
}
.comment-author {
  font-weight: 600;
  color: var(--gold-bright);
}
.grade-badge {
  font-size: 0.68rem;
  letter-spacing: 0.04em;
  padding: 0.1rem 0.45rem;
  border-radius: 999px;
  border: 1px solid var(--badge);
  color: var(--badge);
}
.comment-date {
  font-size: 0.78rem;
}
.comment-body {
  margin: 0;
  line-height: 1.7;
  white-space: pre-wrap;
}
.comment-body.deleted {
  color: var(--gold-dim);
  font-style: italic;
}
.comment-actions {
  display: flex;
  gap: 0.8rem;
  margin-top: 0.4rem;
}
.link-btn {
  background: none;
  border: none;
  color: var(--gold-dim);
  cursor: pointer;
  font-size: 0.78rem;
  padding: 0;
}
.link-btn:hover {
  color: var(--gold-bright);
}
.link-btn.danger:hover {
  color: #e8a0a0;
}
.reply-list {
  margin-top: 0.6rem;
  padding-left: 1.2rem;
  border-left: 1px solid rgba(212, 175, 106, 0.14);
}
.reply-arrow {
  color: var(--gold-dim);
}
.reply-form {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}
.reply-form input {
  flex: 1;
}
.comment-form {
  margin-top: 1.4rem;
}
.comment-form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 0.6rem;
}
.btn.small {
  padding: 0.4rem 0.9rem;
  font-size: 0.85rem;
}
.error {
  color: #e8a0a0;
}
.empty {
  padding: 1.2rem 0;
}
</style>
