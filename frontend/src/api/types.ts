// 백엔드 DTO와 1:1 대응하는 클라이언트 타입. enum 대신 string literal union(ECC TS 룰).

export type BoardType = 'GENERAL' | 'GALLERY' | 'CARD' | 'VIDEO' | 'LETTER' | 'RANK'

export interface Board {
  id: number
  code: string
  nameKr: string
  nameEn: string | null
  description: string | null
  type: BoardType
  sortOrder: number
  categories: string[]
  hasNew: boolean
}

export interface PostSummary {
  id: number
  title: string
  authorNickname: string
  viewCount: number
  likeCount: number
  pinned: boolean
  secret: boolean
  category: string | null
  thumbnailUrl: string | null
  createdAt: string
}

export interface PostDetail {
  id: number
  boardCode: string
  boardNameKr: string
  title: string
  content: string
  authorId: number
  authorNickname: string
  viewCount: number
  likeCount: number
  pinned: boolean
  secret: boolean
  category: string | null
  imageUrls: string[]
  createdAt: string
  updatedAt: string
}

// 작성자는 서버가 세션에서 도출한다 — 본문에 authorId 없음. imageUrls는 업로드 후 받은 경로.
export interface PostCreateRequest {
  boardCode: string
  title: string
  content: string
  imageUrls: string[]
  category: string | null
  pinned: boolean
  secret: boolean
}

// 글 수정 본문. category·pinned 포함(말머리·고정공지 반영).
export interface PostUpdateRequest {
  title: string
  content: string
  imageUrls: string[]
  category: string | null
  pinned: boolean
  secret: boolean
}

// 외부링크 배너 항목(관리자가 이름+링크+아이콘 자유 추가). iconUrl이 null이면 기본 엠블럼.
export interface BannerItem {
  label: string
  url: string
  iconUrl: string | null
}

// 사이트 브랜딩(헤더 로고). logoUrl이 null이면 기본 엠블럼.
export interface Branding {
  logoUrl: string | null
}

export type Role = 'GUEST' | 'MEMBER' | 'ADMIN'
export type MemberStatus = 'ACTIVE' | 'SUSPENDED' | 'WITHDRAWN'
export type Provider = 'NAVER' | 'KAKAO' | 'GOOGLE'

export interface Comment {
  id: number
  parentId: number | null
  content: string
  authorNickname: string
  authorGradeName: string | null
  authorGradeColor: string | null
  likeCount: number
  liked: boolean
  secret: boolean
  createdAt: string
}

export interface CommentCreateRequest {
  content: string
  parentId: number | null
  secret: boolean
}

export interface LikeStatus {
  liked: boolean
  likeCount: number
}

export interface Attendance {
  checkedToday: boolean
  streak: number
  totalDays: number
  today: string
  year: number
  month: number
  daysInMonth: number
  monthAttendedDays: number[]
}

export type LiveOverrideMode = 'AUTO' | 'FORCE_ON' | 'FORCE_OFF'

export interface LiveStatus {
  live: boolean
  title: string | null
  streamUrl: string | null
  mode: LiveOverrideMode
  channelId: string | null
  updatedAt: string | null
}

export interface LiveUpdateRequest {
  mode: LiveOverrideMode
  title: string | null
  streamUrl: string | null
  channelId: string | null
}

export interface Grade {
  id: number
  name: string
  sortOrder: number
  badgeColor: string | null
  isDefault: boolean
}

export interface GradeInput {
  name: string
  sortOrder: number
  badgeColor: string | null
  isDefault: boolean
}

export interface BoardAdmin {
  id: number
  code: string
  nameKr: string
  nameEn: string | null
  description: string | null
  type: BoardType
  sortOrder: number
  visible: boolean
  writeRole: Role
  categories: string[]
}

export interface BoardCreateRequest {
  code: string
  nameKr: string
  nameEn: string | null
  description: string | null
  sortOrder: number
  type: BoardType
  writeRole: Role
  categories: string[]
}

export interface BoardUpdateRequest {
  nameKr: string
  nameEn: string | null
  description: string | null
  sortOrder: number
  type: BoardType
  writeRole: Role
  visible: boolean
  categories: string[]
}

// 공개 통계(비로그인 노출). 백엔드 PublicStatsResponse와 1:1.
export interface PublicStats {
  memberCount: number
}

// 운영 대시보드 통계. 백엔드 DashboardStatsResponse와 1:1.
export interface DashboardStats {
  totalMembers: number
  activeMembers: number
  suspendedMembers: number
  withdrawnMembers: number
  newMembers7d: number
  totalPosts: number
  newPosts7d: number
  totalComments: number
  totalBoards: number
  totalGrades: number
  attendanceToday: number
}

export interface MemberAdmin {
  id: number
  provider: Provider
  nickname: string
  role: Role
  status: MemberStatus
  gradeId: number | null
  gradeName: string | null
  gradeColor: string | null
  createdAt: string
}
