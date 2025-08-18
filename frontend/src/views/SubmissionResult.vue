<template>
  <div class="submission-result">
    <!-- 页面标题 -->
    <div class="header">
      <h1 class="title">作业结果详情</h1>
      <button @click="goBack" class="back-btn">
        ← 返回作业列表
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>正在加载作业结果...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchSubmissionResult" class="retry-btn">重试</button>
    </div>

    <!-- 作业结果内容 -->
    <div v-else-if="submissionResult" class="result-content">
      <!-- 总体信息 -->
      <div class="summary-card">
        <h2>总体评价</h2>
        <div class="summary-info">
          <div class="score-section">
            <span class="label">总分：</span>
            <span class="score">{{ submissionResult.totalScore }}/{{ submissionResult.maxScore }}</span>
            <span class="percentage">({{ scorePercentage }}%)</span>
          </div>
          <div class="comment-section" v-if="submissionResult.teacherComment || submissionResult.teacherFeedback">
            <span class="label">教师评语：</span>
            <p class="comment">{{ submissionResult.teacherFeedback || submissionResult.teacherComment }}</p>
          </div>
        </div>
      </div>

      <!-- 逐题详情 -->
      <div class="questions-section">
        <h2>逐题详情</h2>
        <div 
          v-for="(question, index) in submissionResult.questions" 
          :key="question.questionId"
          class="question-card"
        >
          <div class="question-header">
            <h3>第{{ index + 1 }}题</h3>
            <span class="question-score">
              {{ question.score }}/{{ question.maxScore }}分
            </span>
          </div>

          <!-- 题目内容 -->
          <div class="question-content">
            <h4>题目：</h4>
            <p class="content-text">{{ question.content }}</p>
          </div>

          <!-- 答案对比 -->
          <div class="answer-comparison">
            <h4>答案对比：</h4>
            
            <!-- 学生答案 -->
            <div class="student-answer">
              <h5>我的答案：</h5>
              <p class="answer-text">{{ question.studentAnswer || '未作答' }}</p>
            </div>

            <!-- 标准答案 -->
            <div class="standard-answer">
              <h5>正确答案：</h5>
              <p class="answer-text">{{ question.standardAnswer }}</p>
            </div>
          </div>

          <!-- 文字解析 -->
          <div v-if="question.explanation" class="explanation">
            <h4>解析：</h4>
            <p class="explanation-text">{{ question.explanation }}</p>
          </div>

          <!-- 视频讲解 -->
          <div v-if="question.videoUrl" class="video-section">
            <h4>视频讲解：</h4>
            <VideoPlayer :video-url="question.videoUrl" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import VideoPlayer from '@/components/VideoPlayer.vue'
import { submissionApi } from '@/api/submission'
import type { SubmissionResult } from '@/types/submission'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const error = ref('')
const submissionResult = ref<SubmissionResult | null>(null)

// 计算属性
const scorePercentage = computed(() => {
  if (!submissionResult.value) return 0
  return Math.round((submissionResult.value.totalScore / submissionResult.value.maxScore) * 100)
})

// 获取作业结果数据
const fetchSubmissionResult = async () => {
  try {
    loading.value = true
    error.value = ''
    
    const submissionId = route.params.submissionId as string
    if (!submissionId) {
      throw new Error('缺少作业提交ID')
    }

    const result = await submissionApi.getSubmissionResult(submissionId)
    submissionResult.value = result
  } catch (err) {
    console.error('获取作业结果失败:', err)
    error.value = err instanceof Error ? err.message : '获取作业结果失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 返回作业列表
const goBack = () => {
  router.push('/assignments')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchSubmissionResult()
})
</script>

<style scoped>
.submission-result {
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

.back-btn {
  padding: 10px 20px;
  background-color: #6b7280;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.back-btn:hover {
  background-color: #4b5563;
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

.result-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.summary-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.summary-card h2 {
  margin: 0 0 20px 0;
  font-size: 24px;
  font-weight: 600;
}

.summary-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.score-section {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
}

.label {
  font-weight: 600;
}

.score {
  font-size: 24px;
  font-weight: 700;
}

.percentage {
  font-size: 16px;
  opacity: 0.9;
}

.comment-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment {
  background: rgba(255, 255, 255, 0.1);
  padding: 16px;
  border-radius: 8px;
  margin: 0;
  line-height: 1.6;
}

.questions-section h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 20px 0;
}

.question-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s;
}

.question-card:hover {
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.question-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.question-score {
  background: #3b82f6;
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.question-content,
.answer-comparison,
.student-answer,
.standard-answer,
.explanation {
  margin-bottom: 20px;
}

.answer-comparison {
  background-color: #f9fafb;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
}

.answer-comparison h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.answer-comparison .student-answer,
.answer-comparison .standard-answer {
  margin-bottom: 12px;
  padding: 12px;
  background-color: white;
  border-radius: 6px;
}

.question-content h4,
.student-answer h5,
.standard-answer h5,
.explanation h4,
.video-section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.content-text,
.answer-text,
.explanation-text {
  margin: 0;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  line-height: 1.6;
  color: #1f2937;
}

.student-answer .answer-text {
  background: #fef3c7;
  border-left: 4px solid #f59e0b;
}

.standard-answer .answer-text {
  background: #d1fae5;
  border-left: 4px solid #10b981;
}

.explanation-text {
  background: #e0f2fe;
  border-left: 4px solid #0284c7;
}

.video-section {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .submission-result {
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
  
  .question-card {
    padding: 16px;
  }
  
  .question-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>