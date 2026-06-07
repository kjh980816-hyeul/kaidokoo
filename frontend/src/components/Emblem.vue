<script setup lang="ts">
/**
 * 천문 항해 키홀 엠블럼 — 원본 시안(준하님 캡처) 복원.
 * 이중 점선 링 + 4방위 눈금 + 상단 반짝임 + 이중 사각 프레임 + 중앙 열쇠구멍.
 * currentColor 로 부모 골드 색을 상속한다.
 */
withDefaults(defineProps<{ title?: string }>(), { title: '' })
</script>

<template>
  <svg
    class="emblem"
    viewBox="0 0 100 100"
    fill="none"
    stroke-linecap="round"
    xmlns="http://www.w3.org/2000/svg"
    :role="title ? 'img' : undefined"
    :aria-label="title || undefined"
    :aria-hidden="title ? undefined : true"
  >
    <!-- 외곽 실선 링 -->
    <circle cx="50" cy="50" r="46" stroke="currentColor" stroke-width="1.1" opacity="0.95" />
    <!-- 점선 링 -->
    <circle
      cx="50"
      cy="50"
      r="40"
      stroke="currentColor"
      stroke-width="1"
      stroke-dasharray="2.4 2.6"
      opacity="0.85"
    />
    <!-- 희미한 내부 링 -->
    <circle cx="50" cy="50" r="33.5" stroke="currentColor" stroke-width="0.8" opacity="0.3" />

    <!-- 4방위 십자 눈금 -->
    <g stroke="currentColor" stroke-width="1.2">
      <line x1="50" y1="1" x2="50" y2="13" />
      <line x1="50" y1="87" x2="50" y2="99" />
      <line x1="1" y1="50" x2="13" y2="50" />
      <line x1="87" y1="50" x2="99" y2="50" />
    </g>

    <!-- 상단 반짝임 -->
    <path
      class="tw"
      d="M50 3.5 L51.5 10 L58 11.5 L51.5 13 L50 19.5 L48.5 13 L42 11.5 L48.5 10 Z"
      fill="currentColor"
    />

    <!-- 이중 사각 프레임 -->
    <rect x="30" y="30" width="40" height="40" stroke="currentColor" stroke-width="1.1" />
    <rect
      x="34.5"
      y="34.5"
      width="31"
      height="31"
      stroke="currentColor"
      stroke-width="0.9"
      stroke-dasharray="2 2.2"
      opacity="0.8"
    />

    <!-- 중앙 열쇠구멍 -->
    <circle cx="50" cy="46" r="4.6" stroke="currentColor" stroke-width="1.1" />
    <path
      d="M46.8 49.4 L44.4 60.2 L55.6 60.2 L53.2 49.4"
      stroke="currentColor"
      stroke-width="1.1"
      stroke-linejoin="round"
    />
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
  transform-origin: 50px 11.5px;
}
@keyframes tw {
  0%,
  100% {
    opacity: 0.45;
  }
  50% {
    opacity: 1;
  }
}
</style>
