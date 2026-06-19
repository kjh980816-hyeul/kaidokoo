import { http } from './http'
import type { BannerItem } from './types'

/** 외부링크 배너 항목 목록(공개 조회). */
export function fetchBannerLinks(): Promise<BannerItem[]> {
  return http<BannerItem[]>('/banner-links')
}

/** 배너 항목 일괄 저장(ADMIN). items 전체로 교체. 권한은 서버에서 재검증된다. */
export function updateBannerLinks(items: BannerItem[]): Promise<BannerItem[]> {
  return http<BannerItem[]>('/admin/banner-links', {
    method: 'PUT',
    body: JSON.stringify({ items }),
  })
}
