<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  fetchDashboardStats,
  fetchAdminBoards, createBoard, updateBoard, deleteBoard,
  fetchAdminMembers, assignMemberGrade, changeMemberRole, changeMemberStatus,
  createGrade, updateGrade, deleteGrade, setLiveStatus,
} from '@/api/admin'
import { fetchGrades } from '@/api/grades'
import { fetchLiveStatus } from '@/api/live'
import { fetchBannerLinks, updateBannerLinks } from '@/api/banner'
import { fetchBranding, updateLogo } from '@/api/branding'
import { uploadPostImage } from '@/api/posts'
import { HttpError } from '@/api/http'
import type {
  BannerItem,
  BoardAdmin, BoardCreateRequest, BoardType, DashboardStats, Grade, GradeInput, LiveOverrideMode, LiveStatus,
  MemberAdmin, MemberStatus, Role,
} from '@/api/types'

// 게시판 형식 라벨(목록 렌더링 방식). 글 데이터는 동일하고 표시만 달라진다.
const BOARD_TYPES: { value: BoardType; label: string }[] = [
  { value: 'GENERAL', label: '일반 (목록)' },
  { value: 'GALLERY', label: '갤러리 (이미지 그리드)' },
  { value: 'CARD', label: '카드 (썸네일+요약)' },
  { value: 'VIDEO', label: '영상 (16:9 썸네일)' },
  { value: 'LETTER', label: '편지 (편지지 카드)' },
  { value: 'RANK', label: '랭킹 (좋아요순)' },
]
const boardTypeLabel = (t: BoardType): string =>
  BOARD_TYPES.find((x) => x.value === t)?.label ?? t

type Tab = 'dashboard' | 'boards' | 'grades' | 'members' | 'live' | 'banner' | 'branding'
const tab = ref<Tab>('dashboard')
const forbidden = ref(false)
const loadError = ref<string | null>(null)
const banner = ref<string | null>(null)

const stats = ref<DashboardStats | null>(null)
const boards = ref<BoardAdmin[]>([])
const grades = ref<Grade[]>([])
const members = ref<MemberAdmin[]>([])
const live = ref<LiveStatus | null>(null)

const ROLES: Role[] = ['GUEST', 'MEMBER', 'ADMIN']
const STATUSES: MemberStatus[] = ['ACTIVE', 'SUSPENDED', 'WITHDRAWN']
const STATUS_LABELS: Record<MemberStatus, string> = {
  ACTIVE: '정상',
  SUSPENDED: '활동정지',
  WITHDRAWN: '강제탈퇴',
}

function emptyBoard(): BoardCreateRequest {
  return { code: '', nameKr: '', nameEn: null, description: null, sortOrder: 0, type: 'GENERAL', writeRole: 'MEMBER', categories: [] }
}
const boardForm = ref<BoardCreateRequest>(emptyBoard())
const editingBoardId = ref<number | null>(null)
const boardVisible = ref(true)
// 말머리는 쉼표 구분 텍스트로 입력 → 제출 시 string[]로 변환.
const boardCategoriesText = ref('')
function parseCategories(text: string): string[] {
  return text.split(',').map((s) => s.trim()).filter((s) => s.length > 0)
}

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

// 배너: 이름+링크+아이콘 자유 추가. 빈 행 1개를 기본 제공.
const bannerItems = ref<BannerItem[]>([])
const bannerIconBusy = ref<number | null>(null) // 아이콘 업로드 중인 행 인덱스
function addBannerRow(): void {
  bannerItems.value.push({ label: '', url: '', iconUrl: null })
}
function removeBannerRow(i: number): void {
  bannerItems.value.splice(i, 1)
}
async function onPickBannerIcon(i: number, event: Event): Promise<void> {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  bannerIconBusy.value = i
  try {
    const { url } = await uploadPostImage(file)
    bannerItems.value[i].iconUrl = url
    notify('아이콘을 올렸습니다. 저장을 눌러 반영하세요.')
  } catch (e: unknown) {
    notify(e instanceof HttpError ? e.message : '아이콘 업로드에 실패했습니다.')
  } finally {
    bannerIconBusy.value = null
    input.value = ''
  }
}
function clearBannerIcon(i: number): void {
  bannerItems.value[i].iconUrl = null
}

// 로고
const logoUrl = ref<string | null>(null)
const logoBusy = ref(false)
const logoInput = ref<HTMLInputElement | null>(null)

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
    // 호출들은 서로 독립 — 병렬 로딩.
    const [statsData, boardList, memberList, gradeList, liveStatus, bannerList, branding] = await Promise.all([
      fetchDashboardStats(),
      fetchAdminBoards(),
      fetchAdminMembers(),
      fetchGrades(),
      fetchLiveStatus(),
      fetchBannerLinks(),
      fetchBranding(),
    ])
    stats.value = statsData
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
    bannerItems.value = bannerList.length > 0 ? bannerList : [{ label: '', url: '', iconUrl: null }]
    logoUrl.value = branding.logoUrl
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
    const categories = parseCategories(boardCategoriesText.value)
    if (editingBoardId.value === null) {
      await createBoard({ ...boardForm.value, categories })
      notify('게시판을 추가했습니다.')
    } else {
      await updateBoard(editingBoardId.value, { ...boardForm.value, categories, visible: boardVisible.value })
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
    sortOrder: b.sortOrder, type: b.type, writeRole: b.writeRole, categories: b.categories,
  }
  boardCategoriesText.value = b.categories.join(', ')
}
function resetBoardForm(): void {
  editingBoardId.value = null
  boardVisible.value = true
  boardForm.value = emptyBoard()
  boardCategoriesText.value = ''
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
  // 정지·탈퇴는 즉시 로그아웃되는 강한 제재 → 실수 방지 확인. (취소 시 select 원복 위해 목록 재로딩)
  if (status !== 'ACTIVE' && !window.confirm(`${m.nickname} 님을 '${STATUS_LABELS[status]}' 처리할까요? 즉시 로그아웃되며 로그인이 차단됩니다.`)) {
    members.value = await fetchAdminMembers()
    return
  }
  try {
    await changeMemberStatus(m.id, status)
    members.value = await fetchAdminMembers()
    notify(`${m.nickname} 상태를 '${STATUS_LABELS[status]}'(으)로 변경했습니다.`)
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

// ── 외부링크 배너 ──
async function submitBanner(): Promise<void> {
  try {
    // 이름·링크 둘 다 있는 행만 저장(빈 행 무시).
    const items = bannerItems.value
      .map((it) => ({ label: it.label.trim(), url: it.url.trim(), iconUrl: it.iconUrl }))
      .filter((it) => it.label !== '' && it.url !== '')
    const saved = await updateBannerLinks(items)
    bannerItems.value = saved.length > 0 ? saved : [{ label: '', url: '', iconUrl: null }]
    notify('외부링크 배너를 저장했습니다.')
  } catch (e: unknown) {
    notify(errOf(e))
  }
}

// ── 로고 ──
async function onPickLogo(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  logoBusy.value = true
  try {
    const { url } = await uploadPostImage(file) // 업로드 → 경로 확보
    const res = await updateLogo(url)           // 경로를 로고로 저장
    logoUrl.value = res.logoUrl
    notify('로고를 저장했습니다.')
  } catch (e: unknown) {
    notify(e instanceof HttpError ? e.message : '로고 업로드에 실패했습니다.')
  } finally {
    logoBusy.value = false
    if (logoInput.value) logoInput.value.value = ''
  }
}
async function resetLogo(): Promise<void> {
  if (!confirm('로고를 기본 엠블럼으로 되돌릴까요?')) return
  try {
    const res = await updateLogo(null)
    logoUrl.value = res.logoUrl
    notify('기본 엠블럼으로 되돌렸습니다.')
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
        <button :class="{ active: tab === 'dashboard' }" @click="tab = 'dashboard'">대시보드</button>
        <button :class="{ active: tab === 'boards' }" @click="tab = 'boards'">게시판</button>
        <button :class="{ active: tab === 'grades' }" @click="tab = 'grades'">등급</button>
        <button :class="{ active: tab === 'members' }" @click="tab = 'members'">회원</button>
        <button :class="{ active: tab === 'live' }" @click="tab = 'live'">라이브</button>
        <button :class="{ active: tab === 'banner' }" @click="tab = 'banner'">배너</button>
        <button :class="{ active: tab === 'branding' }" @click="tab = 'branding'">로고</button>
      </nav>

      <!-- 대시보드 -->
      <section v-if="tab === 'dashboard'" class="panel section">
        <h2 class="section-title">운영 현황</h2>
        <div v-if="stats" class="stat-grid">
          <article class="stat-card primary">
            <p class="stat-label">전체 회원</p>
            <p class="stat-value">{{ stats.totalMembers.toLocaleString() }}</p>
            <p class="stat-sub">정상 {{ stats.activeMembers }} · 정지 {{ stats.suspendedMembers }} · 탈퇴 {{ stats.withdrawnMembers }}</p>
          </article>
          <article class="stat-card primary">
            <p class="stat-label">전체 게시글</p>
            <p class="stat-value">{{ stats.totalPosts.toLocaleString() }}</p>
            <p class="stat-sub">댓글 {{ stats.totalComments.toLocaleString() }}개</p>
          </article>
          <article class="stat-card">
            <p class="stat-label">최근 7일 신규 가입</p>
            <p class="stat-value">{{ stats.newMembers7d.toLocaleString() }}</p>
          </article>
          <article class="stat-card">
            <p class="stat-label">최근 7일 새 글</p>
            <p class="stat-value">{{ stats.newPosts7d.toLocaleString() }}</p>
          </article>
          <article class="stat-card">
            <p class="stat-label">오늘 출석</p>
            <p class="stat-value">{{ stats.attendanceToday.toLocaleString() }}</p>
          </article>
          <article class="stat-card">
            <p class="stat-label">게시판 / 등급</p>
            <p class="stat-value">{{ stats.totalBoards }} <span class="stat-divider">/</span> {{ stats.totalGrades }}</p>
          </article>
        </div>
      </section>

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
            <label>형식
              <select v-model="boardForm.type">
                <option v-for="t in BOARD_TYPES" :key="t.value" :value="t.value">{{ t.label }}</option>
              </select>
            </label>
            <label>작성권한
              <select v-model="boardForm.writeRole"><option value="MEMBER">회원</option><option value="ADMIN">관리자</option></select>
            </label>
            <label v-if="editingBoardId !== null" class="check">
              <input v-model="boardVisible" type="checkbox" /> 노출
            </label>
          </div>
          <div class="row">
            <label>말머리(쉼표로 구분)
              <input v-model="boardCategoriesText" placeholder="공지, 질문, 자유" />
            </label>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn">{{ editingBoardId === null ? '추가' : '저장' }}</button>
            <button v-if="editingBoardId !== null" type="button" class="btn ghost" @click="resetBoardForm">취소</button>
          </div>
        </form>

        <table class="data">
          <thead><tr><th>정렬</th><th>이름</th><th>코드</th><th>형식</th><th>노출</th><th></th></tr></thead>
          <tbody>
            <tr v-for="b in boards" :key="b.id">
              <td>{{ b.sortOrder }}</td>
              <td>{{ b.nameKr }}</td>
              <td class="mono">{{ b.code }}</td>
              <td>{{ boardTypeLabel(b.type) }}</td>
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
                  <option v-for="s in STATUSES" :key="s" :value="s">{{ STATUS_LABELS[s] }}</option>
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

      <!-- 외부링크 배너 -->
      <section v-if="tab === 'banner'" class="panel section">
        <form class="grid-form" @submit.prevent="submitBanner">
          <h2 class="section-title">외부링크 배너</h2>
          <p class="muted hint">홈 화면(게시판↔출석 사이)에 큰 버튼으로 노출됩니다. 이름과 링크를 자유롭게 추가하세요. 빈 칸은 저장되지 않습니다.</p>
          <div v-for="(item, i) in bannerItems" :key="i" class="row banner-row">
            <label>이름<input v-model="item.label" maxlength="30" placeholder="유튜브" /></label>
            <label>링크<input v-model="item.url" placeholder="https://..." /></label>
            <div class="banner-icon-cell">
              <span class="field-cap">아이콘</span>
              <div class="banner-icon-controls">
                <span class="banner-icon-prev">
                  <img v-if="item.iconUrl" :src="item.iconUrl" alt="" />
                  <span v-else class="muted tiny">기본</span>
                </span>
                <label class="link-btn upload-btn">
                  {{ bannerIconBusy === i ? '올리는 중…' : '업로드' }}
                  <input
                    type="file"
                    accept="image/jpeg,image/png,image/webp,image/gif"
                    hidden
                    :disabled="bannerIconBusy === i"
                    @change="onPickBannerIcon(i, $event)"
                  />
                </label>
                <button v-if="item.iconUrl" type="button" class="link-btn danger" @click="clearBannerIcon(i)">해제</button>
              </div>
            </div>
            <button type="button" class="link-btn danger banner-del" @click="removeBannerRow(i)">행 삭제</button>
          </div>
          <div class="form-actions">
            <button type="button" class="btn ghost" @click="addBannerRow">+ 배너 추가</button>
            <button type="submit" class="btn">저장</button>
          </div>
        </form>
      </section>

      <!-- 로고 -->
      <section v-if="tab === 'branding'" class="panel section">
        <h2 class="section-title">사이트 로고</h2>
        <p class="muted hint">헤더 좌측에 표시됩니다. PNG·JPG·WEBP·GIF, 2MB 이하. 가로로 긴 이미지가 잘 어울립니다.</p>
        <div class="logo-preview">
          <img v-if="logoUrl" :src="logoUrl" alt="현재 로고" />
          <span v-else class="muted">현재: 기본 엠블럼</span>
        </div>
        <div class="form-actions">
          <label class="btn upload-btn">
            {{ logoBusy ? '업로드 중…' : '이미지 업로드' }}
            <input
              ref="logoInput"
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              hidden
              :disabled="logoBusy"
              @change="onPickLogo"
            />
          </label>
          <button v-if="logoUrl" type="button" class="btn ghost" @click="resetLogo">기본으로</button>
        </div>
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
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 0.9rem;
}
.stat-card {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 1.1rem 1.2rem;
  background: rgba(212, 175, 106, 0.04);
}
.stat-card.primary {
  border-color: var(--gold);
  background: rgba(212, 175, 106, 0.1);
}
.stat-label {
  margin: 0;
  font-size: 0.78rem;
  letter-spacing: 0.04em;
  color: var(--gold-dim);
}
.stat-value {
  margin: 0.45rem 0 0;
  font-size: 2rem;
  line-height: 1.1;
  color: var(--gold-bright);
}
.stat-divider {
  color: var(--gold-dim);
  font-size: 1.4rem;
}
.stat-sub {
  margin: 0.55rem 0 0;
  font-size: 0.78rem;
  color: var(--gold-dim);
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
.banner-row {
  align-items: flex-end;
}
.banner-del {
  padding-bottom: 0.5rem;
  white-space: nowrap;
}
.banner-icon-cell {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}
.field-cap {
  font-size: 0.78rem;
  color: var(--gold-dim);
}
.banner-icon-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.banner-icon-prev {
  width: 30px;
  height: 30px;
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed var(--line);
  border-radius: 6px;
  overflow: hidden;
}
.banner-icon-prev img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.tiny {
  font-size: 0.62rem;
}
.logo-preview {
  display: flex;
  align-items: center;
  min-height: 60px;
  padding: 0.8rem 1rem;
  margin-bottom: 1rem;
  border: 1px dashed var(--line);
  border-radius: 8px;
  background: rgba(212, 175, 106, 0.04);
}
.logo-preview img {
  height: 48px;
  width: auto;
  max-width: 240px;
  object-fit: contain;
  display: block;
}
.upload-btn {
  cursor: pointer;
}
</style>
