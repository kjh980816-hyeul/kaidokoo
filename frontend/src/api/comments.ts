import { http } from './http'
import type { Comment, CommentCreateRequest, LikeStatus } from './types'

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

/** 댓글 좋아요 토글(로그인 회원). 결과로 현재 좋아요 여부·총 수를 받는다. */
export function toggleCommentLike(commentId: number): Promise<LikeStatus> {
  return http<LikeStatus>(`/comments/${commentId}/like`, { method: 'POST' })
}
