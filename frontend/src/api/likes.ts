import { http } from './http'
import type { LikeStatus } from './types'

export function fetchLikeStatus(postId: number): Promise<LikeStatus> {
  return http<LikeStatus>(`/posts/${postId}/like`)
}

export function toggleLike(postId: number): Promise<LikeStatus> {
  return http<LikeStatus>(`/posts/${postId}/like`, { method: 'POST' })
}
