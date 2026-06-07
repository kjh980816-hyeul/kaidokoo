import { http } from './http'
import type { Attendance } from './types'

export function fetchAttendance(): Promise<Attendance> {
  return http<Attendance>('/attendance')
}

export function checkInAttendance(): Promise<Attendance> {
  return http<Attendance>('/attendance', { method: 'POST' })
}
