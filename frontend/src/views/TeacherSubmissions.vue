<template>
  <div class="teacher-submissions">
    <!-- 页面标题 -->
    <div class="header">
      <h1 class="title">学生提交管理</h1>
      <div class="header-actions">
        <button @click="exportGrades" class="export-btn">
          <span class="btn-icon">📊</span>
          导出成绩
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
        <label>作业筛选：</label>
        <select v-model="assignmentFilter" @change="filterSubmissions">
          <option value="">全部作业</option>
          <option v-for="assignment in assignments" :key="assignment.id" :value="assignment.id">
            {{ assignment.title }}
          </option>
        </select>
      </div>
      <div class="filter-group">
        <label>状态筛选：</label>
        <select v-model="statusFilter" @change="filterSubmissions">
          <option value="">全部状态</option>
          <option value="SUBMITTED">已提交</option>
          <option value="GRADED">已评分</option>
          <option value="RETURNED">已返回</option>
        </select>
      </div>
      <div class="filter-group">
        <label>搜索学生：</label>
        <input 
          v-model="searchQuery" 
          @input="filterSubmissions"
          type="text" 
          placeholder="搜索学生姓名..."
          class="search-input"
        />
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="stats">
      <div class="stat-card">
        <div class="stat-number">{{ filteredSubmissions.length }}</div>
        <div class="stat-label">总提交数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ gradedCount }}</div>
        <div class="stat-label">已评分</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ pendingCount }}</div>
        <div class="stat-label">待评分</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ averageScore.toFixed(1) }}</div>
        <div class="stat-label">平均分</div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>正在加载提交列表...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="loadSubmissions" class="retry-btn">重试</button>
    </div>

    <!-- 提交列表 -->
    <div v-else class="submissions-list">
      <div v-if="filteredSubmissions.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <h3>暂无提交</h3>
        <p>当前筛选条件下没有找到学生提交</p>
      </div>
      
      <div 
        v-for="submission in filteredSubmissions" 
        :key="submission.id"
        class="submission-card"
      >
        <div class="card-header">
          <div class="student-info">
            <div class="student-avatar">{{ getInitials(submission.studentName) }}</div>
            <div class="student-details">
              <h3 class="student-name">{{ submission.studentName }}</h3>
              <p class="student-id">学号: {{ submission.studentId }}</p>
            </div>
          </div>
          <div class="submission-status">
            <div class="status-badge" :class="getStatusClass(submission.status)">
              {{ getStatusText(submission.status) }}
            </div>
            <div class="submission-time">{{ formatDate(submission.submittedAt) }}</div>
          </div>
        </div>
        
        <div class="card-content">
          <div class="assignment-info">
            <h4 class="assignment-title">{{ submission.assignmentTitle }}</h4>
            <p class="assignment-description">{{ submission.assignmentDescription }}</p>
          </div>
          
          <div class="submission-details">
            <div class="detail-item">
              <span class="detail-label">提交时间：</span>
              <span class="detail-value">{{ formatDate(submission.submittedAt) }}</span>
            </div>
            <div class="detail-item" v-if="submission.score !== null">
              <span class="detail-label">得分：</span>
              <span class="detail-value score">{{ submission.score }}/{{ submission.maxScore }}</span>
            </div>
            <div class="detail-item" v-if="submission.gradedAt">
              <span class="detail-label">评分时间：</span>
              <span class="detail-value">{{ formatDate(submission.gradedAt) }}</span>
            </div>
          </div>
          
          <div v-if="submission.feedback" class="feedback">
            <h5>教师反馈：</h5>
            <p>{{ submission.feedback }}</p>
          </div>
        </div>
        
        <div class="card-actions">
          <button @click="viewSubmission(submission)" class="action-btn view-btn">
            👁️ 查看详情
          </button>
          <button 
            v-if="submission.status !== 'GRADED'"
            @click="gradeSubmission(submission)" 
            class="action-btn grade-btn"
          >
            ✏️ 评分
          </button>
          <button 
            v-else
            @click="editGrade(submission)" 
            class="action-btn edit-grade-btn"
          >
            📝 修改评分
          </button>
          <button @click="downloadSubmission(submission.id)" class="action-btn download-btn">
            💾 下载
          </button>
        </div>
      </div>
    </div>

    <!-- 评分模态框 -->
    <div v-if="showGradeModal" class="modal-overlay" @click="closeGradeModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>{{ isEditingGrade ? '修改评分' : '评分作业' }}</h2>
          <button @click="closeGradeModal" class="close-btn">✕</button>
        </div>
        
        <div class="modal-content">
          <div class="grading-info">
            <h3>{{ currentSubmission?.studentName }}</h3>
            <p>作业：{{ currentSubmission?.assignmentTitle }}</p>
            <p>提交时间：{{ formatDate(currentSubmission?.submittedAt) }}</p>
          </div>
          
          <form @submit.prevent="submitGrade" class="grading-form">
            <div class="form-group">
              <label for="score">分数 (满分: {{ currentSubmission?.maxScore }})</label>
              <input 
                id="score"
                v-model.number="gradeForm.score" 
                type="number" 
                :min="0"
                :max="currentSubmission?.maxScore"
                required 
                placeholder="请输入分数"
              />
            </div>
            
            <div class="form-group">
              <label for="feedback">反馈意见</label>
              <textarea 
                id="feedback"
                v-model="gradeForm.feedback" 
                rows="6" 
                placeholder="请输入对学生作业的反馈意见..."
              ></textarea>
            </div>
            
            <div class="modal-actions">
              <button type="button" @click="closeGradeModal" class="cancel-btn">
                取消
              </button>
              <button type="submit" class="submit-btn" :disabled="grading">
                {{ grading ? '提交中...' : (isEditingGrade ? '更新评分' : '提交评分') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 响应式数据
const loading = ref(true)
const error = ref('')
const submissions = ref<Array<{
  id: string
  studentId: string
  studentName: string
  assignmentId: string
  assignmentTitle: string
  assignmentDescription: string
  submittedAt: string
  status: 'SUBMITTED' | 'GRADED' | 'RETURNED'
  score: number | null
  maxScore: number
  feedback: string
  gradedAt: string | null
}>>([])

const assignments = ref<Array<{
  id: string
  title: string
}>>([])

// 筛选和搜索
const assignmentFilter = ref('')
const statusFilter = ref('')
const searchQuery = ref('')

// 评分模态框
const showGradeModal = ref(false)
const grading = ref(false)
const isEditingGrade = ref(false)
const currentSubmission = ref<any>(null)
const gradeForm = ref({
  score: 0,
  feedback: ''
})

// 计算属性
const filteredSubmissions = computed(() => {
  let filtered = submissions.value
  
  // 作业筛选
  if (assignmentFilter.value) {
    filtered = filtered.filter(s => s.assignmentId === assignmentFilter.value)
  }
  
  // 状态筛选
  if (statusFilter.value) {
    filtered = filtered.filter(s => s.status === statusFilter.value)
  }
  
  // 搜索筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(s => 
      s.studentName.toLowerCase().includes(query) ||
      s.studentId.toLowerCase().includes(query)
    )
  }
  
  return filtered
})

const gradedCount = computed(() => {
  return filteredSubmissions.value.filter(s => s.status === 'GRADED').length
})

const pendingCount = computed(() => {
  return filteredSubmissions.value.filter(s => s.status === 'SUBMITTED').length
})

const averageScore = computed(() => {
  const gradedSubmissions = filteredSubmissions.value.filter(s => s.score !== null)
  if (gradedSubmissions.length === 0) return 0
  
  const totalScore = gradedSubmissions.reduce((sum, s) => sum + (s.score || 0), 0)
  return totalScore / gradedSubmissions.length
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

const getInitials = (name: string) => {
  return name.split('').slice(0, 2).join('').toUpperCase()
}

const getStatusClass = (status: string) => {
  const classes = {
    'SUBMITTED': 'status-submitted',
    'GRADED': 'status-graded',
    'RETURNED': 'status-returned'
  }
  return classes[status as keyof typeof classes] || ''
}

const getStatusText = (status: string) => {
  const texts = {
    'SUBMITTED': '已提交',
    'GRADED': '已评分',
    'RETURNED': '已返回'
  }
  return texts[status as keyof typeof texts] || status
}

const loadSubmissions = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    submissions.value = [
      {
        id: '1',
        studentId: '2021001',
        studentName: '张三',
        assignmentId: '1',
        assignmentTitle: '数学练习题第一章',
        assignmentDescription: '完成教材第一章的所有练习题',
        submittedAt: '2024-01-20T14:30:00Z',
        status: 'GRADED',
        score: 85,
        maxScore: 100,
        feedback: '整体完成得不错，但第5题的解题思路需要改进。',
        gradedAt: '2024-01-21T10:15:00Z'
      },
      {
        id: '2',
        studentId: '2021002',
        studentName: '李四',
        assignmentId: '1',
        assignmentTitle: '数学练习题第一章',
        assignmentDescription: '完成教材第一章的所有练习题',
        submittedAt: '2024-01-22T16:45:00Z',
        status: 'SUBMITTED',
        score: null,
        maxScore: 100,
        feedback: '',
        gradedAt: null
      },
      {
        id: '3',
        studentId: '2021003',
        studentName: '王五',
        assignmentId: '3',
        assignmentTitle: '物理实验报告',
        assignmentDescription: '完成光学实验并提交实验报告',
        submittedAt: '2024-01-19T11:20:00Z',
        status: 'GRADED',
        score: 92,
        maxScore: 120,
        feedback: '实验数据记录详细，分析透彻，是一份优秀的实验报告。',
        gradedAt: '2024-01-20T09:30:00Z'
      }
    ]
    
    assignments.value = [
      { id: '1', title: '数学练习题第一章' },
      { id: '2', title: '英语阅读理解' },
      { id: '3', title: '物理实验报告' }
    ]
    
    // 如果URL中有作业ID参数，自动筛选
    const assignmentId = route.query.assignment as string
    if (assignmentId) {
      assignmentFilter.value = assignmentId
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '加载提交列表失败'
  } finally {
    loading.value = false
  }
}

const refreshList = () => {
  loadSubmissions()
}

const filterSubmissions = () => {
  // 筛选逻辑已在计算属性中处理
}

const viewSubmission = (submission: any) => {
  console.log('查看提交详情:', submission.id)
  alert('查看详情功能开发中...')
}

const gradeSubmission = (submission: any) => {
  currentSubmission.value = submission
  isEditingGrade.value = false
  gradeForm.value = {
    score: 0,
    feedback: ''
  }
  showGradeModal.value = true
}

const editGrade = (submission: any) => {
  currentSubmission.value = submission
  isEditingGrade.value = true
  gradeForm.value = {
    score: submission.score || 0,
    feedback: submission.feedback || ''
  }
  showGradeModal.value = true
}

const submitGrade = async () => {
  try {
    grading.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新提交记录
    const submission = submissions.value.find(s => s.id === currentSubmission.value.id)
    if (submission) {
      submission.score = gradeForm.value.score
      submission.feedback = gradeForm.value.feedback
      submission.status = 'GRADED'
      submission.gradedAt = new Date().toISOString()
    }
    
    closeGradeModal()
    alert(isEditingGrade.value ? '评分更新成功！' : '评分提交成功！')
  } catch (err) {
    console.error('提交评分失败:', err)
    alert('提交评分失败，请重试')
  } finally {
    grading.value = false
  }
}

const downloadSubmission = (submissionId: string) => {
  console.log('下载提交文件:', submissionId)
  alert('下载功能开发中...')
}

const exportGrades = () => {
  console.log('导出成绩')
  alert('导出功能开发中...')
}

const closeGradeModal = () => {
  showGradeModal.value = false
  currentSubmission.value = null
  gradeForm.value = {
    score: 0,
    feedback: ''
  }
}

onMounted(() => {
  loadSubmissions()
})
</script>

<style scoped>
.teacher-submissions {
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

.export-btn, .refresh-btn {
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

.export-btn {
  background: #10b981;
  color: white;
}

.export-btn:hover {
  background: #059669;
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
  flex-wrap: wrap;
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

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-number {
  font-size: 2.5rem;
  font-weight: bold;
  color: #3b82f6;
  margin-bottom: 8px;
}

.stat-label {
  color: #6b7280;
  font-size: 0.9rem;
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

.submissions-list {
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

.submission-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.submission-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px -3px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #3b82f6;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 1.1rem;
}

.student-name {
  font-size: 1.2rem;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.student-id {
  color: #6b7280;
  font-size: 0.9rem;
  margin: 0;
}

.submission-status {
  text-align: right;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  white-space: nowrap;
  margin-bottom: 8px;
  display: inline-block;
}

.status-submitted {
  background: #fef3c7;
  color: #92400e;
}

.status-graded {
  background: #d1fae5;
  color: #065f46;
}

.status-returned {
  background: #dbeafe;
  color: #1e40af;
}

.submission-time {
  color: #6b7280;
  font-size: 0.8rem;
}

.assignment-title {
  font-size: 1.1rem;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.assignment-description {
  color: #6b7280;
  margin-bottom: 16px;
  line-height: 1.5;
}

.submission-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-label {
  font-weight: 500;
  color: #374151;
  font-size: 0.9rem;
}

.detail-value {
  color: #6b7280;
  font-size: 0.9rem;
}

.detail-value.score {
  font-weight: bold;
  color: #059669;
}

.feedback {
  background: #f9fafb;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.feedback h5 {
  margin: 0 0 8px 0;
  color: #374151;
  font-size: 0.9rem;
}

.feedback p {
  margin: 0;
  color: #6b7280;
  line-height: 1.5;
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

.view-btn {
  background: #3b82f6;
  color: white;
}

.view-btn:hover {
  background: #2563eb;
}

.grade-btn {
  background: #f59e0b;
  color: white;
}

.grade-btn:hover {
  background: #d97706;
}

.edit-grade-btn {
  background: #10b981;
  color: white;
}

.edit-grade-btn:hover {
  background: #059669;
}

.download-btn {
  background: #6b7280;
  color: white;
}

.download-btn:hover {
  background: #4b5563;
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
  max-width: 600px;
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

.grading-info {
  background: #f9fafb;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.grading-info h3 {
  margin: 0 0 8px 0;
  color: #1f2937;
}

.grading-info p {
  margin: 4px 0;
  color: #6b7280;
  font-size: 0.9rem;
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
  min-height: 120px;
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