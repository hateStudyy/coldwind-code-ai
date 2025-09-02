<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { listAppVoByPageByAdmin, deleteAppByAdmin } from '@/api/appController.ts'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  StarOutlined,
  PlusOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 响应式数据
const loading = ref(false)
const appsData = ref<API.AppVO[]>([])
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`,
})

// 搜索表单
const searchForm = ref<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 20,
  appName: '',
  codeGenType: '',
  userId: undefined,
})

// 计算属性
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')

// 获取应用列表
const fetchApps = async (page = 1, searchParams?: API.AppQueryRequest) => {
  if (!isAdmin.value) return

  loading.value = true
  try {
    const params: API.AppQueryRequest = {
      pageNum: page,
      pageSize: pagination.value.pageSize,
      ...searchParams,
    }

    const res = await listAppVoByPageByAdmin(params)
    if (res.data.code === 0) {
      appsData.value = res.data.data?.records || []
      pagination.value.total = res.data.data?.totalRow || 0
      pagination.value.current = page
    } else {
      message.error('获取应用列表失败：' + res.data.message)
    }
  } catch (error) {
    message.error('获取应用列表失败')
    console.error('获取应用列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理分页变化
const handlePageChange = (page: number, pageSize: number) => {
  pagination.value.pageSize = pageSize
  fetchApps(page, searchForm.value)
}

// 搜索应用
const handleSearch = () => {
  fetchApps(1, searchForm.value)
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    pageNum: 1,
    pageSize: 20,
    appName: '',
    codeGenType: '',
    userId: undefined,
  }
  fetchApps(1)
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
const handleDeleteApp = (app: API.AppVO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用"${app.appName || '未命名应用'}"吗？此操作不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteAppByAdmin({ id: app.id! })
        if (res.data.code === 0) {
          message.success('删除成功')
          // 重新获取列表
          fetchApps(pagination.value.current, searchForm.value)
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch (error) {
        message.error('删除失败，请稍后重试')
        console.error('删除失败:', error)
      }
    },
  })
}

// 设置精选应用
const handleSetFeatured = (app: API.AppVO) => {
  Modal.confirm({
    title: '设置精选应用',
    content: `确定要将应用"${app.appName || '未命名应用'}"设置为精选应用吗？`,
    okText: '确认设置',
    cancelText: '取消',
    onOk: async () => {
      try {
        // 这里需要调用更新接口设置优先级为99
        // 由于没有直接的设置精选接口，这里先提示用户
        message.info('精选功能需要编辑应用信息来设置优先级')
        // 跳转到编辑页面
        router.push(`/app/edit/${app.id}`)
      } catch (error) {
        message.error('设置失败，请稍后重试')
        console.error('设置失败:', error)
      }
    },
  })
}

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString()
}

// 获取状态标签
const getStatusTag = (app: API.AppVO) => {
  if (app.deployedTime) {
    return { color: 'success', text: '已部署' }
  }
  if (app.priority && app.priority >= 99) {
    return { color: 'warning', text: '精选' }
  }
  return { color: 'default', text: '正常' }
}

// 组件挂载时获取数据
onMounted(() => {
  if (isAdmin.value) {
    fetchApps()
  }
})
</script>

<template>
  <div class="app-manage-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">应用管理</h1>
        <span class="page-subtitle">管理系统中的所有应用</span>
      </div>

      <div class="header-actions">
        <a-button @click="handleReset" class="reset-btn">
          <ReloadOutlined />
          重置
        </a-button>
      </div>
    </div>

    <!-- 权限检查 -->
    <div v-if="!isAdmin" class="permission-error">
      <a-result status="403" title="403" sub-title="抱歉，您没有权限访问此页面。">
        <template #extra>
          <a-button type="primary" @click="router.push('/')"> 返回主页 </a-button>
        </template>
      </a-result>
    </div>

    <!-- 主要内容 -->
    <div v-else class="main-content">
      <!-- 搜索表单 -->
      <a-card title="搜索条件" class="search-card">
        <a-form :model="searchForm" layout="inline" class="search-form">
          <a-form-item label="应用名称">
            <a-input
              v-model:value="searchForm.appName"
              placeholder="请输入应用名称"
              allow-clear
              style="width: 200px"
            />
          </a-form-item>

          <a-form-item label="创建者">
            <a-input
              v-model:value="searchForm.userId"
              placeholder="请输入创建者ID"
              allow-clear
              style="width: 200px"
            />
          </a-form-item>

          <a-form-item label="应用类型">
            <a-input
              v-model:value="searchForm.codeGenType"
              placeholder="请输入应用类型"
              allow-clear
              style="width: 200px"
            />
          </a-form-item>

          <a-form-item label="用户角色">
            <a-select
              v-model:value="searchForm.userId"
              placeholder="请选择用户角色"
              allow-clear
              style="width: 150px"
            >
              <a-select-option :value="undefined">全部用户</a-select-option>
              <a-select-option :value="1">普通用户</a-select-option>
              <a-select-option :value="2">管理员</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item>
            <a-button type="primary" @click="handleSearch" class="search-btn">
              <SearchOutlined />
              搜索
            </a-button>
          </a-form-item>
        </a-form>
      </a-card>

      <!-- 应用列表 -->
      <a-card title="应用列表" class="list-card">
        <a-table
          :loading="loading"
          :data-source="appsData"
          :pagination="pagination"
          @change="handlePageChange"
          :row-key="(record: API.AppVO) => record.id!"
          class="apps-table"
        >
          <!-- 应用封面 -->
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'cover'">
              <div class="app-cover">
                <img
                  v-if="record.cover"
                  :src="record.cover"
                  :alt="record.appName"
                  class="cover-image"
                />
                <div v-else class="cover-placeholder">
                  <span>{{ record.appName?.charAt(0) || 'A' }}</span>
                </div>
              </div>
            </template>

            <!-- 应用名称 -->
            <template v-else-if="column.key === 'appName'">
              <div class="app-name-cell">
                <div class="app-name">{{ record.appName || '未命名应用' }}</div>
                <div class="app-type">{{ record.codeGenType || 'Web应用' }}</div>
              </div>
            </template>

            <!-- 创建者 -->
            <template v-else-if="column.key === 'creator'">
              <div class="creator-cell">
                <div class="creator-name">{{ record.user?.userName || '未知用户' }}</div>
                <div class="creator-role">
                  <a-tag :color="record.user?.userRole === 'admin' ? 'red' : 'blue'">
                    {{ record.user?.userRole === 'admin' ? '管理员' : '普通用户' }}
                  </a-tag>
                </div>
              </div>
            </template>

            <!-- 状态 -->
            <template v-else-if="column.key === 'status'">
              <a-tag :color="getStatusTag(record).color">
                {{ getStatusTag(record).text }}
              </a-tag>
            </template>

            <!-- 时间 -->
            <template v-else-if="column.key === 'createTime'">
              {{ formatTime(record.createTime || '') }}
            </template>

            <!-- 操作 -->
            <template v-else-if="column.key === 'action'">
              <div class="action-buttons">
                <a-tooltip title="查看详情">
                  <a-button type="text" size="small" @click="handleViewApp(record)">
                    <EyeOutlined />
                  </a-button>
                </a-tooltip>

                <a-tooltip title="编辑应用">
                  <a-button type="text" size="small" @click="handleEditApp(record)">
                    <EditOutlined />
                  </a-button>
                </a-tooltip>

                <a-tooltip title="设置精选">
                  <a-button type="text" size="small" @click="handleSetFeatured(record)">
                    <StarOutlined />
                  </a-button>
                </a-tooltip>

                <a-tooltip title="删除应用">
                  <a-button type="text" size="small" danger @click="handleDeleteApp(record)">
                    <DeleteOutlined />
                  </a-button>
                </a-tooltip>
              </div>
            </template>
          </template>

          <!-- 表格列定义 -->
          <a-table-column key="cover" title="封面" width="80" />
          <a-table-column key="appName" title="应用名称" />
          <a-table-column key="creator" title="创建者" width="150" />
          <a-table-column key="status" title="状态" width="100" />
          <a-table-column key="createTime" title="创建时间" width="180" />
          <a-table-column key="action" title="操作" width="200" fixed="right" />
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<style scoped>
.app-manage-page {
  max-width: 1400px;
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
  flex-direction: column;
  gap: 4px;
}

.page-title {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 600;
  color: #333;
}

.page-subtitle {
  color: #666;
  font-size: 0.875rem;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.reset-btn {
  border-radius: 6px;
}

/* 权限错误 */
.permission-error {
  text-align: center;
  padding: 60px 20px;
}

/* 主要内容 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 搜索卡片 */
.search-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-form {
  margin-bottom: 0;
}

.search-btn {
  border-radius: 6px;
}

/* 列表卡片 */
.list-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.apps-table {
  background: transparent;
}

/* 应用封面 */
.app-cover {
  text-align: center;
}

.cover-image {
  width: 50px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.cover-placeholder {
  width: 50px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  color: white;
  font-size: 1rem;
  font-weight: bold;
}

/* 应用名称单元格 */
.app-name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.app-name {
  font-weight: 600;
  color: #333;
}

.app-type {
  font-size: 0.875rem;
  color: #666;
}

/* 创建者单元格 */
.creator-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.creator-name {
  font-weight: 500;
  color: #333;
}

.creator-role {
  display: flex;
  justify-content: center;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 4px;
  justify-content: center;
}

.action-buttons .ant-btn {
  padding: 4px 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-manage-page {
    padding: 0 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .search-form {
    flex-direction: column;
    align-items: stretch;
  }

  .search-form .ant-form-item {
    margin-bottom: 16px;
    margin-right: 0;
  }

  .search-form .ant-input,
  .search-form .ant-select {
    width: 100% !important;
  }

  .action-buttons {
    flex-direction: column;
    gap: 2px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }

  .header-actions {
    width: 100%;
  }

  .reset-btn {
    width: 100%;
  }
}
</style>
