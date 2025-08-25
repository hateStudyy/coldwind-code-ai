<template>
  <a-layout-header class="global-header">
    <div class="header-container">
      <!-- 左侧：Logo和网站标题 -->
      <div class="header-left">
        <img src="@/assets/logo.png" alt="Logo" class="logo" />
        <h1 class="site-title">Coldwind Code AI</h1>
      </div>

      <!-- 中间：导航菜单 -->
      <div class="header-center">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          class="nav-menu"
        />
      </div>

      <!-- 右侧：用户信息 -->
      <div class="user-login-status">
        <div v-if="loginUserStore.loginUser.id">
          <a-space>
            <a-avatar :src="loginUserStore.loginUser.userAvatar" />
            {{ loginUserStore.loginUser.userName ?? '无名' }}
          </a-space>
        </div>
        <div v-else>
          <a-button type="primary" href="/user/login">登录</a-button>
        </div>
      </div>

      <div v-if="!loginUserStore" class="header-right">
        <a-button type="primary" @click="handleLogin">
          <template #icon>
            <UserOutlined />
          </template>
          登录
        </a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { UserOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'

// JS 中引入 Store
import { useLoginUserStore } from '@/stores/loginUser.ts'
const loginUserStore = useLoginUserStore()

const router = useRouter()
const selectedKeys = ref<string[]>(['home'])

// 菜单配置
const menuItems = [
  {
    key: 'home',
    label: '首页',
    onClick: () => router.push('/')
  },
  {
    key: 'about',
    label: '关于',
    onClick: () => router.push('/about')
  },
  {
    key: 'features',
    label: '功能',
    children: [
      {
        key: 'code-analysis',
        label: '代码分析'
      },
      {
        key: 'ai-assistant',
        label: 'AI助手'
      }
    ]
  },
  {
    key: 'docs',
    label: '文档'
  }
]

// 处理登录
const handleLogin = () => {
  console.log('登录按钮被点击')
  // 这里可以添加登录逻辑
}
</script>

<style scoped>
.global-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 64px;
  line-height: 64px;
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.site-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  border: none;
  background: transparent;
}

.header-right {
  display: flex;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }

  .site-title {
    font-size: 16px;
  }

  .header-center {
    display: none; /* 在移动端隐藏菜单 */
  }
}

@media (max-width: 480px) {
  .site-title {
    display: none; /* 在很小屏幕上隐藏标题 */
  }
}
</style>
