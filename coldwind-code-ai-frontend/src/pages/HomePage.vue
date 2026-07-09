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

const userPrompt = ref('')
const creating = ref(false)

const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({ current: 1, pageSize: 6, total: 0 })

const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({ current: 1, pageSize: 6, total: 0 })

const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

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
    const res = await addApp({ initPrompt: userPrompt.value.trim() })
    if (res.data.code === 0 && res.data.data) {
      message.success(t('home.message.createSuccess'))
      await router.push(`/app/chat/${String(res.data.data)}`)
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

const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) return
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

const viewChat = (appId: string | number | undefined) => {
  if (appId) router.push(`/app/chat/${appId}?view=1`)
}

const viewWork = (app: API.AppVO) => {
  if (app.deployKey) window.open(getDeployUrl(app.deployKey), '_blank')
}

const scrollToSection = (id: string) => {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
})
</script>

<template>
  <div id="homePage">
    <!-- ====== Hero 区域：居中布局 ====== -->
    <section class="hero">
      <div class="hero-bg-glow"></div>
      <div class="hero-content">
        <img class="hero-logo" src="@/assets/logo-large.svg" alt="WindCode" />
        <h1 class="hero-title">{{ t('home.title') }}</h1>
        <p class="hero-subtitle">{{ t('home.subtitle') }}</p>

        <!-- 输入框 —— 核心 CTA -->
        <div class="hero-input-wrap">
          <a-textarea
            v-model:value="userPrompt"
            :placeholder="t('home.promptPlaceholder')"
            :rows="3"
            :maxlength="100000"
            class="hero-input"
            @pressEnter="createApp"
          />
          <a-button
            type="primary"
            size="large"
            :loading="creating"
            @click="createApp"
            class="hero-submit"
          >
            <template #icon><span style="font-size:18px">&#8593;</span></template>
          </a-button>
        </div>

        <!-- 快捷模板 -->
        <div class="hero-templates">
          <a-button size="small" class="template-chip" @click="setPrompt(t('prompt.blog'))">
            {{ t('home.examples.blog') }}
          </a-button>
          <a-button size="small" class="template-chip" @click="setPrompt(t('prompt.company'))">
            {{ t('home.examples.company') }}
          </a-button>
          <a-button size="small" class="template-chip" @click="setPrompt(t('prompt.shop'))">
            {{ t('home.examples.shop') }}
          </a-button>
          <a-button size="small" class="template-chip" @click="setPrompt(t('prompt.portfolio'))">
            {{ t('home.examples.portfolio') }}
          </a-button>
        </div>

        <!-- 快捷跳转 -->
        <div class="hero-nav">
          <a-button type="default" class="hero-nav-btn" @click="scrollToSection('featured-apps')">
            ⭐ {{ t('home.featured') }}
          </a-button>
          <a-button type="default" class="hero-nav-btn" @click="scrollToSection('my-apps')">
            📱 {{ t('home.myWorks') }}
          </a-button>
        </div>
      </div>
    </section>

    <!-- ====== 精选案例 ====== -->
    <section class="section" id="featured-apps">
      <div class="section-container">
        <div class="section-header">
          <span class="section-badge">⭐</span>
          <h2 class="section-title">{{ t('home.featured') }}</h2>
          <p class="section-desc">发现优秀作品，获取灵感</p>
        </div>
        <div class="app-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div v-if="featuredApps.length === 0" class="empty-hint">暂无精选作品</div>
        <div class="pagination-wrap" v-if="featuredAppsPage.total > featuredAppsPage.pageSize">
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
    </section>

    <!-- ====== 我的作品 ====== -->
    <section class="section section-alt" id="my-apps">
      <div class="section-container">
        <div class="section-header">
          <span class="section-badge">📱</span>
          <h2 class="section-title">{{ t('home.myWorks') }}</h2>
          <p class="section-desc">你创建的所有应用</p>
        </div>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div v-if="!loginUserStore.loginUser.id" class="empty-hint">
          登录后即可创建和管理你的应用
        </div>
        <div v-else-if="myApps.length === 0" class="empty-hint">
          还没有作品，在上面输入框描述你想要的应用开始创建吧
        </div>
        <div class="pagination-wrap" v-if="myAppsPage.total > myAppsPage.pageSize">
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
    </section>

    <!-- ====== 语言切换 ====== -->
    <section class="section">
      <div class="section-container">
        <div class="lang-bar">
          <div class="lang-text">
            <span class="lang-kicker">{{ t('nav.language') }}</span>
            <span class="lang-desc">{{ t('home.language.description') }}</span>
          </div>
          <div class="lang-options">
            <a-button
              v-for="option in languageOptions"
              :key="option.locale"
              :type="locale === option.locale ? 'primary' : 'default'"
              class="lang-choice"
              @click="setLocale(option.locale)"
            >
              {{ option.label }}
            </a-button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ====== 全局 ====== */
#homePage {
  width: 100%;
  min-height: 100vh;
  background: #fafbfc;
}

/* ====== Hero ====== */
.hero {
  position: relative;
  padding: 80px 20px 72px;
  text-align: center;
  background: linear-gradient(180deg, #fff7e6 0%, #fffbf5 40%, #fafbfc 100%);
  overflow: hidden;
}

.hero-bg-glow {
  position: absolute;
  top: -30%;
  left: 50%;
  transform: translateX(-50%);
  width: 700px;
  height: 500px;
  background: radial-gradient(ellipse, rgba(250, 140, 22, 0.10) 0%, transparent 70%);
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-logo {
  width: 96px;
  height: 96px;
  margin-bottom: 24px;
  animation: float 4s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  margin: 0 0 12px;
  background: linear-gradient(135deg, #fa8c16, #d46b08);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -1px;
}

.hero-subtitle {
  font-size: 18px;
  color: #64748b;
  margin: 0 0 40px;
}

/* ---- 输入框 ---- */
.hero-input-wrap {
  position: relative;
  width: 100%;
  max-width: 640px;
  margin-bottom: 24px;
}

.hero-input {
  border-radius: 20px !important;
  padding: 18px 64px 18px 24px !important;
  font-size: 16px !important;
  border: 2px solid rgba(250, 140, 22, 0.15) !important;
  background: #fff !important;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.hero-input:focus {
  border-color: rgba(250, 140, 22, 0.5) !important;
  box-shadow: 0 8px 40px rgba(250, 140, 22, 0.15) !important;
}

.hero-submit {
  position: absolute;
  right: 10px;
  bottom: 10px;
  width: 44px;
  height: 44px;
  border-radius: 14px !important;
  background: linear-gradient(135deg, #fa8c16, #ff9c2a) !important;
  border: none !important;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(250, 140, 22, 0.35);
}

.hero-submit:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(250, 140, 22, 0.5) !important;
}

/* ---- 快捷模板 ---- */
.hero-templates {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
  margin-bottom: 28px;
}

.template-chip {
  border-radius: 20px !important;
  border: 1px solid rgba(250, 140, 22, 0.15) !important;
  color: #d46b08 !important;
  background: rgba(255, 255, 255, 0.8) !important;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
}

.template-chip:hover {
  background: rgba(250, 140, 22, 0.08) !important;
  border-color: rgba(250, 140, 22, 0.4) !important;
}

/* ---- 快捷导航 ---- */
.hero-nav {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.hero-nav-btn {
  border-radius: 24px !important;
  border: 2px solid rgba(250, 140, 22, 0.2) !important;
  color: #d46b08 !important;
  font-weight: 600;
  font-size: 14px;
  padding: 8px 24px !important;
  height: auto !important;
  background: rgba(255, 255, 255, 0.9) !important;
  transition: all 0.3s;
}

.hero-nav-btn:hover {
  background: rgba(250, 140, 22, 0.06) !important;
  border-color: rgba(250, 140, 22, 0.5) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(250, 140, 22, 0.15);
}

/* ====== 通用 Section ====== */
.section {
  padding: 60px 20px;
}

.section-alt {
  background: linear-gradient(180deg, #fafbfc 0%, #f1f5f9 50%, #fafbfc 100%);
}

.section-container {
  max-width: 1280px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-badge {
  font-size: 28px;
  margin-bottom: 8px;
  display: block;
}

.section-title {
  margin: 0 0 8px;
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
}

.section-desc {
  margin: 0;
  color: #64748b;
  font-size: 16px;
}

/* ---- 卡片网格 ---- */
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

/* ---- 空状态 ---- */
.empty-hint {
  text-align: center;
  padding: 48px 20px;
  color: #94a3b8;
  font-size: 15px;
}

/* ---- 分页 ---- */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

/* ====== 语言栏 ====== */
.lang-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 0;
  border-top: 1px solid rgba(250, 140, 22, 0.1);
  border-bottom: 1px solid rgba(250, 140, 22, 0.1);
}

.lang-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.lang-kicker {
  font-size: 12px;
  font-weight: 700;
  color: #d46b08;
  text-transform: uppercase;
}

.lang-desc {
  font-size: 14px;
  color: #64748b;
}

.lang-options {
  display: flex;
  gap: 8px;
}

.lang-choice {
  border-radius: 8px !important;
  font-weight: 500;
}

/* ====== 响应式 ====== */
@media (max-width: 768px) {
  .hero {
    padding: 48px 16px 48px;
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 15px;
    margin-bottom: 28px;
  }

  .hero-logo {
    width: 72px;
    height: 72px;
  }

  .app-grid {
    grid-template-columns: 1fr;
  }

  .section {
    padding: 40px 16px;
  }

  .section-title {
    font-size: 24px;
  }

  .lang-bar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
