<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { fetchPost, deletePost } from '@/api/posts'
import { fetchLikeStatus, toggleLike } from '@/api/likes'
import { fetchComments, createComment, deleteComment, toggleCommentLike } from '@/api/comments'
import type { Comment, LikeStatus, PostDetail } from '@/api/types'
import { useMe } from '@/composables/useMe'
import { formatDateTime } from '@/lib/format'
import { renderPostHtml } from '@/lib/sanitizeHtml'

const props = defineProps<{ id: string }>()
const postId = computed(() => Number(props.id))
const router = useRouter()
const { me } = useMe()

const post = ref<PostDetail | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const deleting = ref(false)

// 작성자 본인 또는 ADMIN에게만 삭제 버튼 노출(실제 권한은 서버 재검증).
const canDelete = computed(
  () =>
    !!post.value &&
    me.value?.authenticated === true &&
    (me.value.id === post.value.authorId || me.value.role === 'ADMIN'),
)

async function onDeletePost(): Promise<void> {
  if (!post.value || deleting.value) return
  if (!confirm('이 글을 삭제할까요? 되돌릴 수 없습니다.')) return
  deleting.value = true
  try {
    const boardCode = post.value.boardCode
    await deletePost(post.value.id)
    await router.push({ name: 'board', params: { code: boardCode } })
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '글을 삭제하지 못했습니다.'
  } finally {
    deleting.value = false
  }
}

const like = ref<LikeStatus>({ liked: false, likeCount: 0 })
const likeBusy = ref(false)
const likeError = ref<string | null>(null)

const comments = ref<Comment[]>([])
const newComment = ref('')
const newSecret = ref(false) // 비밀댓글(작성자·운영자만)
const replyTo = ref<number | null>(null)
const replyText = ref('')
const replySecret = ref(false)
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

async function load(): Promise<void> {
  loading.value = true
  error.value = null
  post.value = null
  comments.value = []
  like.value = { liked: false, likeCount: 0 }
  likeError.value = null
  commentError.value = null
  replyTo.value = null
  try {
    post.value = await fetchPost(postId.value)
    const [likeStatus] = await Promise.all([fetchLikeStatus(postId.value), loadComments()])
    like.value = likeStatus
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '글을 불러오지 못했습니다'
  } finally {
    loading.value = false
  }
}

// onMounted만 쓰면 글 간 이동(라우트 파라미터만 변경) 시 컴포넌트가 재사용돼 이전 글이 남는다.
watch(() => props.id, load, { immediate: true })

async function onToggleLike(): Promise<void> {
  if (likeBusy.value) return
  likeBusy.value = true
  likeError.value = null
  try {
    like.value = await toggleLike(postId.value)
  } catch (e: unknown) {
    likeError.value = e instanceof Error ? e.message : '좋아요 처리에 실패했습니다'
  } finally {
    likeBusy.value = false
  }
}

async function submitComment(parentId: number | null): Promise<void> {
  const text = parentId === null ? newComment.value : replyText.value
  if (!text.trim()) return
  const secret = parentId === null ? newSecret.value : replySecret.value
  commentBusy.value = true
  commentError.value = null
  try {
    await createComment(postId.value, { content: text.trim(), parentId, secret })
    await loadComments()
    if (parentId === null) {
      newComment.value = ''
      newSecret.value = false
    } else {
      replyText.value = ''
      replySecret.value = false
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
  replySecret.value = false
}

// 댓글 좋아요 토글. 응답으로 받은 값으로 해당 댓글 객체를 직접 갱신(반응형).
async function onToggleCommentLike(c: Comment): Promise<void> {
  try {
    const res = await toggleCommentLike(c.id)
    c.liked = res.liked
    c.likeCount = res.likeCount
  } catch (e: unknown) {
    commentError.value = e instanceof Error ? e.message : '좋아요 처리에 실패했습니다'
  }
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
        <h1 class="post-title">
          <span v-if="post.secret" class="secret-tag" title="비밀글">🔒 비밀글</span>
          {{ post.title }}
        </h1>
        <p class="post-meta muted">
          {{ post.authorNickname }} · {{ formatDateTime(post.createdAt) }} · 조회 {{ post.viewCount }}
        </p>
        <div v-if="canDelete" class="post-tools">
          <RouterLink :to="{ name: 'post-edit', params: { id: post.id } }" class="link-btn">글 수정</RouterLink>
          <button type="button" class="link-btn danger" :disabled="deleting" @click="onDeletePost">
            {{ deleting ? '삭제 중…' : '글 삭제' }}
          </button>
        </div>
      </header>
      <!-- 본문은 서버 정제 + 클라이언트 DOMPurify 2중 방어 후 렌더(댓글은 평문 유지) -->
      <div class="post-body" v-html="renderPostHtml(post.content)"></div>

      <div v-if="post.imageUrls.length" class="post-images">
        <img v-for="(url, i) in post.imageUrls" :key="url" :src="url" :alt="`첨부 이미지 ${i + 1}`" />
      </div>

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
      <p v-if="likeError" class="error like-error">{{ likeError }}</p>
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
            <span v-if="c.secret" class="secret-mini" title="비밀댓글">🔒 비밀</span>
            <span class="comment-date muted">{{ formatDateTime(c.createdAt) }}</span>
          </div>
          <p class="comment-body">{{ c.content }}</p>
          <div class="comment-actions">
            <button class="like-mini" :class="{ liked: c.liked }" @click="onToggleCommentLike(c)">
              <span aria-hidden="true">{{ c.liked ? '♥' : '♡' }}</span> {{ c.likeCount }}
            </button>
            <button class="link-btn" @click="toggleReply(c.id)">답글</button>
            <button class="link-btn danger" @click="onDeleteComment(c.id)">삭제</button>
          </div>

          <form v-if="replyTo === c.id" class="reply-form" @submit.prevent="submitComment(c.id)">
            <input v-model="replyText" type="text" maxlength="1000" placeholder="답글을 남겨주세요" />
            <label class="secret-check" title="작성자와 운영자만 볼 수 있어요">
              <input v-model="replySecret" type="checkbox" /> 🔒 비밀
            </label>
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
                <span v-if="r.secret" class="secret-mini" title="비밀댓글">🔒 비밀</span>
                <span class="comment-date muted">{{ formatDateTime(r.createdAt) }}</span>
              </div>
              <p class="comment-body">{{ r.content }}</p>
              <div class="comment-actions">
                <button class="like-mini" :class="{ liked: r.liked }" @click="onToggleCommentLike(r)">
                  <span aria-hidden="true">{{ r.liked ? '♥' : '♡' }}</span> {{ r.likeCount }}
                </button>
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
          <label class="secret-check" title="작성자와 운영자만 볼 수 있어요">
            <input v-model="newSecret" type="checkbox" /> 🔒 비밀댓글
          </label>
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
.post-tools {
  margin-top: 0.8rem;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}
.post-body {
  line-height: 1.9;
  font-size: 1.05rem;
  word-break: break-word;
}
/* HTML 본문 요소별 간격 (gold-navy 테마) */
.post-body :deep(p) {
  margin: 0 0 0.9em;
}
.post-body :deep(h1),
.post-body :deep(h2),
.post-body :deep(h3) {
  margin: 1.3em 0 0.5em;
}
.post-body :deep(h1) { font-size: 1.7rem; }
.post-body :deep(h2) { font-size: 1.5rem; }
.post-body :deep(h3) { font-size: 1.25rem; }
.post-body :deep(ul),
.post-body :deep(ol) {
  margin: 0 0 0.9em;
  padding-left: 1.6em;
}
.post-body :deep(li) {
  margin: 0.2em 0;
}
.post-body :deep(blockquote) {
  margin: 0 0 0.9em;
  padding: 0.2em 0 0.2em 1em;
  border-left: 3px solid var(--gold-1);
  color: var(--ink-body);
}
.post-body :deep(a) {
  color: var(--gold-3);
  text-decoration: underline;
}
.post-body :deep(a:hover) {
  color: var(--gold-2);
}
.post-body :deep(audio) {
  display: block;
  width: 100%;
  margin: 0.8em 0;
}
.post-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
}
/* 임베드 영상 16:9 반응형 */
.post-body :deep(iframe) {
  display: block;
  width: 100%;
  max-width: 100%;
  aspect-ratio: 16 / 9;
  height: auto;
  margin: 1em 0;
  border: 1px solid var(--line);
  border-radius: 8px;
}
.post-images {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1.6rem;
}
.post-images img {
  width: 100%;
  border-radius: 10px;
  border: 1px solid var(--line);
  display: block;
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
.like-error {
  text-align: center;
  margin-top: 0.5rem;
  font-size: 0.85rem;
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
.comment-actions {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  margin-top: 0.4rem;
}
.like-mini {
  background: none;
  border: none;
  color: var(--gold-dim);
  cursor: pointer;
  font-size: 0.8rem;
  padding: 0;
  display: inline-flex;
  align-items: center;
  gap: 0.2rem;
  transition: color var(--dur) var(--ease);
}
.like-mini:hover {
  color: var(--gold-bright);
}
.like-mini.liked {
  color: var(--gold-bright);
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
  align-items: center;
  justify-content: flex-end;
  gap: 0.9rem;
  margin-top: 0.6rem;
}
/* 비밀글/비밀댓글 표식 */
.secret-tag {
  display: inline-block;
  font-style: normal;
  font-size: 0.6em;
  letter-spacing: 0.04em;
  vertical-align: middle;
  color: var(--gold-bright);
  border: 1px solid var(--gold-dim);
  border-radius: 999px;
  padding: 0.12em 0.6em;
  margin-right: 0.5rem;
  white-space: nowrap;
}
.secret-mini {
  font-size: 0.68rem;
  letter-spacing: 0.02em;
  color: var(--gold-bright);
  border: 1px solid var(--gold-dim);
  border-radius: 999px;
  padding: 0.05rem 0.4rem;
}
.secret-check {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.8rem;
  color: var(--gold-dim);
  cursor: pointer;
  white-space: nowrap;
}
.secret-check input {
  width: auto;
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
