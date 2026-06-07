<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

/**
 * 페이지 전역 천체 배경: 성운 그라디언트(#sky) + 별하늘 캔버스(#starfield) + 외곽 골드 프레임.
 * prefers-reduced-motion 이면 별은 한 번만 정적으로 그리고 애니메이션을 멈춘다.
 */

const canvas = ref<HTMLCanvasElement | null>(null)
let raf = 0
let stars: Array<{ x: number; y: number; r: number; base: number; phase: number; speed: number }> = []
let shooting: { x: number; y: number; vx: number; vy: number; life: number } | null = null
let cleanup: (() => void) | null = null

function buildStars(w: number, h: number): void {
  const count = Math.min(220, Math.floor((w * h) / 9000))
  stars = Array.from({ length: count }, () => ({
    x: Math.random() * w,
    y: Math.random() * h,
    r: Math.random() * 1.3 + 0.3,
    base: Math.random() * 0.5 + 0.3,
    phase: Math.random() * Math.PI * 2,
    speed: Math.random() * 0.8 + 0.3,
  }))
}

function paint(ctx: CanvasRenderingContext2D, w: number, h: number, t: number, animate: boolean): void {
  ctx.clearRect(0, 0, w, h)
  for (const s of stars) {
    const tw = animate ? s.base + Math.sin(t * 0.001 * s.speed + s.phase) * 0.35 : s.base
    const alpha = Math.max(0, Math.min(1, tw))
    ctx.beginPath()
    ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(255, 248, 231, ${alpha})`
    ctx.fill()
  }
  if (animate && shooting) {
    const s = shooting
    const grad = ctx.createLinearGradient(s.x, s.y, s.x - s.vx * 12, s.y - s.vy * 12)
    grad.addColorStop(0, `rgba(232, 213, 160, ${s.life})`)
    grad.addColorStop(1, 'rgba(232, 213, 160, 0)')
    ctx.strokeStyle = grad
    ctx.lineWidth = 1.4
    ctx.beginPath()
    ctx.moveTo(s.x, s.y)
    ctx.lineTo(s.x - s.vx * 12, s.y - s.vy * 12)
    ctx.stroke()
    s.x += s.vx
    s.y += s.vy
    s.life -= 0.012
    if (s.life <= 0) shooting = null
  }
}

onMounted(() => {
  const el = canvas.value
  if (!el) return
  const ctx = el.getContext('2d')
  if (!ctx) return

  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches

  const resize = (): void => {
    const dpr = Math.min(window.devicePixelRatio || 1, 2)
    el.width = window.innerWidth * dpr
    el.height = window.innerHeight * dpr
    el.style.width = `${window.innerWidth}px`
    el.style.height = `${window.innerHeight}px`
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    buildStars(window.innerWidth, window.innerHeight)
    if (reduce) paint(ctx, window.innerWidth, window.innerHeight, 0, false)
  }
  resize()
  window.addEventListener('resize', resize)

  if (!reduce) {
    const loop = (t: number): void => {
      if (!shooting && Math.random() < 0.004) {
        shooting = {
          x: Math.random() * window.innerWidth * 0.6,
          y: Math.random() * window.innerHeight * 0.4,
          vx: 4 + Math.random() * 3,
          vy: 1.5 + Math.random() * 1.5,
          life: 1,
        }
      }
      paint(ctx, window.innerWidth, window.innerHeight, t, true)
      raf = requestAnimationFrame(loop)
    }
    raf = requestAnimationFrame(loop)
  }

  cleanup = (): void => window.removeEventListener('resize', resize)
})

onBeforeUnmount(() => {
  if (raf) cancelAnimationFrame(raf)
  if (cleanup) cleanup()
})
</script>

<template>
  <div id="sky" aria-hidden="true"></div>
  <canvas id="starfield" ref="canvas" aria-hidden="true"></canvas>

  <div class="deco-frame" aria-hidden="true">
    <svg class="corner tl" viewBox="0 0 46 46" fill="none">
      <path d="M2 18 A16 16 0 0 1 18 2" stroke="currentColor" stroke-width="1" />
      <path
        d="M6 14 A9 9 0 0 1 14 6 A6.5 6.5 0 0 0 6 14 Z"
        fill="currentColor"
        opacity="0.85"
      />
      <circle cx="3" cy="3" r="1.2" fill="currentColor" />
    </svg>
    <svg class="corner tr" viewBox="0 0 46 46" fill="none">
      <path d="M2 18 A16 16 0 0 1 18 2" stroke="currentColor" stroke-width="1" />
      <path d="M6 14 A9 9 0 0 1 14 6 A6.5 6.5 0 0 0 6 14 Z" fill="currentColor" opacity="0.85" />
      <circle cx="3" cy="3" r="1.2" fill="currentColor" />
    </svg>
    <svg class="corner bl" viewBox="0 0 46 46" fill="none">
      <path d="M2 18 A16 16 0 0 1 18 2" stroke="currentColor" stroke-width="1" />
      <path d="M6 14 A9 9 0 0 1 14 6 A6.5 6.5 0 0 0 6 14 Z" fill="currentColor" opacity="0.85" />
      <circle cx="3" cy="3" r="1.2" fill="currentColor" />
    </svg>
    <svg class="corner br" viewBox="0 0 46 46" fill="none">
      <path d="M2 18 A16 16 0 0 1 18 2" stroke="currentColor" stroke-width="1" />
      <path d="M6 14 A9 9 0 0 1 14 6 A6.5 6.5 0 0 0 6 14 Z" fill="currentColor" opacity="0.85" />
      <circle cx="3" cy="3" r="1.2" fill="currentColor" />
    </svg>
  </div>
</template>
