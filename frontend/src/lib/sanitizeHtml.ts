import DOMPurify from 'dompurify'

// 본문에 직접 박을 iframe src는 유튜브/비메오 임베드만 허용한다(심층 방어 — 백엔드도 1차 제한).
const ALLOWED_IFRAME_HOST = /^https:\/\/(?:www\.)?(?:youtube\.com|youtube-nocookie\.com|youtu\.be|player\.vimeo\.com)\//i

// iframe[src]가 화이트리스트 호스트가 아니면 통째로 제거한다.
let hookRegistered = false
function registerIframeHook(): void {
  if (hookRegistered) return
  hookRegistered = true
  DOMPurify.addHook('uponSanitizeElement', (node, data) => {
    if (data.tagName !== 'iframe') return
    const el = node as Element
    const src = el.getAttribute('src') ?? ''
    if (!ALLOWED_IFRAME_HOST.test(src)) {
      el.remove()
    }
  })
}

// HTML 여부 판별: 태그가 하나도 없으면 레거시 평문으로 본다.
const HTML_TAG = /<[a-z][\s\S]*>/i

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

/**
 * 게시글 본문 HTML을 안전하게 렌더 가능한 문자열로 변환한다.
 * - 레거시 평문(태그 없음): 이스케이프 후 줄바꿈을 <br>로 보존.
 * - HTML: DOMPurify로 정제(iframe/임베드 속성 허용 + src 호스트 제한).
 */
export function renderPostHtml(raw: string): string {
  if (!raw) return ''

  if (!HTML_TAG.test(raw)) {
    return escapeHtml(raw).replace(/\r\n|\r|\n/g, '<br>')
  }

  registerIframeHook()
  return DOMPurify.sanitize(raw, {
    ADD_TAGS: ['iframe'],
    ADD_ATTR: ['allow', 'allowfullscreen', 'frameborder', 'target', 'style', 'rel'],
    ALLOWED_URI_REGEXP:
      /^(?:(?:https?|mailto|tel):|[^a-z]|[a-z+.\-]+(?:[^a-z+.\-:]|$))/i,
  })
}
