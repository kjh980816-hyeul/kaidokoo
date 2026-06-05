import { http } from './http'
import type { Board } from './types'

export function fetchBoards(): Promise<Board[]> {
  return http<Board[]>('/boards')
}
