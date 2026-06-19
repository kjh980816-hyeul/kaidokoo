import { http } from './http'
import type { Branding } from './types'

/** 사이트 브랜딩(로고) 공개 조회. */
export function fetchBranding(): Promise<Branding> {
  return http<Branding>('/branding')
}

/** 로고 URL 설정(ADMIN). 빈 문자열이면 기본 엠블럼으로 해제. 권한은 서버 재검증. */
export function updateLogo(logoUrl: string | null): Promise<Branding> {
  return http<Branding>('/admin/branding', {
    method: 'PUT',
    body: JSON.stringify({ logoUrl }),
  })
}
