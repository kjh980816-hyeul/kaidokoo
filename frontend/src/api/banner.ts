import { http } from './http'
import type { BannerLinks } from './types'

/** 사이드바 외부링크 배너(공개 조회). */
export function fetchBannerLinks(): Promise<BannerLinks> {
  return http<BannerLinks>('/banner-links')
}

/** 외부링크 배너 저장(ADMIN). 권한은 서버에서 재검증된다. */
export function updateBannerLinks(links: BannerLinks): Promise<void> {
  return http<void>('/admin/banner-links', { method: 'PUT', body: JSON.stringify(links) })
}
