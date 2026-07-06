<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController.ts'
import { message } from 'ant-design-vue'
import {
  SaveOutlined,
  ArrowLeftOutlined,
  UserOutlined,
  CalendarOutlined,
  CodeOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 响应式数据
const appData = ref<API.AppVO>({} as API.AppVO)
const loading = ref(false)
const saving = ref(false)
const formData = ref({
  appName: '',
  cover: '',
  priority: 0,
})

// 计算属性
const appId = computed(() => route.params.id as string)
const isOwner = computed(() => appData.value.userId === loginUserStore.loginUser.id)
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')
const canEdit = computed(() => isOwner.value || isAdmin.value)
const isAdminEdit = computed(() => isAdmin.value && !isOwner.value)

// 获取应用信息
const fetchAppInfo = async () => {
  if (!appId.value) return

  loading.value = true
  try {
    const res = await getAppVoById({ id: appId.value })
    if (res.data.code === 0) {
      appData.value = res.data.data!
      // 初始化表单数据
      formData.value = {
        appName: appData.value.appName || '',
        cover: appData.value.cover || '',
        priority: appData.value.priority || 0,
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

// 保存应用信息
const handleSave = async () => {
  if (!appData.value.id) return

  saving.value = true
  try {
    let res

    if (isAdminEdit.value) {
      // 管理员编辑
      res = await updateAppByAdmin({
        id: appData.value.id,
        appName: formData.value.appName,
        cover: formData.value.cover,
        priority: formData.value.priority,
      })
    } else {
      // 普通用户编辑
      res = await updateApp({
        id: appData.value.id,
        appName: formData.value.appName,
      })
    }

    if (res.data.code === 0) {
      message.success('保存成功')
      // 更新本地数据
      appData.value = {
        ...appData.value,
        appName: formData.value.appName,
        cover: formData.value.cover,
        priority: formData.value.priority,
      }
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch (error) {
    message.error('保存失败，请稍后重试')
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

// 返回上一页
const handleBack = () => {
  router.back()
}

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString()
}

// 组件挂载时获取应用信息
onMounted(() => {
  fetchAppInfo()
})
</script>

<template>
  <div class="app-edit-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <a-button type="text" @click="handleBack" class="back-btn">
          <ArrowLeftOutlined />
          返回
        </a-button>
        <h1 class="page-title">编辑应用</h1>
      </div>

      <div class="header-actions">
        <a-button type="primary" :loading="saving" @click="handleSave" class="save-btn">
          <SaveOutlined />
          保存
        </a-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <a-spin size="large" />
      <p>加载中...</p>
    </div>

    <!-- 主要内容 -->
    <div v-else class="main-content">
      <!-- 权限提示 -->
      <div v-if="!canEdit" class="permission-warning">
        <a-alert message="权限不足" description="您没有权限编辑此应用" type="warning" show-icon />
      </div>

      <!-- 应用信息展示 -->
      <div class="app-info-section">
        <a-card title="应用基本信息" class="info-card">
          <div class="app-cover-container">
            <img
              v-if="appData.cover"
              :src="appData.cover"
              :alt="appData.appName"
              class="app-cover"
            />
            <div v-else class="cover-placeholder">
              <span>{{ appData.appName?.charAt(0) || 'A' }}</span>
            </div>
          </div>

          <div class="app-details">
            <div class="detail-item">
              <label>应用名称：</label>
              <span>{{ appData.appName || '未命名' }}</span>
            </div>

            <div class="detail-item">
              <label>应用类型：</label>
              <span>{{ appData.codeGenType || 'Web应用' }}</span>
            </div>

            <div class="detail-item">
              <label>创建者：</label>
              <span>{{ appData.user?.userName || '未知用户' }}</span>
            </div>

            <div class="detail-item">
              <label>创建时间：</label>
              <span>{{ formatTime(appData.createTime || '') }}</span>
            </div>

            <div class="detail-item">
              <label>更新时间：</label>
              <span>{{ formatTime(appData.updateTime || '') }}</span>
            </div>

            <div v-if="appData.deployedTime" class="detail-item">
              <label>部署时间：</label>
              <span>{{ formatTime(appData.deployedTime) }}</span>
            </div>
          </div>
        </a-card>
      </div>

      <!-- 编辑表单 -->
      <div v-if="canEdit" class="edit-form-section">
        <a-card title="编辑应用信息" class="edit-card">
          <a-form :model="formData" layout="vertical" class="edit-form">
            <!-- 应用名称 -->
            <a-form-item label="应用名称" required>
              <a-input
                v-model:value="formData.appName"
                placeholder="请输入应用名称"
                :maxlength="50"
                show-count
              />
            </a-form-item>

            <!-- 应用封面（仅管理员可见） -->
            <a-form-item v-if="isAdmin" label="应用封面">
              <a-input
                v-model:value="formData.cover"
                placeholder="请输入封面图片URL"
                :maxlength="500"
              />
              <div class="form-help">支持图片URL链接，建议尺寸 400x300</div>
            </a-form-item>

            <!-- 优先级（仅管理员可见） -->
            <a-form-item v-if="isAdmin" label="优先级">
              <a-input-number
                v-model:value="formData.priority"
                placeholder="请输入优先级"
                :min="0"
                :max="999"
                style="width: 100%"
              />
              <div class="form-help">数字越大优先级越高，精选应用优先级为99</div>
            </a-form-item>

            <!-- 初始提示词（只读） -->
            <a-form-item label="初始提示词">
              <a-textarea
                :value="appData.initPrompt"
                placeholder="无"
                :rows="4"
                readonly
                class="readonly-textarea"
              />
            </a-form-item>
          </a-form>
        </a-card>
      </div>

      <!-- 操作历史 -->
      <div class="history-section">
        <a-card title="操作历史" class="history-card">
          <a-timeline>
            <a-timeline-item>
              <template #dot>
                <UserOutlined style="color: #1890ff" />
              </template>
              <p>应用创建</p>
              <p class="timeline-time">{{ formatTime(appData.createTime || '') }}</p>
            </a-timeline-item>

            <a-timeline-item v-if="appData.updateTime && appData.updateTime !== appData.createTime">
              <template #dot>
                <CodeOutlined style="color: #52c41a" />
              </template>
              <p>应用更新</p>
              <p class="timeline-time">{{ formatTime(appData.updateTime) }}</p>
            </a-timeline-item>

            <a-timeline-item v-if="appData.deployedTime">
              <template #dot>
                <CalendarOutlined style="color: #faad14" />
              </template>
              <p>应用部署</p>
              <p class="timeline-time">{{ formatTime(appData.deployedTime) }}</p>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-edit-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  color: #666;
}

.page-title {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 600;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.save-btn {
  border-radius: 6px;
}

/* 加载状态 */
.loading-container {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.loading-container p {
  margin-top: 16px;
}

/* 主要内容 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 权限提示 */
.permission-warning {
  margin-bottom: 24px;
}

/* 应用信息展示 */
.info-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.app-cover-container {
  text-align: center;
  margin-bottom: 24px;
}

.app-cover {
  width: 200px;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.cover-placeholder {
  width: 200px;
  height: 150px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  color: white;
  font-size: 3rem;
  font-weight: bold;
}

.app-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-item label {
  font-weight: 600;
  color: #666;
  min-width: 80px;
}

.detail-item span {
  color: #333;
}

/* 编辑表单 */
.edit-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.edit-form {
  max-width: 600px;
}

.form-help {
  font-size: 0.875rem;
  color: #999;
  margin-top: 4px;
}

.readonly-textarea {
  background: #f8f9fa;
  color: #666;
}

/* 操作历史 */
.history-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.timeline-time {
  font-size: 0.875rem;
  color: #999;
  margin-top: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-edit-page {
    padding: 0 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .app-details {
    grid-template-columns: 1fr;
  }

  .app-cover,
  .cover-placeholder {
    width: 100%;
    max-width: 300px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }

  .header-actions {
    width: 100%;
  }

  .save-btn {
    width: 100%;
  }
}
</style>
