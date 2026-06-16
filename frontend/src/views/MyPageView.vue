<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMe } from '@/composables/useMe'
import { updateNickname, uploadAvatar, removeAvatar } from '@/api/me'
import { loginUrl } from '@/api/auth'
import { HttpError } from '@/api/http'

const ACCEPT = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
const MAX_BYTES = 2 * 1024 * 1024

const { me, load, set } = useMe()

const nickname = ref('')
const savingName = ref(false)
const busyAvatar = ref(false)
const message = ref<{ kind: 'ok' | 'err'; text: string } | null>(null)

const fileInput = ref<HTMLInputElement | null>(null)
const pendingFile = ref<File | null>(null)
const preview = ref<string | null>(null)

const authed = computed(() => me.value?.authenticated === true)
const shownAvatar = computed(() => preview.value ?? me.value?.avatarUrl ?? null)

onMounted(async () => {
  if (me.value === null) await load()
  nickname.value = me.value?.nickname ?? ''
})

function flash(kind: 'ok' | 'err', text: string): void {
  message.value = { kind, text }
}

function errText(e: unknown, fallback: string): string {
  return e instanceof HttpError ? e.message : fallback
}

async function onSaveNickname(): Promise<void> {
  const next = nickname.value.trim()
  if (next.length < 2 || next.length > 20) {
    flash('err', '닉네임은 2~20자여야 합니다.')
    return
  }
  savingName.value = true
  try {
    set(await updateNickname(next))
    flash('ok', '닉네임을 변경했어요.')
  } catch (e) {
    flash('err', errText(e, '닉네임 변경에 실패했어요.'))
  } finally {
    savingName.value = false
  }
}

function onPickFile(event: Event): void {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (!ACCEPT.includes(file.type)) {
    flash('err', 'JPG·PNG·WEBP·GIF 이미지만 올릴 수 있어요.')
    return
  }
  if (file.size > MAX_BYTES) {
    flash('err', '이미지는 2MB 이하만 올릴 수 있어요.')
    return
  }
  clearPreview()
  pendingFile.value = file
  preview.value = URL.createObjectURL(file)
}

async function onUpload(): Promise<void> {
  if (!pendingFile.value) return
  busyAvatar.value = true
  try {
    set(await uploadAvatar(pendingFile.value))
    clearPending()
    flash('ok', '프로필 사진을 변경했어요.')
  } catch (e) {
    flash('err', errText(e, '업로드에 실패했어요.'))
  } finally {
    busyAvatar.value = false
  }
}

async function onRemove(): Promise<void> {
  busyAvatar.value = true
  try {
    set(await removeAvatar())
    clearPending()
    flash('ok', '프로필 사진을 제거했어요.')
  } catch (e) {
    flash('err', errText(e, '제거에 실패했어요.'))
  } finally {
    busyAvatar.value = false
  }
}

function clearPreview(): void {
  if (preview.value) URL.revokeObjectURL(preview.value)
  preview.value = null
}

function clearPending(): void {
  clearPreview()
  pendingFile.value = null
  if (fileInput.value) fileInput.value.value = ''
}
</script>

<template>
  <section class="mypage">
    <header class="mp-head">
      <h1 class="mp-title gold-text">마이페이지</h1>
      <p class="mp-sub">선원 정보를 관리하세요</p>
    </header>

    <p v-if="!authed && me" class="mp-guard">
      로그인이 필요한 화면이에요.
      <a class="mp-link" :href="loginUrl('naver')">네이버 로그인</a>
      <a class="mp-link" :href="loginUrl('google')">구글 로그인</a>
    </p>

    <div v-else-if="authed" class="mp-card">
      <p v-if="message" class="mp-msg" :class="message.kind">{{ message.text }}</p>

      <!-- 프로필 사진 -->
      <div class="mp-block">
        <h2 class="mp-block-title">프로필 사진</h2>
        <div class="mp-avatar-row">
          <img v-if="shownAvatar" :src="shownAvatar" alt="현재 프로필 사진" class="mp-avatar" />
          <span v-else class="mp-avatar mp-avatar--blank" aria-hidden="true"></span>

          <div class="mp-avatar-actions">
            <input
              ref="fileInput"
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              class="mp-file"
              @change="onPickFile"
            />
            <div class="mp-btn-row">
              <button
                type="button"
                class="mp-btn primary"
                :disabled="!pendingFile || busyAvatar"
                @click="onUpload"
              >
                {{ busyAvatar ? '처리 중…' : '사진 저장' }}
              </button>
              <button
                v-if="me?.avatarUrl"
                type="button"
                class="mp-btn"
                :disabled="busyAvatar"
                @click="onRemove"
              >
                제거
              </button>
            </div>
            <p class="mp-hint">JPG·PNG·WEBP·GIF · 2MB 이하</p>
          </div>
        </div>
      </div>

      <!-- 닉네임 -->
      <div class="mp-block">
        <h2 class="mp-block-title">닉네임</h2>
        <div class="mp-name-row">
          <input
            v-model="nickname"
            type="text"
            class="mp-input"
            maxlength="20"
            placeholder="2~20자"
            @keyup.enter="onSaveNickname"
          />
          <button type="button" class="mp-btn primary" :disabled="savingName" @click="onSaveNickname">
            {{ savingName ? '저장 중…' : '닉네임 저장' }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.mypage {
  max-width: 640px;
  margin: 0 auto;
}
.mp-head {
  text-align: center;
  margin-bottom: clamp(1.5rem, 4vw, 2.5rem);
}
.mp-title {
  font-family: var(--serif);
  font-size: clamp(1.8rem, 1rem + 3vw, 2.6rem);
  letter-spacing: 0.12em;
}
.mp-sub {
  font-family: var(--kr-serif);
  color: var(--ink-faint);
  letter-spacing: 0.2em;
  font-size: 0.82rem;
  margin-top: 0.5rem;
}
.mp-guard {
  text-align: center;
  color: var(--ink-body);
  display: flex;
  gap: 0.8rem;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
}
.mp-link {
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 0.35rem 0.9rem;
  color: var(--ink-body);
  font-size: 0.78rem;
}
.mp-link:hover {
  color: var(--gold-2);
  border-color: var(--gold-1);
}
.mp-card {
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: clamp(1.2rem, 3vw, 2rem);
  background: rgba(13, 27, 62, 0.35);
}
.mp-msg {
  border-radius: 8px;
  padding: 0.6rem 0.9rem;
  font-size: 0.85rem;
  margin-bottom: 1.2rem;
}
.mp-msg.ok {
  color: var(--gold-2);
  border: 1px solid var(--gold-1);
}
.mp-msg.err {
  color: #f3b0a0;
  border: 1px solid #a05040;
}
.mp-block + .mp-block {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid var(--line);
}
.mp-block-title {
  font-family: var(--serif);
  letter-spacing: 0.18em;
  text-transform: uppercase;
  font-size: 0.9rem;
  color: var(--ink-bright);
  margin-bottom: 1rem;
}
.mp-avatar-row {
  display: flex;
  gap: 1.3rem;
  align-items: flex-start;
  flex-wrap: wrap;
}
.mp-avatar {
  width: 92px;
  height: 92px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--gold-1);
  flex: none;
  background: var(--navy-900, #0a0e27);
}
.mp-avatar--blank {
  background: radial-gradient(circle at 50% 40%, var(--neb, #243b5e), var(--navy-900, #0a0e27));
}
.mp-avatar-actions {
  flex: 1;
  min-width: 200px;
}
.mp-file {
  font-size: 0.8rem;
  color: var(--ink-body);
  max-width: 100%;
}
.mp-btn-row {
  display: flex;
  gap: 0.6rem;
  margin-top: 0.8rem;
}
.mp-hint {
  font-size: 0.74rem;
  color: var(--ink-faint);
  margin-top: 0.6rem;
  letter-spacing: 0.04em;
}
.mp-name-row {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}
.mp-input {
  flex: 1;
  min-width: 180px;
  background: rgba(6, 9, 18, 0.6);
  border: 1px solid var(--line);
  border-radius: 8px;
  color: var(--ink-bright);
  padding: 0.55rem 0.85rem;
  font-size: 0.92rem;
}
.mp-input:focus {
  outline: none;
  border-color: var(--gold-1);
}
.mp-btn {
  border: 1px solid var(--line);
  border-radius: 999px;
  background: transparent;
  color: var(--ink-body);
  font-family: var(--serif);
  letter-spacing: 0.1em;
  font-size: 0.78rem;
  padding: 0.5rem 1.1rem;
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.3s, border-color 0.3s;
}
.mp-btn:hover:not(:disabled) {
  color: var(--gold-2);
  border-color: var(--gold-1);
}
.mp-btn.primary {
  border-color: var(--gold-1);
  color: var(--gold-2);
}
.mp-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
