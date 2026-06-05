<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { createPost } from '@/api/posts'

const props = defineProps<{ code: string }>()
const router = useRouter()

// TODO(P2-auth): 인증 도입 전 임시 작성자(개발 시드 회원 id=1). 로그인 후 세션에서 도출.
const DEV_AUTHOR_ID = 1

const title = ref('')
const content = ref('')
const submitting = ref(false)
const error = ref<string | null>(null)

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
      authorId: DEV_AUTHOR_ID,
      title: title.value.trim(),
      content: content.value.trim(),
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

      <p v-if="error" class="error">{{ error }}</p>

      <div class="actions">
        <button type="submit" class="btn" :disabled="submitting">
          {{ submitting ? '띄우는 중…' : '출항' }}
        </button>
      </div>
      <p class="muted note">※ 로그인 기능(P2) 도입 전이라 임시 작성자로 저장됩니다.</p>
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
.actions {
  display: flex;
  justify-content: flex-end;
}
.error {
  color: #e8a0a0;
  margin: 0;
}
.note {
  margin: 0;
}
</style>
