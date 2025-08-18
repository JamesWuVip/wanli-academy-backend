// 作业提交相关类型定义

// 题目结果详情
export interface QuestionResult {
  questionId: string
  content: string
  studentAnswer: string | null
  standardAnswer: string
  score: number
  maxScore: number
  explanation?: string
  videoUrl?: string
}

// 作业结果详情
export interface SubmissionResult {
  submissionId: string
  homeworkId: string
  homeworkTitle: string
  studentId: string
  studentName: string
  totalScore: number
  maxScore: number
  teacherComment?: string
  teacherFeedback?: string
  submittedAt: string
  gradedAt?: string
  questions: QuestionResult[]
}

// 作业提交状态
export enum SubmissionStatus {
  SUBMITTED = 'SUBMITTED',
  GRADED = 'GRADED',
  DRAFT = 'DRAFT'
}

// 作业列表项
export interface AssignmentItem {
  id: string
  title: string
  description?: string
  dueDate: string
  status: SubmissionStatus
  submissionId?: string
  score?: number
  maxScore?: number
  submittedAt?: string
  gradedAt?: string
}

// API响应基础类型
export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
  error?: string
}

// 分页响应
export interface PaginatedResponse<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}