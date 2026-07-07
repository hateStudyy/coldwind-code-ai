<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

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
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
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
            <h1 class="hero-title">龙宝宝代码外卖小店</h1>
            <p class="hero-description">一句话轻松创建网站应用</p>
          </div>
        </div>

        <!-- 右侧：输入框和快捷操作 -->
        <div class="hero-right">
          <div class="input-section">
            <a-textarea
              v-model:value="userPrompt"
              placeholder="描叙您想要的网站，为您加急配送～"
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
              我的作品
            </a-button>
            <a-button type="default" @click="scrollToFeatured" class="nav-btn">
              <template #icon>
                <span>⭐</span>
              </template>
              精选案例
            </a-button>
          </div>

          <!-- 示例按钮 -->
          <div class="quick-actions">
            <a-button
              type="default"
              @click="
                setPrompt(
                  '创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能、评论系统和个人简介页面。采用简洁的设计风格，支持响应式布局，文章支持Markdown格式，首页展示最新文章和热门推荐。',
                )
              "
              >个人博客网站</a-button
            >
            <a-button
              type="default"
              @click="
                setPrompt(
                  '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。',
                )
              "
              >企业官网</a-button
            >
            <a-button
              type="default"
              @click="
                setPrompt(
                  '构建一个功能完整的在线商城，包含商品展示、购物车、用户注册登录、订单管理、支付结算等功能。设计现代化的商品卡片布局，支持商品搜索筛选、用户评价、优惠券系统和会员积分功能。',
                )
              "
              >在线商城</a-button
            >
            <a-button
              type="default"
              @click="
                setPrompt(
                  '开发一个作品展示网站，用于展示个人或团队的设计作品、项目案例、技能展示等。包含作品分类、详情展示、技能标签、联系方式等功能，采用现代化的卡片式布局和动画效果。',
                )
              "
              >作品展示网站</a-button
            >
          </div>
        </div>
      </div>

      <!-- 我的作品 -->
      <div class="section" id="my-apps">
        <h2 class="section-title">我的作品</h2>
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
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <!-- 精选案例 -->
      <div class="section" id="featured-apps">
        <h2 class="section-title">精选案例</h2>
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
            :show-total="(total: number) => `共 ${total} 个案例`"
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
}
</style>
