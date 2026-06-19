import { http } from './http'
import type { PublicStats } from './types'

// 공개 통계(인증 불필요). 사이드바 "선원 N명" 표시용.
export function fetchPublicStats(): Promise<PublicStats> {
  return http<PublicStats>('/stats')
}
