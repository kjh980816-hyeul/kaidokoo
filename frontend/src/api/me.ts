import { http } from './http'
import type { Me } from './auth'

/** 닉네임 변경(PATCH /api/me). 갱신된 내 정보를 돌려준다. */
export function updateNickname(nickname: string): Promise<Me> {
  return http<Me>('/me', {
    method: 'PATCH',
    body: JSON.stringify({ nickname }),
  })
}

/** 프로필 사진 업로드(POST /api/me/avatar, multipart 필드명 file). */
export function uploadAvatar(file: File): Promise<Me> {
  const form = new FormData()
  form.append('file', file)
  return http<Me>('/me/avatar', { method: 'POST', body: form })
}

/** 프로필 사진 제거(DELETE /api/me/avatar). */
export function removeAvatar(): Promise<Me> {
  return http<Me>('/me/avatar', { method: 'DELETE' })
}
