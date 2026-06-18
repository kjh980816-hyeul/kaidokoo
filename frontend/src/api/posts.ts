import { http } from './http'
import type { PostSummary, PostDetail, PostCreateRequest } from './types'

export function fetchPosts(boardCode: string): Promise<PostSummary[]> {
  return http<PostSummary[]>(`/boards/${encodeURIComponent(boardCode)}/posts`)
}

export function fetchPost(id: number): Promise<PostDetail> {
  return http<PostDetail>(`/posts/${id}`)
}

export function createPost(req: PostCreateRequest): Promise<{ id: number }> {
  return http<{ id: number }>('/posts', {
    method: 'POST',
    body: JSON.stringify(req),
  })
}

/** 게시글 첨부 이미지 업로드(multipart 필드명 file). 저장된 공개 URL을 돌려준다. */
export function uploadPostImage(file: File): Promise<{ url: string }> {
  const form = new FormData()
  form.append('file', file)
  return http<{ url: string }>('/posts/images', { method: 'POST', body: form })
}

/** 글 수정(작성자 본인 또는 ADMIN). 제목·본문·이미지 교체. */
export function updatePost(
  id: number,
  req: { title: string; content: string; imageUrls: string[] },
): Promise<void> {
  return http<void>(`/posts/${id}`, { method: 'PATCH', body: JSON.stringify(req) })
}

/** 글 삭제(작성자 본인 또는 ADMIN). 권한은 서버에서 재검증된다. */
export function deletePost(id: number): Promise<void> {
  return http<void>(`/posts/${id}`, { method: 'DELETE' })
}
