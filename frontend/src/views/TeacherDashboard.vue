<template>
  <div class="teacher-dashboard">
    <!-- 页面标题 -->
    <div class="header">
      <h1 class="title">教师工作台</h1>
      <div class="welcome-info">
        <span>欢迎，{{ authStore.user?.name }}老师</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📚</div>
        <div class="stat-content">
          <h3>{{ stats.totalAssignments }}</h3>
          <p>总作业数</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📝</div>
        <div class="stat-content">
          <h3>{{ stats.pendingSubmissions }}</h3>
          <p>待批改</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <h3>{{ stats.totalStudents }}</h3>
          <p>学生总数</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <h3>{{ stats.gradedSubmissions }}</h3>
          <p>已批改</p>
        </div>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <h2>快速操作</h2>
      <div class="action-grid">
        <router-link to="/teacher/assignments" class="action-card">
          <div class="action-icon">📋</div>
          <h3>作业管理</h3>
          <p>创建、编辑和发布作业</p>
        </router-link>
        <router-link to="/teacher/submissions" class="action-card">
          <div class="action-icon">📊</div>
          <h3>提交管理</h3>
          <p>查看和批改学生作业</p>
        </router-link>
        <router-link to="/teacher/students" class="action-card">
          <div class="action-icon">👨‍🎓</div>
          <h3>学生管理</h3>
          <p>查看学生信息和成绩</p>
        </router-link>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activity">
      <h2>最近活动</h2>
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>正在加载...</p>
      </div>
      <div v-else-if="recentActivities.length === 0" class="empty-state">
        <p>暂无最近活动</p>
      </div>
      <div v-else class="activity-list">
        <div 
          v-for="activity in recentActivities" 
          :key="activity.id"
          class="activity-item"
        >
          <div class="activity-icon">{{ activity.icon }}</div>
          <div class="activity-content">
            <h4>{{ activity.title }}</h4>
            <p>{{ activity.description }}</p>
            <span class="activity-time">{{ formatTime(activity.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(true)

// 统计数据
const stats = ref({
  totalAssignments: 0,
  pendingSubmissions: 0,
  totalStudents: 0,
  gradedSubmissions: 0
})

// 最近活动
const recentActivities = ref<Array<{
  id: string
  title: string
  description: string
  icon: string
  createdAt: string
}>>([])

// 格式化时间
const formatTime = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else {
    return `${days}天前`
  }
}

// 加载数据
const loadDashboardData = async () => {
  try {
    loading.value = true
    
    // 模拟数据加载
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 设置模拟统计数据
    stats.value = {
      totalAssignments: 12,
      pendingSubmissions: 8,
      totalStudents: 45,
      gradedSubmissions: 23
    }
    
    // 设置模拟活动数据
    recentActivities.value = [
      {
        id: '1',
        title: '新作业提交',
        description: '张三提交了《数学练习题》',
        icon: '📝',
        createdAt: new Date(Date.now() - 30 * 60 * 1000).toISOString()
      },
      {
        id: '2',
        title: '作业已批改',
        description: '完成了李四的《英语阅读理解》批改',
        icon: '✅',
        createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString()
      },
      {
        id: '3',
        title: '发布新作业',
        description: '发布了《物理实验报告》作业',
        icon: '📚',
        createdAt: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString()
      }
    ]
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.teacher-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e5e7eb;
}

.title {
  font-size: 2rem;
  font-weight: bold;
  color: #1f2937;
  margin: 0;
}

.welcome-info {
  color: #6b7280;
  font-size: 1.1rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px -3px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 2.5rem;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  border-radius: 12px;
}

.stat-content h3 {
  font-size: 2rem;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.stat-content p {
  color: #6b7280;
  margin: 0;
  font-size: 0.9rem;
}

.quick-actions {
  margin-bottom: 40px;
}

.quick-actions h2 {
  font-size: 1.5rem;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 20px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.action-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s, box-shadow 0.2s;
  display: block;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px -3px rgba(0, 0, 0, 0.1);
  text-decoration: none;
  color: inherit;
}

.action-icon {
  font-size: 2.5rem;
  margin-bottom: 12px;
}

.action-card h3 {
  font-size: 1.25rem;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.action-card p {
  color: #6b7280;
  margin: 0;
  font-size: 0.9rem;
}

.recent-activity h2 {
  font-size: 1.5rem;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 20px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  color: #6b7280;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f4f6;
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #6b7280;
}

.activity-list {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: