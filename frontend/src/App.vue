<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 退出登录
const logout = async () => {
  try {
    await authStore.logout()
    router.push('/')
  } catch (err) {
    console.error('退出登录失败:', err)
    // 即使失败也跳转到首页
    router.push('/')
  }
}

// 监听路由变化，检查认证状态
watch(() => router.currentRoute.value, async (to) => {
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    const isValid = await authStore.checkAuth()
    if (!isValid) {
      router.push('/')
    }
  }
})

onMounted(() => {
  // 初始化认证状态
  authStore.initAuth()
})
</script>

<template>
  <div class="app">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="container">
        <div class="header-content">
          <div class="logo">
            <h1>万里书院</h1>
            <span class="subtitle">在线作业系统</span>
          </div>
          
          <nav class="nav" v-if="authStore.isAuthenticated">
            <RouterLink to="/assignments" class="nav-link" data-testid="assignments-link">
              <span>我的作业</span>
            </RouterLink>
          </nav>
          
          <div class="user-info" v-if="authStore.isAuthenticated">
            <span class="username">{{ authStore.user?.name || authStore.user?.username }}</span>
            <button @click="logout" class="logout-btn">退出</button>
          </div>
          <div v-else class="login-prompt">
            <span>请登录</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main">
      <RouterView />
    </main>

    <!-- 底部 -->
    <footer class="footer" v-if="authStore.isAuthenticated">
      <div class="container">
        <p>&copy; 2024 万里书院. 保留所有权利.</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 0;
}

.logo h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 700;
  color: white;
}

.logo .subtitle {
  font-size: 0.9rem;
  opacity: 0.9;
  margin-left: 0.5rem;
}

.nav {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

.nav-link.router-link-active {
  background-color: rgba(255, 255, 255, 0.2);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.username {
  font-weight: 500;
  color: white;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.main {
  flex: 1;
  padding: 2rem 0;
}

.footer {
  background-color: #2d3748;
  color: #a0aec0;
  padding: 1rem 0;
  text-align: center;
  margin-top: auto;
}

.footer p {
  margin: 0;
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
  }
  
  .nav {
    gap: 1rem;
  }
  
  .logo h1 {
    font-size: 1.5rem;
  }
}
</style>
