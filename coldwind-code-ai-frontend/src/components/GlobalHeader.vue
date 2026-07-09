<template>
  <a-layout-header class="header">
    <div class="header-inner">
      <!-- 左侧：Logo和标题 -->
      <RouterLink to="/" class="brand">
        <img class="logo" src="@/assets/logo.svg" alt="WindCode Logo" />
        <span class="site-title">WindCode</span>
      </RouterLink>

      <!-- 中间：导航菜单 -->
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        :items="menuItems"
        @click="handleMenuClick"
        class="header-nav"
      />

      <!-- 右侧：用户操作区域 -->
      <div class="header-actions">
        <a-dropdown placement="bottomRight">
          <a-button class="lang-btn">
            <GlobalOutlined />
            <span class="lang-label">{{ currentLanguage.shortLabel }}</span>
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

        <template v-if="loginUserStore.loginUser.id">
          <a-dropdown placement="bottomRight">
            <a-space class="user-trigger">
              <a-avatar :src="loginUserStore.loginUser.userAvatar" :size="32" />
              <span class="user-name">{{ loginUserStore.loginUser.userName ?? t('nav.anonymous') }}</span>
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
        </template>
        <a-button v-else type="primary" size="small" href="/user/login" class="login-btn">
          {{ t('nav.login') }}
        </a-button>
      </div>
    </div>
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

const selectedKeys = ref<string[]>(['/'])
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

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

const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems.value))

const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  if (key.startsWith('/')) {
    router.push(key)
  }
}

const handleLanguageChange: MenuProps['onClick'] = (e) => {
  setLocale(e.key as Locale)
}

const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: '未登录' })
    message.success(t('auth.logoutSuccess'))
    await router.push('/user/login')
  } else {
    message.error(t('auth.logoutFailed') + res.data.message)
  }
}
</script>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(250, 140, 22, 0.12);
  padding: 0 32px;
  height: 64px;
  line-height: 64px;
  box-shadow: 0 1px 20px rgba(0, 0, 0, 0.04);
}

.header-inner {
  display: flex;
  align-items: center;
  max-width: 1280px;
  margin: 0 auto;
  height: 100%;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  flex-shrink: 0;
  margin-right: 40px;
}

.logo {
  height: 36px;
  width: 36px;
}

.site-title {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #fa8c16, #ff9c2a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-nav {
  flex: 1;
  border-bottom: none !important;
  background: transparent !important;
  line-height: 64px;
}

.header-nav :deep(.ant-menu-item-selected) {
  color: #fa8c16 !important;
}

.header-nav :deep(.ant-menu-item-selected::after) {
  border-bottom-color: #fa8c16 !important;
}

.header-nav :deep(.ant-menu-item:hover) {
  color: #fa8c16 !important;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.lang-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid rgba(250, 140, 22, 0.2);
  color: #d46b08;
  border-radius: 8px;
  height: 36px;
  padding: 0 10px;
}

.lang-btn:hover {
  border-color: rgba(250, 140, 22, 0.5) !important;
  color: #fa8c16 !important;
}

.lang-label {
  font-size: 12px;
  font-weight: 600;
}

.user-trigger {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-trigger:hover {
  background: rgba(250, 140, 22, 0.06);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.login-btn {
  border-radius: 8px;
  height: 36px;
  font-weight: 600;
  background: linear-gradient(135deg, #fa8c16, #ff9c2a);
  border: none;
}

.login-btn:hover {
  background: linear-gradient(135deg, #e07b10, #fa8c16) !important;
}

.language-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 120px;
  gap: 16px;
}

@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }

  .site-title {
    display: none;
  }

  .brand {
    margin-right: 16px;
  }

  .lang-label {
    display: none;
  }

  .user-name {
    display: none;
  }
}
</style>
