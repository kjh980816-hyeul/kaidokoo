import { http } from './http'
import type { Grade } from './types'

export function fetchGrades(): Promise<Grade[]> {
  return http<Grade[]>('/grades')
}
