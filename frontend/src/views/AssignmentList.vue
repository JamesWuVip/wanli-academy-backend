<template>
  <div class="assignment-list">
    <!-- 页面标题 -->
    <div class="header">
      <h1 class="title">我的作业</h1>
      <div class="header-actions">
        <button @click="refreshList" class="refresh-btn" :disabled="loading">
          <span class="refresh-icon">🔄</span>
          刷新
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>正在加载作业列表...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="() => fetchAssignments()" class="retry-btn">重试</button>
    </div>

    <!-- 作业列表 -->
    <div v-else-if="assignments.length > 0" class="assignments-container">
      <div 
        v-for="assignment in assignments" 
        :key="assignment.id"
        class="assignment-card"
        :class="getCardClass(assignment.status)"
      >
        <!-- 作业基本信息 -->
        <div class="assignment-info">
          <h3 class="assignment-title">{{ assignment.title }}</h3>
          <p v-if="assignment.description" class="assignment-description">
            {{ assignment.description }}
          </p>
          
          <div class="assignment-meta">
            <div class="meta-item">
              <span class="meta-label">截止时间：</span>
              <span class="meta-value" :class="getDueDateClass(assignment.dueDate)">
                {{ formatDate(assignment.dueDate) }}
              </span>
            </div>
            
            <div class="meta-item">
              <span class="meta-label">状态：</span>
              <span class="status-badge" :class="getStatusClass(assignment.status)">
                {{ getStatusText(assignment.status) }}
              </span>
            </div>
            
            <div v-if="assignment.score !== undefined" class="meta-item">
              <span class="meta-label">得分：</span>
              <span class="score-value">
                {{ assignment.score }}/{{ assignment.maxScore }}分
              </span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="assignment-actions">
          <!-- 查看结果按钮 -->
          <button 
            v-if="assignment.status === 'GRADED' && assignment.submissionId"
            @click="viewResult(assignment.submissionId!)"
            class="action-btn view-result-btn"
            data-testid="view-result-btn"
          >
            📊 查看结果
          </button>
          
          <!-- 继续作答按钮 -->
          <button 
            v-else-if="assignment.status === 'DRAFT'"
            @click="continueAssignment(assignment.id)"
            class="action-btn continue-btn"
          >
            ✏️ 继续作答
          </button>
          
          <!-- 开始作答按钮 -->
          <button 
            v-else-if="assignment.status === 'SUBMITTED'"
            class="action-btn submitted-btn"
            disabled
          >
            ✅ 已提交
          </button>
          
          <!-- 开始作答按钮 -->
          <button 
            v-else
            @click="startAssignment(assignment.id)"
            class="action-btn start-btn"
          >
            🚀 开始作答
          </button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">📚</div>
      <h3>暂无作业</h3>
      <p>目前还没有分配给您的作业</p>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination">
      <button 
        @click="goToPage(currentPage - 1)"
        :disabled="currentPage <= 1"
        class="page-btn"
      >
        上一页
      </button>
      
      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页
      </span>
      
      <button 
        @click="goToPage(currentPage + 1)"
        :disabled="currentPage >= totalPages"
        class="page-btn"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { submissionApi } from '@/api/submission'
import type { AssignmentItem, SubmissionStatus } from '@/types/submission'

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loading = ref(true)
const error = ref('')
const assignments = ref<AssignmentItem[]>([])
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 10

// 计算属性
const totalAssignments = computed(() => assignments.value.length)

// 获取作业列表
const fetchAssignments = async (page: number = 1) => {
  try {
    loading.value = true
    error.value = ''
    
    const response = await submissionApi.getStudentAssignments(page, pageSize)
    assignments.value = response.items
    currentPage.value = response.page
    totalPages.value = response.totalPages
  } catch (err) {
    console.error('获取作业列表失败:', err)
    error.value = err instanceof Error ? err.message : '获取作业列表失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 刷新列表
const refreshList = () => {
  fetchAssignments(currentPage.value)
}

// 翻页
const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    fetchAssignments(page)
  }
}

// 查看作业结果
const viewResult = (submissionId: string) => {
  router.push(`/submissions/${submissionId}/result`)
}

// 开始作答
const startAssignment = (assignmentId: string) => {
  router.push(`/assignment/${assignmentId}`)
}

// 继续作答
const continueAssignment = (assignmentId: string) => {
  router.push(`/assignment/${assignmentId}?continue=true`)
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取截止日期样式类
const getDueDateClass = (dueDate: string) => {
  const now = new Date()
  const due = new Date(dueDate)
  const diffHours = (due.getTime() - now.getTime()) / (1000 * 60 * 60)
  
  if (diffHours < 0) return 'overdue'
  if (diffHours < 24) return 'urgent'
  if (diffHours < 72) return 'warning'
  return 'normal'
}

// 获取状态文本
const getStatusText = (status: SubmissionStatus) => {
  const statusMap = {
    DRAFT: '草稿',
    SUBMITTED: '已提交',
    GRADED: '已批阅'
  }
  return statusMap[status] || status
}

// 获取状态样式类
const getStatusClass = (status: SubmissionStatus) => {
  const classMap = {
    DRAFT: 'status-draft',
    SUBMITTED: 'status-submitted',
    GRADED: 'status-graded'
  }
  return classMap[status] || ''
}

// 获取卡片样式类
const getCardClass = (status: SubmissionStatus) => {
  const classMap = {
    DRAFT: 'card-draft',
    SUBMITTED: 'card-submitted',
    GRADED: 'card-graded'
  }
  return classMap[status] || ''
}

// 监听认证状态，当认证完成后获取数据
watch(() => authStore.isAuthenticated, (isAuth) => {
  if (isAuth) {
    fetchAssignments()
  }
}, { immediate: true })

// 组件挂载时，如果已经认证则立即获取数据
onMounted(() => {
  if (authStore.isAuthenticated) {
    fetchAssignments()
  }
})
</script>

<style scoped>
.assignment-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.refresh-btn:hover:not(:disabled) {
  background-color: #2563eb;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-icon {
  font-size: 16px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #6b7280;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error {
  text-align: center;
  padding: 40px 20px;
  color: #dc2626;
}

.retry-btn {
  margin-top: 16px;
  padding: 10px 20px;
  background-color: #dc2626;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.retry-btn:hover {
  background-color: #b91c1c;
}

.assignments-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.assignment-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.assignment-card:hover {
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-draft {
  border-left: 4px solid #f59e0b;
}

.card-submitted {
  border-left: 4px solid #3b82f6;
}

.card-graded {
  border-left: 4px solid #10b981;
}

.assignment-info {
  flex: 1;
}

.assignment-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.assignment-description {
  color: #6b7280;
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.assignment-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-label {
  font-weight: 500;
  color: #374151;
  min-width: 80px;
}

.meta-value {
  color: #1f2937;
}

.meta-value.normal {
  color: #1f2937;
}

.meta-value.warning {
  color: #f59e0b;
}

.meta-value.urgent {
  color: #dc2626;
  font-weight: 600;
}

.meta-value.overdue {
  color: #dc2626;
  font-weight: 600;
  text-decoration: line-through;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-draft {
  background: #fef3c7;
  color: #92400e;
}

.status-submitted {
  background: #dbeafe;
  color: #1e40af;
}

.status-graded {
  background: #d1fae5;
  color: #065f46;
}

.score-value {
  font-weight: 600;
  color: #059669;
}

.assignment-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: 20px;
}

.action-btn {
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  min-width: 120px;
  text-align: center;
}

.view-result-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.view-result-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.start-btn {
  background: #10b981;
  color: white;
}

.start-btn:hover {
  background: #059669;
}

.continue-btn {
  background: #f59e0b;
  color: white;
}

.continue-btn:hover {
  background: #d97706;
}

.submitted-btn {
  background: #6b7280;
  color: white;
  cursor: not-allowed;
  opacity: 0.7;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #6b7280;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 20px;
  margin: 0 0 8px 0;
  color: #374151;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.page-btn {
  padding: 8px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #2563eb;
}

.page-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.page-info {
  color: #6b7280;
  font-size: 14px;
}

@media (max-width: 768px) {
  .assignment-list {
    padding: 16px;
  }
  
  .header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .title {
    font-size: 24px;
  }
  
  .assignment-card {
    flex-direction: column;
    gap: 16px;
  }
  
  .assignment-actions {
    margin-left: 0;
    flex-direction: row;
    justify-content: flex-end;
  }
  
  .action-btn {
    min-width: auto;
    flex: 1;
  }
  
  .assignment-meta {
    gap: 6px;
  }
  
  .meta-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .meta-label {
    min-width: auto;
    font-size: 12px;
  }
}
</style>