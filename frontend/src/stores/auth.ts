import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/submission'

export interface User {
  id: string
  username: string
  name: string
  role: string
}

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const loading = ref(false)
  const error = ref('')

  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isStudent = computed(() => user.value?.role === 'ROLE_STUDENT')
  const isTeacher = computed(() => 
    user.value?.role === 'ROLE_ADMIN' || 
    user.value?.role === 'ROLE_HQ_TEACHER' || 
    user.value?.role === 'ROLE_FRANCHISE_TEACHER'
  )

  // 初始化认证状态
  const initAuth = () => {
    const savedToken = localStorage.getItem('auth_token')
    const savedUser = localStorage.getItem('auth_user')
    
    if (savedToken && savedUser && savedToken !== 'undefined' && savedUser !== 'undefined') {
      try {
        token.value = savedToken
        user.value = JSON.parse(savedUser)
      } catch (err) {
        console.error('解析用户信息失败:', err)
        clearAuth()
      }
    } else {
      // 清除无效的localStorage数据
      clearAuth()
    }
  }

  // 登录
  const login = async (username: string, password: string) => {
    try {
      loading.value = true
      error.value = ''
      
      const response = await authApi.login(username, password)
      
      // 后端返回格式: { data: { accessToken, userId, username, email, roles, ... }, success, message }
      const { data } = response
      
      // 构造用户对象
      const userInfo = {
        id: data.userId.toString(),
        username: data.username,
        name: data.username, // 使用username作为显示名称
        role: data.roles[0] // 取第一个角色
      }
      
      // 保存认证信息
      token.value = data.accessToken
      user.value = userInfo
      
      // 持久化到localStorage
      localStorage.setItem('auth_token', data.accessToken)
      localStorage.setItem('auth_user', JSON.stringify(userInfo))
      
      return response
    } catch (err) {
      error.value = err instanceof Error ? err.message : '登录失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  // 登出
  const logout = async () => {
    try {
      loading.value = true
      
      // 调用后端登出API
      await authApi.logout()
    } catch (err) {
      console.error('登出请求失败:', err)
      // 即使后端请求失败，也要清除本地状态
    } finally {
      clearAuth()
      loading.value = false
    }
  }

  // 清除认证状态
  const clearAuth = () => {
    user.value = null
    token.value = null
    error.value = ''
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
  }

  // 获取当前用户信息
  const getCurrentUser = async () => {
    try {
      if (!token.value) {
        throw new Error('未登录')
      }
      
      const currentUser = await authApi.getCurrentUser()
      user.value = currentUser
      localStorage.setItem('auth_user', JSON.stringify(currentUser))
      
      return currentUser
    } catch (err) {
      console.error('获取用户信息失败:', err)
      clearAuth()
      throw err
    }
  }

  // 检查认证状态
  const checkAuth = async () => {
    if (!token.value) {
      return false
    }
    
    try {
      await getCurrentUser()
      return true
    } catch (err) {
      clearAuth()
      return false
    }
  }

  return {
    // 状态
    user,
    token,
    loading,
    error,
    
    // 计算属性
    isAuthenticated,
    isStudent,
    isTeacher,
    
    // 方法
    initAuth,
    login,
    logout,
    clearAuth,
    getCurrentUser,
    checkAuth
  }
})