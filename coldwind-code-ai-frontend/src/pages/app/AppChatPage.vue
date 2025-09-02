<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { getAppVoById, chatToGenCode, deployApp } from '@/api/appController.ts'
import { message } from 'ant-design-vue'
import {
  SendOutlined,
  CloudUploadOutlined,
  ReloadOutlined,
  EyeOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 响应式数据
const appData = ref<API.AppVO>({} as API.AppVO)
const loading = ref(false)
const chatLoading = ref(false)
const deployLoading = ref(false)
const deployUrl = ref('')

// 对话相关
const messages = ref<
  Array<{
    id: string
    type: 'user' | 'ai'
    content: string
    timestamp: Date
  }>
>([])
const currentMessage = ref('')
const isGenerating = ref(false)
const generatedCode = ref('')

// 计算属性
const appId = computed(() => route.params.id as string)
const isOwner = computed(() => appData.value.userId === loginUserStore.loginUser.id)
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')
const canEdit = computed(() => isOwner.value || isAdmin.value)

// 获取应用信息
const fetchAppInfo = async () => {
  if (!appId.value) return

  loading.value = true
  try {
    const res = await getAppVoById({ id: parseInt(appId.value) })
    if (res.data.code === 0) {
      appData.value = res.data.data!
      // 如果有初始提示词，自动发送给AI
      if (appData.value.initPrompt && messages.value.length === 0) {
        await sendMessage(appData.value.initPrompt)
      }
    } else {
      message.error('获取应用信息失败：' + res.data.message)
    }
  } catch (error) {
    message.error('获取应用信息失败')
    console.error('获取应用信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 发送消息
const sendMessage = async (content: string) => {
  if (!content.trim() || isGenerating.value) return

  const userMessage = {
    id: Date.now().toString(),
    type: 'user' as const,
    content: content.trim(),
    timestamp: new Date(),
  }

  messages.value.push(userMessage)
  currentMessage.value = ''

  // 添加AI回复占位
  const aiMessageId = (Date.now() + 1).toString()
  const aiMessage = {
    id: aiMessageId,
    type: 'ai' as const,
    content: '',
    timestamp: new Date(),
  }
  messages.value.push(aiMessage)

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 调用AI接口
  await generateCode(content.trim(), aiMessageId)
}

// 生成代码
const generateCode = async (userMessage: string, aiMessageId: string) => {
  if (!appId.value) return

  isGenerating.value = true
  chatLoading.value = true

  try {
    // 使用正确的API路径
    const response = await fetch(
      `/api/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(userMessage)}`,
    )

    if (!response.ok) {
      throw new Error('网络请求失败')
    }

    const reader = response.body?.getReader()
    if (!reader) {
      throw new Error('无法读取响应流')
    }

    let fullContent = ''

    while (true) {
      const { done, value } = await reader.read()

      if (done) break

      const chunk = new TextDecoder().decode(value)
      const lines = chunk.split('\n')

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6)
          if (data === '[DONE]') {
            // 生成完成，更新网页展示
            isGenerating.value = false
            chatLoading.value = false
            updateWebPreview(fullContent)
            return
          }

          try {
            const parsed = JSON.parse(data)
            if (parsed.content) {
              fullContent += parsed.content
              // 更新AI消息内容
              const aiMessage = messages.value.find((msg) => msg.id === aiMessageId)
              if (aiMessage) {
                aiMessage.content = fullContent
              }
              scrollToBottom()
            }
          } catch (e) {
            // 忽略解析错误
          }
        }
      }
    }
  } catch (error) {
    message.error('生成代码失败')
    console.error('生成代码失败:', error)
    isGenerating.value = false
    chatLoading.value = false
  }
}

// 更新网页预览
const updateWebPreview = (code: string) => {
  generatedCode.value = code
  // 这里可以根据生成的代码类型来展示不同的预览
  // 目前简单显示代码内容
}

// 部署应用
const handleDeploy = async () => {
  if (!appId.value) return

  deployLoading.value = true
  try {
    const res = await deployApp({ appId: parseInt(appId.value) })
    if (res.data.code === 0) {
      deployUrl.value = res.data.data!
      message.success('部署成功！')
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } catch (error) {
    message.error('部署失败，请稍后重试')
    console.error('部署失败:', error)
  } finally {
    deployLoading.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  const chatContainer = document.querySelector('.chat-messages')
  if (chatContainer) {
    chatContainer.scrollTop = chatContainer.scrollHeight
  }
}

// 组件挂载时获取应用信息
onMounted(() => {
  fetchAppInfo()
})
</script>

<template>
  <div class="app-chat-page">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <div class="app-info">
        <h1 class="app-name">{{ appData.appName || '未命名应用' }}</h1>
        <span class="app-type">{{ appData.codeGenType || 'Web应用' }}</span>
      </div>

      <div class="top-actions">
        <a-button
          v-if="deployUrl"
          type="link"
          :href="deployUrl"
          target="_blank"
          class="deploy-link"
        >
          <EyeOutlined />
          查看部署
        </a-button>

        <a-button type="primary" :loading="deployLoading" @click="handleDeploy" class="deploy-btn">
          <CloudUploadOutlined />
          部署
        </a-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧对话区域 -->
      <div class="chat-section">
        <div class="chat-header">
          <h3>AI 对话</h3>
          <a-button size="small" @click="fetchAppInfo" :loading="loading">
            <ReloadOutlined />
            刷新
          </a-button>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages">
          <div
            v-for="message in messages"
            :key="message.id"
            :class="['message', `message-${message.type}`]"
          >
            <div class="message-avatar">
              <div v-if="message.type === 'user'" class="user-avatar">
                {{ loginUserStore.loginUser.userName?.charAt(0) || 'U' }}
              </div>
              <div v-else class="ai-avatar">AI</div>
            </div>

            <div class="message-content">
              <div class="message-text">
                <pre v-if="message.type === 'ai' && message.content" class="code-content">{{
                  message.content
                }}</pre>
                <span v-else>{{ message.content }}</span>
              </div>
              <div class="message-time">
                {{ message.timestamp.toLocaleTimeString() }}
              </div>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="isGenerating" class="message message-ai">
            <div class="message-avatar">
              <div class="ai-avatar">AI</div>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入框 -->
        <div class="chat-input">
          <a-input
            v-model:value="currentMessage"
            placeholder="描述越详细，页面越具体，可以一步一步完善生成效果..."
            :disabled="isGenerating"
            @pressEnter="sendMessage(currentMessage)"
            class="message-input"
          />
          <a-button
            type="primary"
            :disabled="!currentMessage.trim() || isGenerating"
            @click="sendMessage(currentMessage)"
            class="send-btn"
          >
            <SendOutlined />
            发送
          </a-button>
        </div>
      </div>

      <!-- 右侧网页展示区域 -->
      <div class="preview-section">
        <div class="preview-header">
          <h3>生成后的网页展示</h3>
          <span class="preview-status">
            {{ isGenerating ? '生成中...' : generatedCode ? '生成完成' : '等待生成' }}
          </span>
        </div>

        <div class="preview-content">
          <div v-if="isGenerating" class="generating-placeholder">
            <div class="loading-spinner"></div>
            <p>AI 正在生成代码，请稍候...</p>
          </div>

          <div v-else-if="generatedCode" class="code-preview">
            <div class="preview-tabs">
              <span class="tab active">代码预览</span>
            </div>
            <div class="code-content">
              <pre>{{ generatedCode }}</pre>
            </div>
          </div>

          <div v-else class="empty-preview">
            <div class="empty-icon">💻</div>
            <p>输入提示词开始生成应用</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-chat-page {
  height: calc(100vh - 88px);
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

/* 顶部栏样式 */
.top-bar {
  background: white;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.app-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.app-name {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #1890ff;
}

.app-type {
  background: #f0f0f0;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.875rem;
  color: #666;
}

.top-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.deploy-link {
  color: #52c41a;
}

.deploy-btn {
  border-radius: 6px;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  gap: 24px;
  padding: 24px;
  overflow: hidden;
}

/* 左侧对话区域 */
.chat-section {
  flex: 1;
  background: white;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h3 {
  margin: 0;
  color: #333;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #1890ff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #52c41a;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.message-content {
  flex: 1;
  max-width: 80%;
}

.message-user .message-content {
  text-align: right;
}

.message-text {
  background: #f0f0f0;
  padding: 12px 16px;
  border-radius: 12px;
  word-wrap: break-word;
}

.message-user .message-text {
  background: #1890ff;
  color: white;
}

.code-content {
  white-space: pre-wrap;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875rem;
  line-height: 1.5;
  margin: 0;
}

.message-time {
  font-size: 0.75rem;
  color: #999;
  margin-top: 4px;
}

.message-user .message-time {
  text-align: right;
}

/* 输入框样式 */
.chat-input {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
}

.message-input {
  flex: 1;
}

.send-btn {
  border-radius: 6px;
}

/* 右侧预览区域 */
.preview-section {
  flex: 1;
  background: white;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.preview-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-header h3 {
  margin: 0;
  color: #333;
}

.preview-status {
  font-size: 0.875rem;
  color: #666;
}

.preview-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 生成中状态 */
.generating-placeholder {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 代码预览 */
.code-preview {
  height: 100%;
}

.preview-tabs {
  margin-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.tab {
  display: inline-block;
  padding: 8px 16px;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  color: #666;
}

.tab.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
}

.code-content {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 16px;
  height: calc(100% - 60px);
  overflow-y: auto;
}

.code-content pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875rem;
  line-height: 1.5;
  color: #333;
}

/* 空状态 */
.empty-preview {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #999;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}
.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%,
  80%,
  100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }

  .top-bar {
    padding: 12px 16px;
  }

  .app-name {
    font-size: 1.25rem;
  }

  .top-actions {
    flex-direction: column;
    gap: 8px;
  }

  .chat-input {
    flex-direction: column;
  }
}
</style>
