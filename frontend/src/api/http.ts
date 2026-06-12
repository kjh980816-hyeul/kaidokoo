// 모든 API 호출의 단일 진입점. 컴포넌트에서 직접 fetch 금지(00-stack: api/ 레이어).
// 인증은 HttpOnly 세션 쿠키(ADR-0004) — fetch 기본(same-origin)으로 자동 전송된다.

const BASE = '/api'

interface ServerError {
  status: number
  error: string
  message: string
}

/** HTTP 상태코드를 보존하는 에러. 호출부에서 401/403 등을 구분 처리할 수 있다. */
export class HttpError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'HttpError'
    this.status = status
  }
}

export async function http<T>(path: string, init?: RequestInit): Promise<T> {
  // init을 먼저 펼쳐야 아래에서 합친 headers가 init.headers에 통째로 덮이지 않는다.
  const res = await fetch(`${BASE}${path}`, {
    ...init,
    headers: {
      // Content-Type은 본문이 있을 때만(GET/DELETE에 붙이지 않는다).
      ...(init?.body !== undefined ? { 'Content-Type': 'application/json' } : {}),
      ...(init?.headers ?? {}),
    },
  })

  if (!res.ok) {
    let message = `요청에 실패했습니다 (${res.status})`
    try {
      const body = (await res.json()) as ServerError
      if (body?.message) message = body.message
    } catch {
      // 비-JSON 에러 본문은 무시하고 기본 메시지 사용
    }
    throw new HttpError(res.status, message)
  }

  if (res.status === 204) {
    return undefined as T
  }
  return (await res.json()) as T
}
