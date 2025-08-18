<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const username = ref('')
const password = ref('')

// 登录处理
const handleLogin = async () => {
  if (!username.value || !password.value) {
    return
  }

  try {
    await authStore.login(username.value, password.value)
    // 登录成功，跳转到作业列表页面
    router.push('/assignments')
  } catch (error) {
    console.error('登录失败:', error)
  }
}

// 快速登录（测试用）
const quickLogin = (user = 'test_student1', pass = 'password123') => {
  username.value = user
  password.value = pass
  handleLogin()
}

// 如果已登录，直接跳转
onMounted(() => {
  if (authStore.isAuthenticated) {
    router.push('/assignments')
  }
})
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>万里书院</h1>
        <p>在线作业系统</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
            id="username"
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            :disabled="authStore.loading"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="请输入密码"
            :disabled="authStore.loading"
            required
          />
        </div>

        <div class="error-message" v-if="authStore.error">
          {{ authStore.error }}
        </div>
        
        <button 
          type="submit"
          :disabled="authStore.loading" 
          class="login-btn"
        >
          {{ authStore.loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <!-- 测试用快速登录按钮 -->
      <div class="quick-login">
        <p class="quick-login-title">快速登录（测试）：</p>
        <div class="quick-login-buttons">
          <button 
            @click="quickLogin('test_student1', 'password123')"
            class="quick-btn student-btn"
            :disabled="authStore.loading"
          >
            学生账号
          </button>
          <button 
            @click="quickLogin('test_hq_teacher', 'password123')"
            class="quick-btn teacher-btn"
            :disabled="authStore.loading"
          >
            教师账号
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  width: 100%;
  max-width: 400px;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-header h1 {
  color: #2d3748;
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-header p {
  color: #718096;
  font-size: 1rem;
  margin: 0;
}

.login-form {
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #2d3748;
  font-weight: 500;
  font-size: 0.9rem;
}

.form-group input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group input:disabled {
  background-color: #f7fafc;
  cursor: not-allowed;
}

.error-message {
  background-color: #fed7d7;
  color: #c53030;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  text-align: center;
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.875rem 1.5rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.quick-login {
  border-top: 1px solid #e2e8f0;
  padding-top: 1.5rem;
}

.quick-login-title {
  text-align: center;
  color: #718096;
  font-size: 0.9rem;
  margin: 0 0 1rem 0;
}

.quick-login-buttons {
  display: flex;
  gap: 0.75rem;
}

.quick-btn {
  flex: 1;
  padding: 0.5rem 1rem;
  border: 2px solid transparent;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.student-btn {
  background-color: #e6fffa;
  color: #234e52;
  border-color: #81e6d9;
}

.student-btn:hover:not(:disabled) {
  background-color: #b2f5ea;
  transform: translateY(-1px);
}

.teacher-btn {
  background-color: #fef5e7;
  color: #744210;
  border-color: #f6ad55;
}

.teacher-btn:hover:not(:disabled) {
  background-color: #fbd38d;
  transform: translateY(-1px);
}

.quick-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-container {
    padding: 1rem;
  }
  
  .login-card {
    padding: 2rem;
  }
  
  .login-header h1 {
    font-size: 1.75rem;
  }
}
</style>
