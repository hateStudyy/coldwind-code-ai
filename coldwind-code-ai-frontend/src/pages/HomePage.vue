<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage, deleteApp } from '@/api/appController.ts'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  StarOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 响应式数据
const promptInput = ref('')
const loading = ref(false)
const myAppsLoading = ref(false)
const goodAppsLoading = ref(false)

// 我的应用分页数据
const myAppsData = ref<API.AppVO[]>([])
const myAppsPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: false,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`,
})

// 精选应用分页数据
const goodAppsData = ref<API.AppVO[]>([])
const goodAppsPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: false,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`,
})

// 搜索关键词
const myAppsSearchKeyword = ref('')
const goodAppsSearchKeyword = ref('')

// 计算属性
const isLoggedIn = computed(() => loginUserStore.loginUser.id)
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')

// 创建应用
const handleCreateApp = async () => {
  if (!isLoggedIn.value) {
    message.warning('请先登录')
    return
  }

  if (!promptInput.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  loading.value = true
  try {
    const res = await addApp({
      initPrompt: promptInput.value.trim(),
    })

    if (res.data.code === 0) {
      message.success('应用创建成功')
      // 跳转到对话页面
      router.push(`/app/chat/${res.data.data}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    message.error('创建失败，请稍后重试')
    console.error('创建应用失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取我的应用列表
const fetchMyApps = async (page = 1, searchKeyword = '') => {
  if (!isLoggedIn.value) return

  myAppsLoading.value = true
  try {
    const params: API.AppQueryRequest = {
      pageNum: page,
      pageSize: myAppsPagination.value.pageSize,
      appName: searchKeyword || undefined,
    }

    const res = await listMyAppVoByPage(params)
    if (res.data.code === 0) {
      myAppsData.value = res.data.data?.records || []
      myAppsPagination.value.total = res.data.data?.totalRow || 0
      myAppsPagination.value.current = page
    }
  } catch (error) {
    message.error('获取应用列表失败')
    console.error('获取我的应用失败:', error)
  } finally {
    myAppsLoading.value = false
  }
}

// 获取精选应用列表
const fetchGoodApps = async (page = 1, searchKeyword = '') => {
  goodAppsLoading.value = true
  try {
    const params: API.AppQueryRequest = {
      pageNum: page,
      pageSize: goodAppsPagination.value.pageSize,
      appName: searchKeyword || undefined,
    }

    const res = await listGoodAppVoByPage(params)
    if (res.data.code === 0) {
      goodAppsData.value = res.data.data?.records || []
      goodAppsPagination.value.total = res.data.data?.totalRow || 0
      goodAppsPagination.value.current = page
    }
  } catch (error) {
    message.error('获取精选应用失败')
    console.error('获取精选应用失败:', error)
  } finally {
    goodAppsLoading.value = false
  }
}

// 处理我的应用分页变化
const handleMyAppsPageChange = (page: number) => {
  fetchMyApps(page, myAppsSearchKeyword.value)
}

// 处理精选应用分页变化
const handleGoodAppsPageChange = (page: number) => {
  fetchGoodApps(page, goodAppsSearchKeyword.value)
}

// 搜索我的应用
const handleMyAppsSearch = () => {
  fetchMyApps(1, myAppsSearchKeyword.value)
}

// 搜索精选应用
const handleGoodAppsSearch = () => {
  fetchGoodApps(1, goodAppsSearchKeyword.value)
}

// 查看应用详情
const handleViewApp = (app: API.AppVO) => {
  router.push(`/app/chat/${app.id}`)
}

// 编辑应用
const handleEditApp = (app: API.AppVO) => {
  router.push(`/app/edit/${app.id}`)
}

// 删除应用
const handleDeleteApp = async (app: API.AppVO) => {
  // 确认删除
  if (!confirm(`确定要删除应用"${app.appName || '未命名应用'}"吗？此操作不可恢复。`)) {
    return
  }

  try {
    const res = await deleteApp({ id: app.id! })
    if (res.data.code === 0) {
      message.success('删除成功')
      // 重新获取我的应用列表
      fetchMyApps(myAppsPagination.value.current, myAppsSearchKeyword.value)
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    message.error('删除失败，请稍后重试')
    console.error('删除应用失败:', error)
  }
}

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const weeks = Math.floor(diff / (1000 * 60 * 60 * 24 * 7))

  if (minutes < 60) return `创建于${minutes}分钟前`
  if (hours < 24) return `创建于${hours}小时前`
  if (days < 7) return `创建于${days}天前`
  if (weeks < 4) return `创建于${weeks}周前`

  return date.toLocaleDateString()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchMyApps()
  fetchGoodApps()
})
</script>

<template>
  <div class="home-page">
    <!-- 网站标题区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <div class="title-container">
          <h1 class="main-title">
            <span class="title-text">一句话</span>
            <span class="title-icon">🐱</span>
            <span class="title-text">呈所想</span>
          </h1>
          <p class="subtitle">与 AI 对话轻松创建应用和网站</p>
        </div>

        <!-- 提示词输入区域 -->
        <div class="prompt-section">
          <div class="prompt-input-container">
            <a-input
              v-model:value="promptInput"
              placeholder="使用 NoCode 创建一个高效的小工具，帮我计算..."
              size="large"
              class="prompt-input"
              @pressEnter="handleCreateApp"
            />
            <div class="prompt-actions">
              <a-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleCreateApp"
                class="create-btn"
              >
                <PlusOutlined />
                创建应用
              </a-button>
            </div>
          </div>

          <!-- 快捷模板 -->
          <div class="quick-templates">
            <a-button
              v-for="template in ['波普风电商页面', '企业网站', '电商运营后台', '暗黑话题社区']"
              :key="template"
              size="small"
              class="template-btn"
              @click="promptInput = template"
            >
              {{ template }}
            </a-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的应用区域 -->
    <div class="apps-section" v-if="isLoggedIn">
      <div class="section-header">
        <h2 class="section-title">我的作品</h2>
        <div class="section-search">
          <a-input-search
            v-model:value="myAppsSearchKeyword"
            placeholder="搜索应用名称"
            @search="handleMyAppsSearch"
            style="width: 200px"
          />
        </div>
      </div>

      <a-list
        :loading="myAppsLoading"
        :data-source="myAppsData"
        :pagination="myAppsPagination"
        @pagination="handleMyAppsPageChange"
        class="apps-list"
      >
        <template #renderItem="{ item }">
          <a-list-item class="app-item">
            <a-card hoverable class="app-card">
              <template #cover>
                <div class="app-cover">
                  <img
                    v-if="item.cover"
                    :src="item.cover"
                    :alt="item.appName"
                    class="cover-image"
                  />
                  <div v-else class="cover-placeholder">
                    <span class="placeholder-text">{{ item.appName?.charAt(0) || 'A' }}</span>
                  </div>
                </div>
              </template>

              <template #actions>
                <a-tooltip title="查看详情">
                  <EyeOutlined @click="handleViewApp(item)" />
                </a-tooltip>
                <a-tooltip title="编辑应用">
                  <EditOutlined @click="handleEditApp(item)" />
                </a-tooltip>
                <a-tooltip title="删除应用">
                  <DeleteOutlined @click="handleDeleteApp(item)" />
                </a-tooltip>
              </template>

              <a-card-meta
                :title="item.appName || '未命名应用'"
                :description="formatTime(item.createTime || '')"
              />
            </a-card>
          </a-list-item>
        </template>
      </a-list>
    </div>

    <!-- 精选应用区域 -->
    <div class="apps-section">
      <div class="section-header">
        <h2 class="section-title">
          <StarOutlined class="star-icon" />
          精选案例
        </h2>
        <div class="section-search">
          <a-input-search
            v-model:value="goodAppsSearchKeyword"
            placeholder="搜索应用名称"
            @search="handleGoodAppsSearch"
            style="width: 200px"
          />
        </div>
      </div>

      <a-list
        :loading="goodAppsLoading"
        :data-source="goodAppsData"
        :pagination="goodAppsPagination"
        @pagination="handleGoodAppsPageChange"
        class="apps-list"
      >
        <template #renderItem="{ item }">
          <a-list-item class="app-item">
            <a-card hoverable class="app-card">
              <template #cover>
                <div class="app-cover">
                  <img
                    v-if="item.cover"
                    :src="item.cover"
                    :alt="item.appName"
                    class="cover-image"
                  />
                  <div v-else class="cover-placeholder">
                    <span class="placeholder-text">{{ item.appName?.charAt(0) || 'A' }}</span>
                  </div>
                </div>
              </template>

              <template #actions>
                <a-tooltip title="查看详情">
                  <EyeOutlined @click="handleViewApp(item)" />
                </a-tooltip>
              </template>

              <a-card-meta
                :title="item.appName || '未命名应用'"
                :description="`${item.user?.userName || '未知用户'} · ${formatTime(item.createTime || '')}`"
              />
            </a-card>
          </a-list-item>
        </template>
      </a-list>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 英雄区域样式 */
.hero-section {
  text-align: center;
  padding: 60px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  margin-bottom: 48px;
  color: white;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
}

.title-container {
  margin-bottom: 40px;
}

.main-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.title-text {
  background: linear-gradient(45deg, #fff, #f0f0f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-icon {
  font-size: 2.5rem;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

.subtitle {
  font-size: 1.25rem;
  opacity: 0.9;
  margin: 0;
}

/* 提示词输入区域 */
.prompt-section {
  max-width: 600px;
  margin: 0 auto;
}

.prompt-input-container {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.prompt-input {
  flex: 1;
  border-radius: 8px;
}

.create-btn {
  border-radius: 8px;
  height: 40px;
  padding: 0 24px;
}

.quick-templates {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.template-btn {
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  backdrop-filter: blur(10px);
}

.template-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
}

/* 应用区域样式 */
.apps-section {
  margin-bottom: 48px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.section-title {
  font-size: 1.75rem;
  font-weight: 600;
  color: #1890ff;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.star-icon {
  color: #faad14;
}

/* 应用列表样式 */
.apps-list {
  background: transparent;
}

.app-item {
  padding: 0;
  margin-bottom: 16px;
}

.app-card {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.app-cover {
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  font-size: 3rem;
  font-weight: bold;
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-page {
    padding: 0 16px;
  }

  .hero-section {
    padding: 40px 0;
    margin-bottom: 32px;
  }

  .main-title {
    font-size: 2rem;
    flex-direction: column;
    gap: 8px;
  }

  .title-icon {
    font-size: 2rem;
  }

  .subtitle {
    font-size: 1rem;
  }

  .prompt-input-container {
    flex-direction: column;
  }

  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .quick-templates {
    justify-content: flex-start;
  }
}

@media (max-width: 480px) {
  .hero-section {
    padding: 30px 0;
  }

  .main-title {
    font-size: 1.5rem;
  }

  .title-icon {
    font-size: 1.5rem;
  }

  .prompt-section {
    padding: 0 16px;
  }
}
</style>
