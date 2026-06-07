// 백엔드 DTO와 1:1 대응하는 클라이언트 타입. enum 대신 string literal union(ECC TS 룰).

export type BoardType = 'GENERAL' | 'GALLERY'

export interface Board {
  id: number
  code: string
  nameKr: string
  nameEn: string | null
  description: string | null
  type: BoardType
  sortOrder: number
}

export interface PostSummary {
  id: number
  title: string
  authorNickname: string
  viewCount: number
  likeCount: number
  pinned: boolean
  createdAt: string
}

export interface PostDetail {
  id: number
  boardCode: string
  boardNameKr: string
  title: string
  content: string
  authorNickname: string
  viewCount: number
  likeCount: number
  pinned: boolean
  createdAt: string
  updatedAt: string
}

export interface PostCreateRequest {
  boardCode: string
  authorId: number
  title: string
  content: string
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
  deleted: boolean
  createdAt: string
}

export interface CommentCreateRequest {
  content: string
  parentId: number | null
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
}

export interface LiveStatus {
  live: boolean
  title: string | null
  streamUrl: string | null
  updatedAt: string | null
}

export interface LiveUpdateRequest {
  live: boolean
  title: string | null
  streamUrl: string | null
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
}

export interface BoardCreateRequest {
  code: string
  nameKr: string
  nameEn: string | null
  description: string | null
  sortOrder: number
  type: BoardType
  writeRole: Role
}

export interface BoardUpdateRequest {
  nameKr: string
  nameEn: string | null
  description: string | null
  sortOrder: number
  type: BoardType
  writeRole: Role
  visible: boolean
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
