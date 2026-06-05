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
