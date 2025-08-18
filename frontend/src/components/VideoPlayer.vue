<template>
  <div class="video-player">
    <div v-if="!videoUrl" class="no-video">
      <p>暂无视频讲解</p>
    </div>
    <div v-else class="video-container">
      <!-- YouTube 嵌入视频 -->
      <iframe 
        v-if="isYouTubeUrl"
        :src="videoUrl"
        class="video-element"
        frameborder="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen
        @load="onIframeLoad"
        @error="onError"
      ></iframe>
      
      <!-- 普通视频文件 -->
      <video 
        v-else
        ref="videoElement"
        :src="videoUrl"
        controls
        preload="metadata"
        class="video-element"
        @loadstart="onLoadStart"
        @loadeddata="onLoadedData"
        @error="onError"
      >
        您的浏览器不支持视频播放。
      </video>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="video-loading">
        <div class="loading-spinner"></div>
        <p>正在加载视频...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-if="error" class="video-error">
        <p>视频加载失败</p>
        <button @click="retryLoad" class="retry-button">重试</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed } from 'vue'

interface Props {
  videoUrl?: string
}

const props = defineProps<Props>()

// 响应式数据
const videoElement = ref<HTMLVideoElement | null>(null)
const loading = ref(false)
const error = ref(false)

// 重置视频状态
const resetVideoState = () => {
  loading.value = false
  error.value = false
}

// 检测是否为YouTube URL
const isYouTubeUrl = computed(() => {
  if (!props.videoUrl) return false
  return props.videoUrl.includes('youtube.com/embed/') || props.videoUrl.includes('youtu.be/')
})

// 监听视频URL变化
watch(
  () => props.videoUrl,
  (newUrl) => {
    if (newUrl) {
      resetVideoState()
    }
  },
  { immediate: true }
)

// 视频开始加载
const onLoadStart = () => {
  loading.value = true
  error.value = false
}

// 视频数据加载完成
const onLoadedData = () => {
  loading.value = false
  error.value = false
}

// iframe加载完成
const onIframeLoad = () => {
  loading.value = false
  error.value = false
}

// 视频加载错误
const onError = () => {
  loading.value = false
  error.value = true
}

// 重试加载
const retryLoad = async () => {
  if (isYouTubeUrl.value) {
    // 对于YouTube视频，重新加载iframe
    resetVideoState()
    await nextTick()
    // 强制重新加载iframe
    const iframe = document.querySelector('.video-element') as HTMLIFrameElement
    if (iframe && props.videoUrl) {
      iframe.src = iframe.src
    }
  } else if (videoElement.value && props.videoUrl) {
    // 对于普通视频文件
    resetVideoState()
    await nextTick()
    videoElement.value.load()
  }
}
</script>

<style scoped>
.video-player {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.no-video {
  text-align: center;
  padding: 40px 20px;
  background: #f9fafb;
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  color: #6b7280;
}

.video-container {
  position: relative;
  width: 100%;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.video-element {
  width: 100%;
  height: auto;
  min-height: 300px;
  display: block;
}

.video-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  z-index: 10;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top: 3px solid #ffffff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.video-error {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  z-index: 10;
}

.video-error p {
  margin: 0 0 16px 0;
  font-size: 16px;
}

.retry-button {
  padding: 8px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.retry-button:hover {
  background: #2563eb;
}

@media (max-width: 768px) {
  .video-element {
    min-height: 200px;
  }
  
  .no-video {
    padding: 30px 16px;
  }
}
</style>