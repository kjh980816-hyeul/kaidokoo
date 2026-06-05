// 모든 API 호출의 단일 진입점. 컴포넌트에서 직접 fetch 금지(00-stack: api/ 레이어).
const BASE = '/api'

interface ServerError {
  status: number
  error: string
  message: string
}

export async function http<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...(init?.headers ?? {}) },
    ...init,
  })

  if (!res.ok) {
    let message = `요청에 실패했습니다 (${res.status})`
    try {
      const body = (await res.json()) as ServerError
      if (body?.message) message = body.message
    } catch {
      // 비-JSON 에러 본문은 무시하고 기본 메시지 사용
    }
    throw new Error(message)
  }

  if (res.status === 204) {
    return undefined as T
  }
  return (await res.json()) as T
}
