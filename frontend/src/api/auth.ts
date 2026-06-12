import { http } from './http'
import type { Role } from './types'

/** 현재 로그인 상태(GET /api/auth/me). 비로그인도 200으로 내려온다. */
export interface Me {
  authenticated: boolean
  id?: number | null
  nickname?: string | null
  role?: Role | null
}

export function fetchMe(): Promise<Me> {
  return http<Me>('/auth/me')
}

export function logout(): Promise<void> {
  return http<void>('/auth/logout', { method: 'POST' })
}

/** 로그인은 서버 리다이렉트 플로우라 fetch가 아니라 전체 페이지 이동으로 시작한다. */
export function loginUrl(provider: 'naver' | 'google'): string {
  return `/api/auth/${provider}/login`
}
