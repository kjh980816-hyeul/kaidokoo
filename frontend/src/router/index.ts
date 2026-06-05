import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
  },
  {
    path: '/board/:code',
    name: 'board',
    component: () => import('@/views/BoardView.vue'),
    props: true,
  },
  {
    path: '/board/:code/write',
    name: 'post-write',
    component: () => import('@/views/PostWriteView.vue'),
    props: true,
  },
  {
    path: '/post/:id',
    name: 'post-detail',
    component: () => import('@/views/PostDetailView.vue'),
    props: true,
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})
