<template>
  <div class="teacher-assignments">
    <!-- 页面标题 -->
    <div class="header">
      <h1 class="title">作业管理</h1>
      <div class="header-actions">
        <button @click="showCreateModal = true" class="create-btn">
          <span class="btn-icon">➕</span>
          创建作业
        </button>
        <button @click="refreshList" class="refresh-btn" :disabled="loading">
          <span class="refresh-icon">🔄</span>
          刷新
        </button>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filters">
      <div class="filter-group">
        <label>状态筛选：</label>
        <select v-model="statusFilter" @change="filterAssignments">
          <option value="">全部</option>
          <option value="DRAFT">草稿</option>
          <option value="PUBLISHED">已发布</option>
          <option value="CLOSED">已关闭</option>
        </select>
      </div>
      <div class="filter-group">
        <label>搜索：</label>
        <input 
          v-model="searchQuery" 
          @input="filterAssignments"
          type="text" 
          placeholder="搜索作业标题..."
          class="search-input"
        />
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
      <button @click="loadAssignments" class="retry-btn">重试</button>
    </div>

    <!-- 作业列表 -->
    <div v-else class="assignments-grid">
      <div v-if="filteredAssignments.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <h3>暂无作业</h3>
        <p>点击"创建作业"按钮开始创建您的第一个作业</p>
      </div>
      
      <div 
        v-for="assignment in filteredAssignments" 
        :key="assignment.id"
        class="assignment-card"
      >
        <div class="card-header">
          <h3 class="assignment-title">{{ assignment.title }}</h3>
          <div class="status-badge" :class="getStatusClass(assignment.status)">
            {{ getStatusText(assignment.status) }}
          </div>
        </div>
        
        <div class="card-content">
          <p class="assignment-description">{{ assignment.description }}</p>
          
          <div class="assignment-meta">
            <div class="meta-item">
              <span class="meta-label">创建时间：</span>
              <span class="meta-value">{{ formatDate(assignment.createdAt) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">截止时间：</span>
              <span class="meta-value">{{ formatDate(assignment.dueDate) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">提交数量：</span>
              <span class="meta-value">{{ assignment.submissionCount || 0 }}</span>
            </div>
          </div>
        </div>
        
        <div class="card-actions">
          <button @click="editAssignment(assignment)" class="action-btn edit-btn">
            ✏️ 编辑
          </button>
          <button 
            v-if="assignment.status === 'DRAFT'"
            @click="publishAssignment(assignment.id)" 
            class="action-btn publish-btn"
          >
            📢 发布
          </button>
          <button 
            v-if="assignment.status === 'PUBLISHED'"
            @click="viewSubmissions(assignment.id)" 
            class="action-btn view-btn"
          >
            📊 查看提交
          </button>
          <button @click="deleteAssignment(assignment.id)" class="action-btn delete-btn">
            🗑️ 删除
          </button>
        </div>
      </div>
    </div>

    <!-- 创建作业模态框 -->
    <div v-if="showCreateModal" class="modal-overlay" @click="closeCreateModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>创建新作业</h2>
          <button @click="closeCreateModal" class="close-btn">✕</button>
        </div>
        
        <form @submit.prevent="createAssignment" class="modal-content">
          <div class="form-group">
            <label for="title">作业标题 *</label>
            <input 
              id="title"
              v-model="newAssignment.title" 
              type="text" 
              required 
              placeholder="请输入作业标题"
            />
          </div>
          
          <div class="form-group">
            <label for="description">作业描述</label>
            <textarea 
              id="description"
              v-model="newAssignment.description" 
              rows="4" 
              placeholder="请输入作业描述"
            ></textarea>
          </div>
          
          <div class="form-group">
            <label for="dueDate">截止时间 *</label>
            <input 
              id="dueDate"
              v-model="newAssignment.dueDate" 
              type="datetime-local" 
              required
            />
          </div>
          
          <div class="form-group">
            <label for="maxScore">总分</label>
            <input 
              id="maxScore"
              v-model.number="newAssignment.maxScore" 
              type="number" 
              min="1" 
              placeholder="100"
            />
          </div>
          
          <div class="modal-actions">
            <button type="button" @click="closeCreateModal" class="cancel-btn">
              取消
            </button>
            <button type="submit" class="submit-btn" :disabled="creating">
              {{ creating ? '创建中...' : '创建作业' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const loading = ref(true)
const error = ref('')
const assignments = ref<Array<{
  id: string
  title: string
  description: string
  status: 'DRAFT' | 'PUBLISHED' | 'CLOSED'
  createdAt: string
  dueDate: string
  maxScore: number
  submissionCount?: number
}>>([])

// 筛选和搜索
const statusFilter = ref('')
const searchQuery = ref('')

// 创建作业模态框
const showCreateModal = ref(false)
const creating = ref(false)
const newAssignment = ref({
  title: '',
  description: '',
  dueDate: '',
  maxScore: 100
})

// 计算属性
const filteredAssignments = computed(() => {
  let filtered = assignments.value
  
  // 状态筛选
  if (statusFilter.value) {
    filtered = filtered.filter(a => a.status === statusFilter.value)
  }
  
  // 搜索筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(a => 
      a.title.toLowerCase().includes(query) ||
      a.description.toLowerCase().includes(query)
    )
  }
  
  return filtered
})

// 方法
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusClass = (status: string) => {
  const classes = {
    'DRAFT': 'status-draft',
    'PUBLISHED': 'status-published',
    'CLOSED': 'status-closed'
  }
  return classes[status as keyof typeof classes] || ''
}

const getStatusText = (status: string) => {
  const texts = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'CLOSED': '已关闭'
  }
  return texts[status as keyof typeof texts] || status
}

const loadAssignments = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    assignments.value = [
      {
        id: '1',
        title: '数学练习题第一章',
        description: '完成教材第一章的所有练习题，包括基础题和提高题。',
        status: 'PUBLISHED',
        createdAt: '2024-01-15T10:00:00Z',
        dueDate: '2024-01-25T23:59:00Z',
        maxScore: 100,
        submissionCount: 12
      },
      {
        id: '2',
        title: '英语阅读理解',
        description: '阅读指定文章并回答相关问题。',
        status: 'DRAFT',
        createdAt: '2024-01-16T14:30:00Z',
        dueDate: '2024-01-30T23:59:00Z',
        maxScore: 80,
        submissionCount: 0
      },
      {
        id: '3',
        title: '物理实验报告',
        description: '完成光学实验并提交实验报告。',
        status: 'PUBLISHED',
        createdAt: '2024-01-10T09:00:00Z',
        dueDate: '2024-01-20T23:59:00Z',
        maxScore: 120,
        submissionCount: 8
      }
    ]
  } catch (err) {
    error.value = err instanceof Error ? err.message : '加载作业列表失败'
  } finally {
    loading.value = false
  }
}

const refreshList = () => {
  loadAssignments()
}

const filterAssignments = () => {
  // 筛选逻辑已在计算属性中处理
}

const createAssignment = async () => {
  try {
    creating.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 添加到列表
    const newId = Date.now().toString()
    assignments.value.unshift({
      id: newId,
      title: newAssignment.value.title,
      description: newAssignment.value.description,
      status: 'DRAFT',
      createdAt: new Date().toISOString(),
      dueDate: newAssignment.value.dueDate,
      maxScore: newAssignment.value.maxScore,
      submissionCount: 0
    })
    
    // 重置表单
    newAssignment.value = {
      title: '',
      description: '',
      dueDate: '',
      maxScore: 100
    }
    
    closeCreateModal()
  } catch (err) {
    console.error('创建作业失败:', err)
    alert('创建作业失败，请重试')
  } finally {
    creating.value = false
  }
}

const editAssignment = (assignment: any) => {
  // 跳转到编辑页面或打开编辑模态框
  console.log('编辑作业:', assignment.id)
  alert('编辑功能开发中...')
}

const publishAssignment = async (assignmentId: string) => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 更新状态
    const assignment = assignments.value.find(a => a.id === assignmentId)
    if (assignment) {
      assignment.status = 'PUBLISHED'
    }
    
    alert('作业发布成功！')
  } catch (err) {
    console.error('发布作业失败:', err)
    alert('发布作业失败，请重试')
  }
}

const viewSubmissions = (assignmentId: string) => {
  router.push(`/teacher/submissions?assignment=${assignmentId}`)
}

const deleteAssignment = async (assignmentId: string) => {
  if (!confirm('确定要删除这个作业吗？此操作不可恢复。')) {
    return
  }
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 从列表中移除
    const index = assignments.value.findIndex(a => a.id === assignmentId)
    if (index > -1) {
      assignments.value.splice(index, 1)
    }
    
    alert('作业删除成功！')
  } catch (err) {
    console.error('删除作业失败:', err)
    alert('删除作业失败，请重试')
  }
}

const closeCreateModal = () => {
  showCreateModal.value = false
  newAssignment.value = {
    title: '',
    description: '',
    dueDate: '',
    maxScore: 100
  }
}

onMounted(() => {
  loadAssignments()
})
</script>

<style scoped>
.teacher-assignments {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.create-btn, .refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.create-btn {
  background: #3b82f6;
  color: white;
}

.create-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.refresh-btn {
  background: #f3f4f6;
  color: #374151;
}

.refresh-btn:hover {
  background: #e5e7eb;
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.filters {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-weight: 500;
  color: #374151;
  white-space: nowrap;
}

.filter-group select,
.search-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}

.search-input {
  min-width: 200px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px;
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

.error {
  text-align: center;
  padding: 60px;
  color: #dc2626;
}

.retry-btn {
  margin-top: 12px;
  padding: 8px 16px;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.assignments-grid {
  display: grid;
  gap: 20px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #6b7280;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 1.5rem;
  margin-bottom: 8px;
  color: #374151;
}

.assignment-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.assignment-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px -3px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.assignment-title {
  font-size: 1.25rem;
  font-weight: bold;
  color: #1f2937;
  margin: 0;
  flex: 1;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  white-space: nowrap;
}

.status-draft {
  background: #fef3c7;
  color: #92400e;
}

.status-published {
  background: #d1fae5;
  color: #065f46;
}

.status-closed {
  background: #fee2e2;
  color: #991b1b;
}

.assignment-description {
  color: #6b7280;
  margin-bottom: 16px;
  line-height: 1.5;
}

.assignment-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-label {
  font-weight: 500;
  color: #374151;
  font-size: 0.9rem;
}

.meta-value {
  color: #6b7280;
  font-size: 0.9rem;
}

.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-btn {
  background: #f59e0b;
  color: white;
}

.edit-btn:hover {
  background: #d97706;
}

.publish-btn {
  background: #10b981;
  color: white;
}

.publish-btn:hover {
  background: #059669;
}

.view-btn {
  background: #3b82f6;
  color: white;
}

.view-btn:hover {
  background: #2563eb;
}

.delete-btn {
  background: #ef4444;
  color: white;
}

.delete-btn:hover {
  background: #dc2626;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #1f2937;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
  padding: 4px;
}

.close-btn:hover {
  color: #374151;
}

.modal-content {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  box-sizing: border-box;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.cancel-btn,
.submit-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn {
  background: #f3f4f6;
  color: #374151;
}

.cancel-btn:hover {
  background: #e5e7eb;
}

.submit-btn {
  background: #3b82f6;
  color: white;
}

.submit-btn:hover {
  background: #2563eb;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>