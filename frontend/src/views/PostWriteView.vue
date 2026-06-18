<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { createPost, uploadPostImage } from '@/api/posts'
import { HttpError } from '@/api/http'

const props = defineProps<{ code: string }>()
const router = useRouter()

const ACCEPT = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
const MAX_BYTES = 2 * 1024 * 1024
const MAX_IMAGES = 20

const title = ref('')
const content = ref('')
const images = ref<string[]>([]) // 업로드 완료된 이미지 URL들
const submitting = ref(false)
const uploading = ref(false)
const error = ref<string | null>(null)

const fileInput = ref<HTMLInputElement | null>(null)

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
  if (!title.value.trim() || !content.value.trim()) {
    error.value = '제목과 본문을 모두 입력해주세요.'
    return
  }
  submitting.value = true
  try {
    const { id } = await createPost({
      boardCode: props.code,
      title: title.value.trim(),
      content: content.value.trim(),
      imageUrls: images.value,
    })
    await router.push({ name: 'post-detail', params: { id } })
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '글을 저장하지 못했습니다.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="write panel">
    <p class="eyebrow">/{{ code }} · 새 글</p>
    <h1 class="write-title">글쓰기</h1>

    <form class="write-form" @submit.prevent="submit">
      <label>
        <span class="field-label">제목</span>
        <input v-model="title" type="text" maxlength="200" placeholder="제목을 입력하세요" />
      </label>
      <label>
        <span class="field-label">본문</span>
        <textarea v-model="content" rows="12" placeholder="안개 너머로 띄울 이야기를 적어주세요" />
      </label>

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
          {{ submitting ? '등록 중…' : '등록' }}
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
