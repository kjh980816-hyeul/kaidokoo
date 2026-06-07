import { http } from './http'
import type {
  BoardAdmin,
  BoardCreateRequest,
  BoardUpdateRequest,
  GradeInput,
  LiveStatus,
  LiveUpdateRequest,
  MemberAdmin,
  Role,
  MemberStatus,
} from './types'

// 게시판 관리
export function fetchAdminBoards(): Promise<BoardAdmin[]> {
  return http<BoardAdmin[]>('/admin/boards')
}
export function createBoard(req: BoardCreateRequest): Promise<{ id: number }> {
  return http<{ id: number }>('/admin/boards', { method: 'POST', body: JSON.stringify(req) })
}
export function updateBoard(id: number, req: BoardUpdateRequest): Promise<void> {
  return http<void>(`/admin/boards/${id}`, { method: 'PUT', body: JSON.stringify(req) })
}
export function deleteBoard(id: number): Promise<void> {
  return http<void>(`/admin/boards/${id}`, { method: 'DELETE' })
}

// 등급 관리
export function createGrade(req: GradeInput): Promise<{ id: number }> {
  return http<{ id: number }>('/admin/grades', { method: 'POST', body: JSON.stringify(req) })
}
export function updateGrade(id: number, req: GradeInput): Promise<void> {
  return http<void>(`/admin/grades/${id}`, { method: 'PUT', body: JSON.stringify(req) })
}
export function deleteGrade(id: number): Promise<void> {
  return http<void>(`/admin/grades/${id}`, { method: 'DELETE' })
}

// 회원 관리
export function fetchAdminMembers(): Promise<MemberAdmin[]> {
  return http<MemberAdmin[]>('/admin/members')
}
export function assignMemberGrade(id: number, gradeId: number | null): Promise<void> {
  return http<void>(`/admin/members/${id}/grade`, { method: 'PUT', body: JSON.stringify({ gradeId }) })
}
export function changeMemberRole(id: number, role: Role): Promise<void> {
  return http<void>(`/admin/members/${id}/role`, { method: 'PUT', body: JSON.stringify({ role }) })
}
export function changeMemberStatus(id: number, status: MemberStatus): Promise<void> {
  return http<void>(`/admin/members/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }) })
}

// 라이브 배너
export function setLiveStatus(req: LiveUpdateRequest): Promise<LiveStatus> {
  return http<LiveStatus>('/admin/live-status', { method: 'PUT', body: JSON.stringify(req) })
}
