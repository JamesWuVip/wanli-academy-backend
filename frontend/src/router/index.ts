import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import AssignmentList from '../views/AssignmentList.vue'
import SubmissionResult from '../views/SubmissionResult.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/assignments',
      name: 'assignments',
      component: AssignmentList,
      meta: {
        requiresAuth: true,
        title: '我的作业'
      }
    },
    {
      path: '/submissions/:submissionId/result',
      name: 'submission-result',
      component: SubmissionResult,
      meta: {
        requiresAuth: true,
        title: '作业结果'
      },
      props: true
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
  ],
})

// 路由守卫 - 检查认证状态
router.beforeEach((to, from, next) => {
  // 检查路由是否需要认证
  if (to.meta.requiresAuth) {
    // 检查 localStorage 中的 token
    const token = localStorage.getItem('auth_token')
    if (!token) {
      // 如果没有token，重定向到登录页面（首页）
      next('/')
      return
    }
  }
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 万里书院`
  }
  
  next()
})

export default router
