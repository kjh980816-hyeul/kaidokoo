import { http } from './http'
import type { Comment, CommentCreateRequest } from './types'

export function fetchComments(postId: number): Promise<Comment[]> {
  return http<Comment[]>(`/posts/${postId}/comments`)
}

export function createComment(postId: number, req: CommentCreateRequest): Promise<{ id: number }> {
  return http<{ id: number }>(`/posts/${postId}/comments`, {
    method: 'POST',
    body: JSON.stringify(req),
  })
}

export function deleteComment(commentId: number): Promise<void> {
  return http<void>(`/comments/${commentId}`, { method: 'DELETE' })
}
