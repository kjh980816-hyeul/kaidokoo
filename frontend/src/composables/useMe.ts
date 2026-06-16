import { ref } from 'vue'
import { fetchMe, type Me } from '@/api/auth'

// 모듈 레벨 단일 ref = 앱 전역 로그인 상태(ADR-0004). 헤더·마이페이지가 공유한다.
const me = ref<Me | null>(null)

export function useMe() {
  async function load(): Promise<void> {
    try {
      me.value = await fetchMe()
    } catch {
      me.value = { authenticated: false }
    }
  }

  /** 프로필 변경 등으로 받은 최신 내 정보를 즉시 반영(헤더 닉네임·아바타 갱신). */
  function set(next: Me): void {
    me.value = next
  }

  function clear(): void {
    me.value = { authenticated: false }
  }

  return { me, load, set, clear }
}
