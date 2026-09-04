import DOMPurify from 'dompurify'

// 본문에 직접 박을 iframe src는 유튜브/비메오 임베드만 허용한다(심층 방어 — 백엔드도 1차 제한).
const ALLOWED_IFRAME_HOST = /^https:\/\/(?:www\.)?(?:youtube\.com|youtube-nocookie\.com|youtu\.be|player\.vimeo\.com)\//i

// audio src는 우리 업로드 저장소 경로만 허용한다(심층 방어 — 백엔드 정화기와 동일 계약).
const ALLOWED_AUDIO_SRC = /^\/uploads\/posts\//

// iframe[src] 화이트리스트 호스트 외 제거 + audio[src] 저장소 경로 외 제거.
let hookRegistered = false
function registerIframeHook(): void {
  if (hookRegistered) return
  hookRegistered = true
  DOMPurify.addHook('uponSanitizeElement', (node, data) => {
    if (data.tagName !== 'iframe' && data.tagName !== 'audio') return
    const el = node as Element
    const src = el.getAttribute('src') ?? ''
    const ok = data.tagName === 'iframe' ? ALLOWED_IFRAME_HOST.test(src) : ALLOWED_AUDIO_SRC.test(src)
    if (!ok) {
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
    // 레거시 평문: 줄바꿈은 <br>, 연속 공백·탭은 nbsp로 보존(원본 띄어쓰기 유지).
    // 첫 칸만 일반 공백으로 둬 단어 줄바꿈은 살린다.
    const escaped = escapeHtml(raw)
      .replace(/\t/g, '    ')
      .replace(/ {2,}/g, (m) => ' ' + ' '.repeat(m.length - 1))
    return escaped.replace(/\r\n|\r|\n/g, '<br>')
  }

  registerIframeHook()
  return DOMPurify.sanitize(raw, {
    ADD_TAGS: ['iframe'],
    ADD_ATTR: ['allow', 'allowfullscreen', 'frameborder', 'target', 'style', 'rel'],
    ALLOWED_URI_REGEXP:
      /^(?:(?:https?|mailto|tel):|[^a-z]|[a-z+.\-]+(?:[^a-z+.\-:]|$))/i,
  })
}
