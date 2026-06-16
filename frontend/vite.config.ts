import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 개발 시 /api 요청을 백엔드(Spring Boot, 8080)로 프록시 → CORS·키 노출 회피
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // 업로드 파일(아바타)도 개발 중엔 백엔드가 서빙 → 프록시. 운영은 Nginx가 직접 서빙.
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
