<template>
  <a-layout-header class="header">
    <a-row :wrap="false">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="360px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.svg" alt="Coldwind Logo" />
            <h1 class="site-title">Coldwind Code AI</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
      <a-col>
        <div class="user-login-status">
          <a-dropdown placement="bottomRight">
            <a-button class="language-button">
              <GlobalOutlined />
              <span>{{ currentLanguage.label }}</span>
            </a-button>
            <template #overlay>
              <a-menu @click="handleLanguageChange">
                <a-menu-item v-for="option in languageOptions" :key="option.locale">
                  <span class="language-option">
                    <span>{{ option.label }}</span>
                    <CheckOutlined v-if="option.locale === locale" />
                  </span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? t('nav.anonymous') }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    {{ t('nav.logout') }}
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">{{ t('nav.login') }}</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { CheckOutlined, GlobalOutlined, HomeOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { type Locale, useI18n } from '@/i18n'

const loginUserStore = useLoginUserStore()
const router = useRouter()
const { locale, languageOptions, currentLanguage, setLocale, t } = useI18n()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, next) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = computed<MenuProps['items']>(() => [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: t('nav.home'),
    title: t('nav.home'),
  },
  {
    key: '/admin/userManage',
    label: t('nav.userManage'),
    title: t('nav.userManage'),
  },
  {
    key: '/admin/appManage',
    label: t('nav.appManage'),
    title: t('nav.appManage'),
  },
  {
    key: 'others',
    label: h('a', { href: 'https://github.com/hateStudyy', target: '_blank' }, t('nav.github')),
    title: t('nav.github'),
  },
])

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems.value))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

const handleLanguageChange: MenuProps['onClick'] = (e) => {
  setLocale(e.key as Locale)
}

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  height: 48px;
  width: 48px;
}

.site-title {
  margin: 0;
  font-size: 18px;
  color: #1890ff;
  white-space: nowrap; /* 防止标题换行 */
}

.ant-menu-horizontal {
  border-bottom: none !important;
}

.user-login-status {
  display: flex;
  align-items: center;
  gap: 12px;
}

.language-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-color: rgba(250, 140, 22, 0.28);
  color: #d46b08;
}

.language-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 110px;
  gap: 16px;
}

@media (max-width: 768px) {
  .site-title {
    font-size: 16px;
  }

  .language-button span:last-child {
    display: none;
  }
}
</style>
