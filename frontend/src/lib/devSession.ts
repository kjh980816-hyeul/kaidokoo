// 임시 dev 세션 (소셜 로그인 도입 전, ADR-0003).
// 시드 회원: 1 = 개발선원(MEMBER), 2 = 선장(ADMIN). 백엔드는 X-Member-Id 헤더로 신원을 해석한다.
// ⚠️ 운영 보안 아님 — 로그인 구현 시 이 모듈과 X-Member-Id 헤더를 제거하고 세션으로 교체한다.
import { ref } from 'vue'

const STORAGE_KEY = 'kaidoku.devMemberId'

const stored = localStorage.getItem(STORAGE_KEY)
export const devMemberId = ref<number>(stored ? Number(stored) : 1)

export function setDevMemberId(id: number): void {
  devMemberId.value = id
  localStorage.setItem(STORAGE_KEY, String(id))
}
