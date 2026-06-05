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
