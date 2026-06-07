import { http } from './http'
import type { LiveStatus } from './types'

export function fetchLiveStatus(): Promise<LiveStatus> {
  return http<LiveStatus>('/live-status')
}
