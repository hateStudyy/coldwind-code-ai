<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'
import { useI18n } from '@/i18n'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const { locale, languageOptions, setLocale, t } = useI18n()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 优化提示词功能已移除

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning(t('home.message.enterPrompt'))
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning(t('home.message.loginFirst'))
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success(t('home.message.createSuccess'))
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error(t('home.message.createFailed') + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error(t('home.message.createRetry'))
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 滚动到我的作品
const scrollToMyApps = () => {
  const myAppsSection = document.getElementById('my-apps')
  if (myAppsSection) {
    myAppsSection.scrollIntoView({ behavior: 'smooth' })
  }
}

// 滚动到精选案例
const scrollToFeatured = () => {
  const featuredSection = document.getElementById('featured-apps')
  if (featuredSection) {
    featuredSection.scrollIntoView({ behavior: 'smooth' })
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <!-- 主要内容区域 -->
      <div class="main-hero">
        <!-- 左侧：Logo和标题 -->
        <div class="hero-left">
          <img class="hero-logo" src="@/assets/logo-large.svg" alt="龙宝宝代码外卖小店" />
          <div class="hero-text">
            <h1 class="hero-title">{{ t('home.title') }}</h1>
            <p class="hero-description">{{ t('home.subtitle') }}</p>
            <p class="cicd-test">{{ t('home.cicdTest') }}</p>
          </div>
        </div>

        <!-- 右侧：输入框和快捷操作 -->
        <div class="hero-right">
          <div class="input-section">
            <a-textarea
              v-model:value="userPrompt"
              :placeholder="t('home.promptPlaceholder')"
              :rows="4"
              :maxlength="100000"
              class="prompt-input"
            />
            <div class="input-actions">
              <a-button type="primary" size="large" @click="createApp" :loading="creating">
                <template #icon>
                  <span>↑</span>
                </template>
              </a-button>
            </div>
          </div>

          <!-- 快捷导航按钮 -->
          <div class="quick-nav">
            <a-button type="default" @click="scrollToMyApps" class="nav-btn">
              <template #icon>
                <span>📱</span>
              </template>
              {{ t('home.myWorks') }}
            </a-button>
            <a-button type="default" @click="scrollToFeatured" class="nav-btn">
              <template #icon>
                <span>⭐</span>
              </template>
              {{ t('home.featured') }}
            </a-button>
          </div>

          <!-- 示例按钮 -->
          <div class="quick-actions">
            <a-button
              type="default"
              @click="setPrompt(t('prompt.blog'))"
              >{{ t('home.examples.blog') }}</a-button
            >
            <a-button
              type="default"
              @click="setPrompt(t('prompt.company'))"
              >{{ t('home.examples.company') }}</a-button
            >
            <a-button
              type="default"
              @click="setPrompt(t('prompt.shop'))"
              >{{ t('home.examples.shop') }}</a-button
            >
            <a-button
              type="default"
              @click="setPrompt(t('prompt.portfolio'))"
              >{{ t('home.examples.portfolio') }}</a-button
            >
          </div>
        </div>
      </div>

      <!-- 多语言支持 -->
      <div class="language-panel">
        <div class="language-copy">
          <span class="language-kicker">{{ t('nav.language') }}</span>
          <h2 class="language-title">{{ t('home.language.title') }}</h2>
          <p class="language-description">{{ t('home.language.description') }}</p>
        </div>
        <div class="language-options">
          <a-button
            v-for="option in languageOptions"
            :key="option.locale"
            :type="locale === option.locale ? 'primary' : 'default'"
            class="language-choice"
            @click="setLocale(option.locale)"
          >
            {{ option.label }}
          </a-button>
        </div>
      </div>

      <!-- 我的作品 -->
      <div class="section" id="my-apps">
        <h2 class="section-title">{{ t('home.myWorks') }}</h2>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => t('home.pagination.apps', { total })"
            @change="loadMyApps"
          />
        </div>
      </div>

      <!-- 精选案例 -->
      <div class="section" id="featured-apps">
        <h2 class="section-title">{{ t('home.featured') }}</h2>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => t('home.pagination.cases', { total })"
            @change="loadFeaturedApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background: linear-gradient(180deg, #fff7e6 0%, #ffe7ba 100%);
  position: relative;
  overflow: hidden;
}

/* 去除网格与光效，保持简约背景 */

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

/* 移除居中光束效果 */

/* 主要内容区域 */
.main-hero {
  display: flex;
  align-items: flex-start;
  gap: 60px;
  padding: 40px 0 60px;
  margin-bottom: 40px;
}

.hero-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.hero-logo {
  width: 200px;
  height: 200px;
  margin-bottom: 20px;
  animation: logoFloat 3s ease-in-out infinite;
}

.hero-text {
  text-align: left;
}

.hero-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(ellipse 800px 400px at center, rgba(250, 140, 22, 0.10) 0%, transparent 70%);
}

/* 取消英雄区动画，保持稳重质感 */

@keyframes rotate {
  0% {
    transform: translate(-50%, -50%) rotate(0deg);
  }
  100% {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

.hero-title {
  font-size: 56px;
  font-weight: 700;
  margin: 0 0 20px;
  line-height: 1.2;
  background: linear-gradient(135deg, #fa8c16 0%, #ff9c2a 60%, #ffd591 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -1px;
  position: relative;
  z-index: 2;
}
/* 去除标题闪烁动画，提升高级感 */

.hero-description {
  font-size: 20px;
  margin: 0;
  opacity: 0.8;
  color: #5c6b75;
  position: relative;
  z-index: 2;
}

.cicd-test {
  margin: 12px 0 0;
  color: #d46b08;
  font-size: 15px;
  font-weight: 600;
}

/* 输入区域 */
.input-section {
  position: relative;
  margin: 0;
}

.prompt-input {
  border-radius: 16px;
  border: none;
  font-size: 16px;
  padding: 20px 60px 20px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.prompt-input:focus {
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.3);
  transform: translateY(-2px);
}

.input-actions {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 快捷导航按钮 */
.quick-nav {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.nav-btn {
  border-radius: 25px;
  padding: 12px 24px;
  height: auto;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid rgba(250, 140, 22, 0.3);
  color: #fa8c16;
  backdrop-filter: blur(15px);
  transition: all 0.3s;
  font-weight: 600;
}

.nav-btn:hover {
  background: rgba(250, 140, 22, 0.1);
  border-color: rgba(250, 140, 22, 0.6);
  color: #d46b08;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(250, 140, 22, 0.2);
}

/* 快捷按钮 */
.quick-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 60px;
  flex-wrap: wrap;
}

.quick-actions .ant-btn {
  border-radius: 25px;
  padding: 8px 20px;
  height: auto;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.2);
  color: #475569;
  backdrop-filter: blur(15px);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.quick-actions .ant-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.1), transparent);
  transition: left 0.5s;
}

.quick-actions .ant-btn:hover::before {
  left: 100%;
}

.quick-actions .ant-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(59, 130, 246, 0.4);
  color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.2);
}

.language-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  padding: 28px 0;
  margin: 0 0 52px;
  border-top: 1px solid rgba(250, 140, 22, 0.18);
  border-bottom: 1px solid rgba(250, 140, 22, 0.18);
}

.language-copy {
  max-width: 560px;
}

.language-kicker {
  display: block;
  margin-bottom: 8px;
  color: #d46b08;
  font-size: 13px;
  font-weight: 700;
}

.language-title {
  margin: 0 0 8px;
  color: #1e293b;
  font-size: 24px;
  font-weight: 700;
}

.language-description {
  margin: 0;
  color: #5c6b75;
  font-size: 15px;
  line-height: 1.7;
}

.language-options {
  display: grid;
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  gap: 12px;
  min-width: 260px;
}

.language-choice {
  height: 40px;
  border-radius: 8px;
  font-weight: 600;
}

/* 区域标题 */
.section {
  margin-bottom: 50px;
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #1e293b;
}

/* 我的作品网格 */
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

/* 精选案例网格 */
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-hero {
    flex-direction: column;
    gap: 40px;
    text-align: center;
  }

  .hero-left {
    align-items: center;
  }

  .hero-text {
    text-align: center;
  }
}

@media (max-width: 768px) {
  .hero-logo {
    width: 150px;
    height: 150px;
  }

  .hero-title {
    font-size: 24px;
  }

  .hero-description {
    font-size: 16px;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
  }

  .quick-actions {
    justify-content: center;
  }

  .quick-nav {
    flex-direction: column;
    gap: 12px;
  }

  .nav-btn {
    width: 100%;
  }

  .language-panel {
    align-items: stretch;
    flex-direction: column;
    gap: 20px;
  }

  .language-options {
    grid-template-columns: 1fr;
    min-width: 0;
  }
}
</style>
