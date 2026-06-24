<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createPost, updatePost, uploadPostImage, fetchPost } from '@/api/posts'
import { fetchBoards } from '@/api/boards'
import { useMe } from '@/composables/useMe'
import { HttpError } from '@/api/http'
import { renderPostHtml } from '@/lib/sanitizeHtml'
import RichEditor from '@/components/RichEditor.vue'

// 본문은 이제 HTML. 태그를 벗기고 공백만 남으면 빈 본문으로 간주한다.
function isContentEmpty(html: string): boolean {
  const text = html
    .replace(/<br\s*\/?>/gi, '')
    .replace(/&nbsp;/gi, ' ')
    .replace(/<[^>]*>/g, '')
    .replace(/\s+/g, '')
  return text.length === 0
}

// 작성 모드는 code(게시판), 수정 모드는 id(글)로 진입한다.
const props = defineProps<{ code?: string; id?: string }>()
const router = useRouter()

const ACCEPT = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
const MAX_BYTES = 2 * 1024 * 1024
const MAX_IMAGES = 20

const editing = computed(() => props.id != null)
const postId = computed(() => Number(props.id))

const { me } = useMe()
const isAdmin = computed(() => me.value?.role === 'ADMIN')

const title = ref('')
const content = ref('')
const images = ref<string[]>([]) // 업로드 완료된 이미지 URL들
const boardCode = ref<string>(props.code ?? '')
const categories = ref<string[]>([]) // 게시판이 정의한 말머리 목록
const category = ref<string>('') // 선택된 말머리('' = 말머리 없음)
const pinned = ref(false) // 공지 고정(ADMIN 전용)
const secret = ref(false) // 비밀글(작성자 본인·운영자만 열람)
const submitting = ref(false)
const uploading = ref(false)
const loading = ref(false)
const error = ref<string | null>(null)

const fileInput = ref<HTMLInputElement | null>(null)

// 게시판 메타(말머리 목록)를 코드로 조회. 작성·수정 양쪽에서 필요하다.
async function loadBoardCategories(code: string): Promise<void> {
  if (!code) return
  try {
    const boards = await fetchBoards()
    categories.value = boards.find((b) => b.code === code)?.categories ?? []
  } catch {
    categories.value = []
  }
}

onMounted(async () => {
  if (!editing.value) {
    await loadBoardCategories(boardCode.value)
    return
  }
  loading.value = true
  try {
    const post = await fetchPost(postId.value)
    title.value = post.title
    // 레거시 평문(태그 없는 \n 본문)은 그대로 TipTap에 넣으면 줄바꿈이 공백으로 뭉개진다.
    // 상세뷰와 같은 변환(평문→<br>, HTML은 정제)을 거쳐 "보이는 그대로" 편집되게 한다.
    content.value = renderPostHtml(post.content)
    images.value = [...post.imageUrls]
    boardCode.value = post.boardCode
    category.value = post.category ?? ''
    pinned.value = post.pinned
    secret.value = post.secret
    await loadBoardCategories(post.boardCode)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '글을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
})

async function onPickFiles(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  if (files.length === 0) return
  error.value = null
  uploading.value = true
  try {
    for (const file of files) {
      if (images.value.length >= MAX_IMAGES) {
        error.value = `이미지는 최대 ${MAX_IMAGES}장까지 올릴 수 있어요.`
        break
      }
      if (!ACCEPT.includes(file.type)) {
        error.value = 'JPG·PNG·WEBP·GIF 이미지만 올릴 수 있어요.'
        continue
      }
      if (file.size > MAX_BYTES) {
        error.value = '이미지는 2MB 이하만 올릴 수 있어요.'
        continue
      }
      const { url } = await uploadPostImage(file)
      images.value.push(url)
    }
  } catch (e: unknown) {
    error.value = e instanceof HttpError ? e.message : '이미지 업로드에 실패했어요.'
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

function removeImage(index: number): void {
  images.value.splice(index, 1)
}

async function submit(): Promise<void> {
  error.value = null
  if (!title.value.trim() || isContentEmpty(content.value)) {
    error.value = '제목과 본문을 모두 입력해주세요.'
    return
  }
  submitting.value = true
  try {
    const cat = category.value || null
    if (editing.value) {
      await updatePost(postId.value, {
        title: title.value.trim(),
        content: content.value,
        imageUrls: images.value,
        category: cat,
        pinned: pinned.value,
        secret: secret.value,
      })
      await router.push({ name: 'post-detail', params: { id: postId.value } })
    } else {
      const { id } = await createPost({
        boardCode: boardCode.value,
        title: title.value.trim(),
        content: content.value,
        imageUrls: images.value,
        category: cat,
        pinned: pinned.value,
        secret: secret.value,
      })
      await router.push({ name: 'post-detail', params: { id } })
    }
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '글을 저장하지 못했습니다.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="write panel">
    <p class="eyebrow">/{{ boardCode }} · {{ editing ? '글 수정' : '새 글' }}</p>
    <h1 class="write-title">{{ editing ? '글 수정' : '글쓰기' }}</h1>

    <p v-if="loading" class="muted">불러오는 중…</p>

    <form v-else class="write-form" @submit.prevent="submit">
      <label v-if="categories.length > 0">
        <span class="field-label">말머리</span>
        <select v-model="category" class="prefix-select">
          <option value="">말머리 없음</option>
          <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
        </select>
      </label>

      <label v-if="isAdmin" class="check-row">
        <input v-model="pinned" type="checkbox" />
        <span>공지로 고정</span>
      </label>

      <label class="check-row">
        <input v-model="secret" type="checkbox" />
        <span>🔒 비밀글 <span class="hint">· 작성자와 운영자만 볼 수 있어요</span></span>
      </label>

      <label>
        <span class="field-label">제목</span>
        <input v-model="title" type="text" maxlength="200" placeholder="제목을 입력하세요" />
      </label>
      <div class="field">
        <span class="field-label">본문</span>
        <RichEditor v-model="content" />
      </div>

      <div class="field">
        <span class="field-label">사진 첨부 <span class="hint">· JPG·PNG·WEBP·GIF · 2MB 이하 · 최대 {{ MAX_IMAGES }}장</span></span>
        <input
          ref="fileInput"
          type="file"
          accept="image/jpeg,image/png,image/webp,image/gif"
          multiple
          class="file"
          :disabled="uploading"
          @change="onPickFiles"
        />
        <p v-if="uploading" class="muted up-state">올리는 중…</p>

        <ul v-if="images.length" class="thumbs">
          <li v-for="(url, i) in images" :key="url" class="thumb">
            <img :src="url" alt="첨부 미리보기" />
            <button type="button" class="thumb-x" aria-label="이미지 제거" @click="removeImage(i)">×</button>
          </li>
        </ul>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <div class="actions">
        <button type="submit" class="btn" :disabled="submitting || uploading">
          {{ submitting ? '등록 중…' : editing ? '수정' : '등록' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.write {
  padding: clamp(1.5rem, 1rem + 2vw, 2.6rem);
}
.write-title {
  font-size: 2rem;
  margin-bottom: 1.4rem;
}
.write-form {
  display: flex;
  flex-direction: column;
  gap: 1.1rem;
}
.field-label {
  display: block;
  font-size: 0.8rem;
  letter-spacing: 0.1em;
  color: var(--gold-dim);
  margin-bottom: 0.4rem;
}
.hint {
  letter-spacing: 0;
  color: var(--gold-dim);
  opacity: 0.8;
}
.prefix-select {
  font-size: 0.95rem;
}
.check-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.92rem;
  color: var(--text);
  cursor: pointer;
}
.check-row input {
  width: auto;
}
.file {
  font-size: 0.85rem;
  color: var(--text);
}
.up-state {
  margin: 0.5rem 0 0;
  font-size: 0.85rem;
}
.thumbs {
  list-style: none;
  display: flex;
  flex-wrap: wrap;
  gap: 0.7rem;
  margin: 0.9rem 0 0;
  padding: 0;
}
.thumb {
  position: relative;
  width: 96px;
  height: 96px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--line);
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.thumb-x {
  position: absolute;
  top: 3px;
  right: 3px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: none;
  background: rgba(8, 12, 24, 0.75);
  color: #fff;
  font-size: 1rem;
  line-height: 1;
  cursor: pointer;
}
.thumb-x:hover {
  background: rgba(180, 60, 50, 0.9);
}
.actions {
  display: flex;
  justify-content: flex-end;
}
.error {
  color: #e8a0a0;
  margin: 0;
}
</style>
