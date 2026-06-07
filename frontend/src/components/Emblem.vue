<script setup lang="ts">
/**
 * 천문 항해 엠블럼 — 나침반 장미 × 별. currentColor 로 부모 골드 색을 상속한다.
 * 원본 시안의 엠블럼은 번들 JS 에만 있어 미추출 → 동일 톤(골드 나침반/별)으로 재제작.
 */
withDefaults(defineProps<{ title?: string }>(), { title: '' })
</script>

<template>
  <svg
    class="emblem"
    viewBox="0 0 100 100"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    :role="title ? 'img' : undefined"
    :aria-label="title || undefined"
    :aria-hidden="title ? undefined : true"
  >
    <!-- 외곽 링 -->
    <circle cx="50" cy="50" r="46" stroke="currentColor" stroke-width="0.9" opacity="0.55" />
    <!-- 내부 점선 링 -->
    <circle
      cx="50"
      cy="50"
      r="38"
      stroke="currentColor"
      stroke-width="0.6"
      stroke-dasharray="1.6 3"
      opacity="0.4"
    />
    <!-- 대각 보조 광선 -->
    <g stroke="currentColor" stroke-width="0.7" opacity="0.45">
      <line x1="50" y1="50" x2="71" y2="29" />
      <line x1="50" y1="50" x2="29" y2="29" />
      <line x1="50" y1="50" x2="71" y2="71" />
      <line x1="50" y1="50" x2="29" y2="71" />
    </g>
    <!-- 나침반 장미 (4 방위 첨예 별) -->
    <polygon
      points="50,10 55,45 90,50 55,55 50,90 45,55 10,50 45,45"
      fill="currentColor"
      opacity="0.92"
    />
    <!-- 중심 -->
    <circle cx="50" cy="50" r="3.4" fill="currentColor" />
    <circle cx="50" cy="50" r="6.4" stroke="currentColor" stroke-width="0.8" opacity="0.7" />
    <!-- 트윙클 별점 -->
    <circle class="tw" cx="50" cy="6" r="1.3" fill="currentColor" />
    <circle class="tw" cx="94" cy="50" r="1.3" fill="currentColor" style="animation-delay: -1s" />
    <circle class="tw" cx="50" cy="94" r="1.3" fill="currentColor" style="animation-delay: -2s" />
    <circle class="tw" cx="6" cy="50" r="1.3" fill="currentColor" style="animation-delay: -1.5s" />
  </svg>
</template>

<style scoped>
.emblem {
  display: block;
  width: 100%;
  height: 100%;
}
.tw {
  animation: tw 3s ease-in-out infinite;
}
@keyframes tw {
  0%,
  100% {
    opacity: 0.35;
  }
  50% {
    opacity: 1;
  }
}
</style>
