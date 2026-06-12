<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  fetchAdminBoards, createBoard, updateBoard, deleteBoard,
  fetchAdminMembers, assignMemberGrade, changeMemberRole, changeMemberStatus,
  createGrade, updateGrade, deleteGrade, setLiveStatus,
} from '@/api/admin'
import { fetchGrades } from '@/api/grades'
import { fetchLiveStatus } from '@/api/live'
import { HttpError } from '@/api/http'
import type {
  BoardAdmin, BoardCreateRequest, Grade, GradeInput, LiveOverrideMode, LiveStatus,
  MemberAdmin, MemberStatus, Role,
} from '@/api/types'

type Tab = 'boards' | 'grades' | 'members' | 'live'
const tab = ref<Tab>('boards')
const forbidden = ref(false)
const loadError = ref<string | null>(null)
const banner = ref<string | null>(null)

const boards = ref<BoardAdmin[]>([])
const grades = ref<Grade[]>([])
const members = ref<MemberAdmin[]>([])
const live = ref<LiveStatus | null>(null)

const ROLES: Role[] = ['GUEST', 'MEMBER', 'ADMIN']
const STATUSES: MemberStatus[] = ['ACTIVE', 'SUSPENDED', 'WITHDRAWN']

function emptyBoard(): BoardCreateRequest {
  return { code: '', nameKr: '', nameEn: null, description: null, sortOrder: 0, type: 'GENERAL', writeRole: 'MEMBER' }
}
const boardForm = ref<BoardCreateRequest>(emptyBoard())
const editingBoardId = ref<number | null>(null)
const boardVisible = ref(true)

function emptyGrade(): GradeInput {
  return { name: '', sortOrder: 0, badgeColor: '#D4AF6A', isDefault: false }
}
const gradeForm = ref<GradeInput>(emptyGrade())
const editingGradeId = ref<number | null>(null)

const liveForm = ref({
  mode: 'FORCE_OFF' as LiveOverrideMode,
  title: '',
  streamUrl: '',
  channelId: '',
})

function notify(msg: string): void {
  banner.value = msg
  setTimeout(() => (banner.value = null), 2600)
}
function errOf(e: unknown): string {
  return e instanceof Error ? e.message : '요청에 실패했습니다'
}

async function loadAll(): Promise<void> {
  forbidden.value = false
  loadError.value = null
  try {
    // 네 호출은 서로 독립 — 병렬 로딩.
    const [boardList, memberList, gradeList, liveStatus] = await Promise.all([
      fetchAdminBoards(),
      fetchAdminMembers(),
      fetchGrades(),
      fetchLiveStatus(),
    ])
    boards.value = boardList
    members.value = memberList
    grades.value = gradeList
    live.value = liveStatus
    liveForm.value = {
      mode: liveStatus.mode,
      title: liveStatus.title ?? '',
      streamUrl: liveStatus.streamUrl ?? '',
      channelId: liveStatus.channelId ?? '',
    }
  } catch (e: unknown) {
    // 권한 문제(401/403)일 때만 패널을 잠근다. 일시적 오류는 재시도 가능해야 한다.
    if (e instanceof HttpError && (e.status === 401 || e.status === 403)) {
      forbidden.value = true
    } else {
      loadError.value = errOf(e)
    }
  }
}
onMounted(loadAll)

// ── 게시판 ──
async function submitBoard(): Promise<void> {
  try {
    if (editingBoardId.value === null) {
      await createBoard(boardForm.value)
      notify('게시판을 추가했습니다.')
    } else {
      await updateBoard(editingBoardId.value, { ...boardForm.value, visible: boardVisible.value })
      notify('게시판을 수정했습니다.')
    }
    resetBoardForm()
    boards.value = await fetchAdminBoards()
  } catch (e: unknown) {
    notify(errOf(e))
  }
}
function editBoard(b: BoardAdmin): void {
  editingBoardId.value = b.id
  boardVisible.value = b.visible
  boardForm.value = {
    code: b.code, nameKr: b.nameKr, nameEn: b.nameEn, description: b.description,
    sortOrder: b.sortOrder, type: b.type, writeRole: b.writeRole,
  }
}
function resetBoardForm(): void {
  editingBoardId.value = null
  boardVisible.value = true
  boardForm.value = emptyBoard()
}
async function removeBoard(b: BoardAdmin): Promise<void> {
  if (!confirm(`'${b.nameKr}' 게시판을 삭제할까요?`)) return
  try {
    await deleteBoard(b.id)
    boards.value = await fetchAdminBoards()
    notify('삭제했습니다.')
  } catch (e: unknown) {
    notify(errOf(e))
  }
}

// ── 등급 ──
async function submitGrade(): Promise<void> {
  try {
    if (editingGradeId.value === null) {
      await createGrade(gradeForm.value)
      notify('등급을 추가했습니다.')
    } else {
      await updateGrade(editingGradeId.value, gradeForm.value)
      notify('등급을 수정했습니다.')
    }
    editingGradeId.value = null
    gradeForm.value = emptyGrade()
    grades.value = await fetchGrades()
    members.value = await fetchAdminMembers()
  } catch (e: unknown) {
    notify(errOf(e))
  }
}
function editGrade(g: Grade): void {
  editingGradeId.value = g.id
  gradeForm.value = { name: g.name, sortOrder: g.sortOrder, badgeColor: g.badgeColor ?? '#D4AF6A', isDefault: g.isDefault }
}
async function removeGrade(g: Grade): Promise<void> {
  if (!confirm(`'${g.name}' 등급을 삭제할까요? 사용 중 회원은 기본 등급으로 이동합니다.`)) return
  try {
    await deleteGrade(g.id)
    grades.value = await fetchGrades()
    members.value = await fetchAdminMembers()
    notify('삭제했습니다.')
  } catch (e: unknown) {
    notify(errOf(e))
  }
}

// ── 회원 ──
async function onAssignGrade(m: MemberAdmin, value: string): Promise<void> {
  const gradeId = value === '' ? null : Number(value)
  try {
    await assignMemberGrade(m.id, gradeId)
    members.value = await fetchAdminMembers()
    notify(`${m.nickname} 등급을 변경했습니다.`)
  } catch (e: unknown) {
    notify(errOf(e))
  }
}
async function onChangeRole(m: MemberAdmin, role: Role): Promise<void> {
  try {
    await changeMemberRole(m.id, role)
    members.value = await fetchAdminMembers()
    notify(`${m.nickname} 권한을 ${role}로 변경했습니다.`)
  } catch (e: unknown) {
    notify(errOf(e))
  }
}
async function onChangeStatus(m: MemberAdmin, status: MemberStatus): Promise<void> {
  try {
    await changeMemberStatus(m.id, status)
    members.value = await fetchAdminMembers()
    notify(`${m.nickname} 상태를 ${status}로 변경했습니다.`)
  } catch (e: unknown) {
    notify(errOf(e))
  }
}

// ── 라이브 ──
async function submitLive(): Promise<void> {
  try {
    live.value = await setLiveStatus({
      mode: liveForm.value.mode,
      title: liveForm.value.title.trim() || null,
      streamUrl: liveForm.value.streamUrl.trim() || null,
      channelId: liveForm.value.channelId.trim() || null,
    })
    notify('라이브 배너 설정을 저장했습니다.')
  } catch (e: unknown) {
    notify(errOf(e))
  }
}
</script>

<template>
  <div class="admin">
    <header class="admin-head">
      <p class="eyebrow">Captain's Bridge</p>
      <h1 class="admin-title">운영 화면</h1>
    </header>

    <p v-if="banner" class="admin-banner">{{ banner }}</p>

    <p v-if="forbidden" class="panel notice">
      관리자(ADMIN) 권한이 필요합니다. 우측 상단 dev 전환에서 <strong>선장(ADMIN)</strong>으로 바꿔주세요.
      <br /><span class="muted">소셜 로그인 도입 전 임시 dev 인증입니다 (ADR-0003).</span>
    </p>

    <p v-else-if="loadError" class="panel notice">
      불러오지 못했습니다: {{ loadError }}
      <br /><button type="button" class="btn" style="margin-top: 0.8rem" @click="loadAll">다시 시도</button>
    </p>

    <template v-else>
      <nav class="tabs" aria-label="운영 메뉴">
        <button :class="{ active: tab === 'boards' }" @click="tab = 'boards'">게시판</button>
        <button :class="{ active: tab === 'grades' }" @click="tab = 'grades'">등급</button>
        <button :class="{ active: tab === 'members' }" @click="tab = 'members'">회원</button>
        <button :class="{ active: tab === 'live' }" @click="tab = 'live'">라이브</button>
      </nav>

      <!-- 게시판 -->
      <section v-if="tab === 'boards'" class="panel section">
        <form class="grid-form" @submit.prevent="submitBoard">
          <h2 class="section-title">{{ editingBoardId === null ? '새 게시판' : '게시판 수정' }}</h2>
          <div class="row">
            <label>코드(슬러그)
              <input v-model="boardForm.code" :disabled="editingBoardId !== null" placeholder="free" />
            </label>
            <label>한글명<input v-model="boardForm.nameKr" placeholder="정박지" /></label>
            <label>영문명<input v-model="boardForm.nameEn" placeholder="Harbor" /></label>
          </div>
          <div class="row">
            <label>설명<input v-model="boardForm.description" placeholder="선원들의 자유 게시판" /></label>
            <label>정렬<input v-model.number="boardForm.sortOrder" type="number" /></label>
            <label>타입
              <select v-model="boardForm.type"><option value="GENERAL">일반</option><option value="GALLERY">갤러리</option></select>
            </label>
            <label>작성권한
              <select v-model="boardForm.writeRole"><option value="MEMBER">회원</option><option value="ADMIN">관리자</option></select>
            </label>
            <label v-if="editingBoardId !== null" class="check">
              <input v-model="boardVisible" type="checkbox" /> 노출
            </label>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn">{{ editingBoardId === null ? '추가' : '저장' }}</button>
            <button v-if="editingBoardId !== null" type="button" class="btn ghost" @click="resetBoardForm">취소</button>
          </div>
        </form>

        <table class="data">
          <thead><tr><th>정렬</th><th>이름</th><th>코드</th><th>타입</th><th>노출</th><th></th></tr></thead>
          <tbody>
            <tr v-for="b in boards" :key="b.id">
              <td>{{ b.sortOrder }}</td>
              <td>{{ b.nameKr }}</td>
              <td class="mono">{{ b.code }}</td>
              <td>{{ b.type === 'GALLERY' ? '갤러리' : '일반' }}</td>
              <td>{{ b.visible ? '✓' : '–' }}</td>
              <td class="actions">
                <button class="link-btn" @click="editBoard(b)">수정</button>
                <button class="link-btn danger" @click="removeBoard(b)">삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- 등급 -->
      <section v-if="tab === 'grades'" class="panel section">
        <form class="grid-form" @submit.prevent="submitGrade">
          <h2 class="section-title">{{ editingGradeId === null ? '새 등급' : '등급 수정' }}</h2>
          <div class="row">
            <label>등급명<input v-model="gradeForm.name" placeholder="갑판원" /></label>
            <label>정렬(서열)<input v-model.number="gradeForm.sortOrder" type="number" /></label>
            <label>뱃지색<input v-model="gradeForm.badgeColor" type="color" /></label>
            <label class="check"><input v-model="gradeForm.isDefault" type="checkbox" /> 기본 등급</label>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn">{{ editingGradeId === null ? '추가' : '저장' }}</button>
            <button v-if="editingGradeId !== null" type="button" class="btn ghost"
              @click="editingGradeId = null; gradeForm = emptyGrade()">취소</button>
          </div>
        </form>

        <table class="data">
          <thead><tr><th>서열</th><th>등급</th><th>기본</th><th></th></tr></thead>
          <tbody>
            <tr v-for="g in grades" :key="g.id">
              <td>{{ g.sortOrder }}</td>
              <td><span class="grade-badge" :style="{ '--badge': g.badgeColor || 'var(--gold)' }">{{ g.name }}</span></td>
              <td>{{ g.isDefault ? '★' : '' }}</td>
              <td class="actions">
                <button class="link-btn" @click="editGrade(g)">수정</button>
                <button class="link-btn danger" @click="removeGrade(g)">삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- 회원 -->
      <section v-if="tab === 'members'" class="panel section">
        <table class="data">
          <thead><tr><th>닉네임</th><th>가입</th><th>등급</th><th>권한</th><th>상태</th></tr></thead>
          <tbody>
            <tr v-for="m in members" :key="m.id">
              <td>{{ m.nickname }}</td>
              <td class="mono">{{ m.provider }}</td>
              <td>
                <select :value="m.gradeId ?? ''" @change="onAssignGrade(m, ($event.target as HTMLSelectElement).value)">
                  <option value="">(없음)</option>
                  <option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</option>
                </select>
              </td>
              <td>
                <select :value="m.role" @change="onChangeRole(m, ($event.target as HTMLSelectElement).value as Role)">
                  <option v-for="r in ROLES" :key="r" :value="r">{{ r }}</option>
                </select>
              </td>
              <td>
                <select :value="m.status" @change="onChangeStatus(m, ($event.target as HTMLSelectElement).value as MemberStatus)">
                  <option v-for="s in STATUSES" :key="s" :value="s">{{ s }}</option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- 라이브 -->
      <section v-if="tab === 'live'" class="panel section">
        <form class="grid-form" @submit.prevent="submitLive">
          <h2 class="section-title">라이브 배너</h2>
          <div class="row">
            <label>모드
              <select v-model="liveForm.mode">
                <option value="AUTO">자동 (씨미 폴링)</option>
                <option value="FORCE_ON">강제 ON</option>
                <option value="FORCE_OFF">강제 OFF</option>
              </select>
            </label>
            <label>씨미 채널 ID<input v-model="liveForm.channelId" placeholder="@kaiijoku" /></label>
          </div>
          <label>배너 제목<input v-model="liveForm.title" placeholder="오늘 별바다 항해 방송" /></label>
          <label>방송 링크<input v-model="liveForm.streamUrl" placeholder="https://ci.me/..." /></label>
          <p class="muted hint">
            ※ 자동 모드는 채널 ID가 있어야 동작하며 30~60초 간격으로 방송 여부를 감지합니다.
            제목은 자동 모드에서 방송 제목으로 갱신됩니다.
          </p>
          <div class="form-actions"><button type="submit" class="btn">저장</button></div>
        </form>
      </section>
    </template>
  </div>
</template>

<style scoped>
.admin-head {
  margin-bottom: 1.4rem;
}
.admin-title {
  font-size: 2rem;
  margin: 0.2rem 0 0;
}
.admin-banner {
  position: sticky;
  top: 4.5rem;
  z-index: 5;
  background: rgba(212, 175, 106, 0.16);
  border: 1px solid var(--gold);
  border-radius: 8px;
  padding: 0.6rem 1rem;
  margin-bottom: 1rem;
}
.notice {
  padding: 1.6rem;
  line-height: 1.7;
}
.tabs {
  display: flex;
  gap: 0.4rem;
  margin-bottom: 1.2rem;
  flex-wrap: wrap;
}
.tabs button {
  background: transparent;
  border: 1px solid var(--line);
  color: var(--gold-dim);
  padding: 0.45rem 1.1rem;
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all var(--dur) var(--ease);
}
.tabs button.active {
  border-color: var(--gold);
  color: var(--gold-bright);
  background: rgba(212, 175, 106, 0.1);
}
.section {
  padding: clamp(1.2rem, 1rem + 1.5vw, 2rem);
}
.section-title {
  font-size: 1.1rem;
  margin: 0 0 1rem;
}
.grid-form {
  margin-bottom: 1.6rem;
  border-bottom: 1px solid var(--line);
  padding-bottom: 1.4rem;
}
.row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
  margin-bottom: 0.8rem;
}
.grid-form label {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  font-size: 0.78rem;
  color: var(--gold-dim);
  flex: 1;
  min-width: 120px;
}
.grid-form label.check {
  flex-direction: row;
  align-items: center;
  gap: 0.4rem;
  min-width: auto;
  color: var(--text);
}
.grid-form label.check.big {
  font-size: 0.95rem;
  margin-bottom: 0.8rem;
}
.form-actions {
  display: flex;
  gap: 0.6rem;
}
.btn.ghost {
  background: transparent;
  border: 1px solid var(--line);
  color: var(--gold-dim);
}
.hint {
  font-size: 0.8rem;
  margin: 0 0 0.8rem;
}
.data {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}
.data th {
  text-align: left;
  color: var(--gold-dim);
  font-weight: 500;
  font-size: 0.76rem;
  letter-spacing: 0.06em;
  padding: 0.5rem 0.6rem;
  border-bottom: 1px solid var(--line);
}
.data td {
  padding: 0.55rem 0.6rem;
  border-bottom: 1px solid rgba(212, 175, 106, 0.08);
}
.data select {
  padding: 0.25rem 0.4rem;
  font-size: 0.85rem;
}
.mono {
  font-family: ui-monospace, monospace;
  color: var(--gold-dim);
  font-size: 0.82rem;
}
.actions {
  display: flex;
  gap: 0.7rem;
}
.link-btn {
  background: none;
  border: none;
  color: var(--gold-dim);
  cursor: pointer;
  font-size: 0.8rem;
  padding: 0;
}
.link-btn:hover {
  color: var(--gold-bright);
}
.link-btn.danger:hover {
  color: #e8a0a0;
}
.grade-badge {
  font-size: 0.72rem;
  padding: 0.12rem 0.5rem;
  border-radius: 999px;
  border: 1px solid var(--badge);
  color: var(--badge);
}
</style>
