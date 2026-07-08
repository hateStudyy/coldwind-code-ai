import { computed, ref } from 'vue'

export type Locale = 'zh-CN' | 'zh-TW' | 'en-US' | 'ja-JP'

export const languageOptions: Array<{
  locale: Locale
  label: string
  shortLabel: string
}> = [
  { locale: 'zh-CN', label: '简体中文', shortLabel: '简' },
  { locale: 'zh-TW', label: '繁体中文', shortLabel: '繁' },
  { locale: 'en-US', label: 'English', shortLabel: 'EN' },
  { locale: 'ja-JP', label: '日本語', shortLabel: '日' },
]

const storageKey = 'coldwind-code-ai-locale'

const messages: Record<Locale, Record<string, string>> = {
  'zh-CN': {
    'nav.home': '主页',
    'nav.userManage': '用户管理',
    'nav.appManage': '应用管理',
    'nav.github': 'GitHub',
    'nav.login': '登录',
    'nav.logout': '退出登录',
    'nav.anonymous': '无名',
    'nav.language': '语言',
    'home.title': '龙宝宝代码外卖小店',
    'home.subtitle': '一句话轻松创建网站应用',
    'home.cicdTest': 'CI/CD 自动部署测试已实现',
    'home.promptPlaceholder': '描述您想要的网站，为您加急配送～',
    'home.myWorks': '我的作品',
    'home.featured': '精选案例',
    'home.examples.blog': '个人博客网站',
    'home.examples.company': '企业官网',
    'home.examples.shop': '在线商城',
    'home.examples.portfolio': '作品展示网站',
    'home.language.title': '多语言支持',
    'home.language.description': '选择界面语言，快速切换简体中文、繁体中文、英文和日文。',
    'home.pagination.apps': '共 {total} 个应用',
    'home.pagination.cases': '共 {total} 个案例',
    'home.message.enterPrompt': '请输入应用描述',
    'home.message.loginFirst': '请先登录',
    'home.message.createSuccess': '应用创建成功',
    'home.message.createFailed': '创建失败：',
    'home.message.createRetry': '创建失败，请重试',
    'prompt.blog':
      '创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能、评论系统和个人简介页面。采用简洁的设计风格，支持响应式布局，文章支持Markdown格式，首页展示最新文章和热门推荐。',
    'prompt.company':
      '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。',
    'prompt.shop':
      '构建一个功能完整的在线商城，包含商品展示、购物车、用户注册登录、订单管理、支付结算等功能。设计现代化的商品卡片布局，支持商品搜索筛选、用户评价、优惠券系统和会员积分功能。',
    'prompt.portfolio':
      '开发一个作品展示网站，用于展示个人或团队的设计作品、项目案例、技能展示等。包含作品分类、详情展示、技能标签、联系方式等功能，采用现代化的卡片式布局和动画效果。',
    'footer.slogan': 'Coldwind Code AI · 一句话，生成你想要的网站',
  },
  'zh-TW': {
    'nav.home': '首頁',
    'nav.userManage': '使用者管理',
    'nav.appManage': '應用管理',
    'nav.github': 'GitHub',
    'nav.login': '登入',
    'nav.logout': '登出',
    'nav.anonymous': '未命名',
    'nav.language': '語言',
    'home.title': '龍寶寶程式外賣小店',
    'home.subtitle': '一句話輕鬆建立網站應用',
    'home.cicdTest': 'CI/CD 自動部署測試已實現',
    'home.promptPlaceholder': '描述您想要的網站，立即為您加急配送～',
    'home.myWorks': '我的作品',
    'home.featured': '精選案例',
    'home.examples.blog': '個人部落格網站',
    'home.examples.company': '企業官網',
    'home.examples.shop': '線上商城',
    'home.examples.portfolio': '作品展示網站',
    'home.language.title': '多語言支援',
    'home.language.description': '選擇介面語言，快速切換簡體中文、繁體中文、英文和日文。',
    'home.pagination.apps': '共 {total} 個應用',
    'home.pagination.cases': '共 {total} 個案例',
    'home.message.enterPrompt': '請輸入應用描述',
    'home.message.loginFirst': '請先登入',
    'home.message.createSuccess': '應用建立成功',
    'home.message.createFailed': '建立失敗：',
    'home.message.createRetry': '建立失敗，請重試',
    'prompt.blog':
      '建立一個現代化的個人部落格網站，包含文章列表、詳情頁、分類標籤、搜尋功能、評論系統和個人簡介頁面。採用簡潔的設計風格，支援響應式佈局，文章支援 Markdown 格式，首頁展示最新文章和熱門推薦。',
    'prompt.company':
      '設計一個專業的企業官網，包含公司介紹、產品服務展示、新聞資訊、聯絡我們等頁面。採用商務風格設計，包含輪播圖、產品展示卡片、團隊介紹、客戶案例展示，支援多語言切換和線上客服功能。',
    'prompt.shop':
      '建立一個功能完整的線上商城，包含商品展示、購物車、使用者註冊登入、訂單管理、支付結算等功能。設計現代化的商品卡片佈局，支援商品搜尋篩選、使用者評價、優惠券系統和會員積分功能。',
    'prompt.portfolio':
      '開發一個作品展示網站，用於展示個人或團隊的設計作品、專案案例、技能展示等。包含作品分類、詳情展示、技能標籤、聯絡方式等功能，採用現代化的卡片式佈局和動畫效果。',
    'footer.slogan': 'Coldwind Code AI · 一句話，生成你想要的網站',
  },
  'en-US': {
    'nav.home': 'Home',
    'nav.userManage': 'Users',
    'nav.appManage': 'Apps',
    'nav.github': 'GitHub',
    'nav.login': 'Log in',
    'nav.logout': 'Log out',
    'nav.anonymous': 'Guest',
    'nav.language': 'Language',
    'home.title': 'Dragon Baby Code Shop',
    'home.subtitle': 'Create website apps from one sentence',
    'home.cicdTest': 'CI/CD auto deployment test is live',
    'home.promptPlaceholder': 'Describe the website you want. We will deliver it fast.',
    'home.myWorks': 'My Works',
    'home.featured': 'Featured',
    'home.examples.blog': 'Personal Blog',
    'home.examples.company': 'Company Site',
    'home.examples.shop': 'Online Store',
    'home.examples.portfolio': 'Portfolio Site',
    'home.language.title': 'Multilingual Support',
    'home.language.description':
      'Choose your interface language and switch between Simplified Chinese, Traditional Chinese, English, and Japanese.',
    'home.pagination.apps': '{total} apps',
    'home.pagination.cases': '{total} cases',
    'home.message.enterPrompt': 'Please enter an app description',
    'home.message.loginFirst': 'Please log in first',
    'home.message.createSuccess': 'App created successfully',
    'home.message.createFailed': 'Create failed: ',
    'home.message.createRetry': 'Create failed. Please try again.',
    'prompt.blog':
      'Create a modern personal blog with article lists, detail pages, categories, search, comments, and an about page. Use a clean responsive design, support Markdown posts, and show latest posts plus popular recommendations on the home page.',
    'prompt.company':
      'Design a professional company website with company profile, products and services, news, and contact pages. Use a business style with carousel banners, product cards, team introduction, customer cases, multilingual switching, and online support.',
    'prompt.shop':
      'Build a complete online store with product display, cart, user registration and login, order management, and checkout. Use modern product cards, search and filters, reviews, coupons, and membership points.',
    'prompt.portfolio':
      'Develop a portfolio website for an individual or team to showcase design works, project cases, and skills. Include categories, detail pages, skill tags, and contact information with a modern card layout and animations.',
    'footer.slogan': 'Coldwind Code AI · Generate the website you want from one sentence',
  },
  'ja-JP': {
    'nav.home': 'ホーム',
    'nav.userManage': 'ユーザー管理',
    'nav.appManage': 'アプリ管理',
    'nav.github': 'GitHub',
    'nav.login': 'ログイン',
    'nav.logout': 'ログアウト',
    'nav.anonymous': 'ゲスト',
    'nav.language': '言語',
    'home.title': 'ドラゴンベビー コードデリバリー',
    'home.subtitle': '一文でWebアプリをかんたん作成',
    'home.cicdTest': 'CI/CD 自動デプロイのテストを実装済み',
    'home.promptPlaceholder': '作りたいWebサイトを説明してください。すばやくお届けします。',
    'home.myWorks': '自分の作品',
    'home.featured': '注目事例',
    'home.examples.blog': '個人ブログ',
    'home.examples.company': '企業サイト',
    'home.examples.shop': 'オンラインストア',
    'home.examples.portfolio': 'ポートフォリオ',
    'home.language.title': '多言語対応',
    'home.language.description':
      '表示言語を選択し、簡体字中国語、繁体字中国語、英語、日本語をすばやく切り替えられます。',
    'home.pagination.apps': '全 {total} 件のアプリ',
    'home.pagination.cases': '全 {total} 件の事例',
    'home.message.enterPrompt': 'アプリの説明を入力してください',
    'home.message.loginFirst': '先にログインしてください',
    'home.message.createSuccess': 'アプリを作成しました',
    'home.message.createFailed': '作成に失敗しました：',
    'home.message.createRetry': '作成に失敗しました。もう一度お試しください。',
    'prompt.blog':
      '記事一覧、詳細ページ、カテゴリタグ、検索、コメント、プロフィールページを備えたモダンな個人ブログを作成してください。シンプルなレスポンシブデザインにし、Markdown形式の記事に対応し、トップページに最新記事と人気記事を表示します。',
    'prompt.company':
      '会社紹介、製品・サービス、ニュース、お問い合わせページを含むプロフェッショナルな企業サイトを設計してください。ビジネス向けのデザインで、カルーセル、製品カード、チーム紹介、導入事例、多言語切替、オンラインサポートを含めます。',
    'prompt.shop':
      '商品表示、カート、ユーザー登録・ログイン、注文管理、決済を備えたオンラインストアを構築してください。モダンな商品カード、検索と絞り込み、レビュー、クーポン、会員ポイントに対応します。',
    'prompt.portfolio':
      '個人またはチームのデザイン作品、プロジェクト事例、スキルを紹介するポートフォリオサイトを開発してください。作品分類、詳細表示、スキルタグ、連絡先を含め、モダンなカードレイアウトとアニメーションを採用します。',
    'footer.slogan': 'Coldwind Code AI · 一文でほしいWebサイトを生成',
  },
}

const isLocale = (value: string | null): value is Locale => {
  return languageOptions.some((option) => option.locale === value)
}

const getInitialLocale = (): Locale => {
  if (typeof window === 'undefined') {
    return 'zh-CN'
  }
  const savedLocale = window.localStorage.getItem(storageKey)
  if (isLocale(savedLocale)) {
    return savedLocale
  }
  const browserLocale = window.navigator.language
  if (browserLocale.startsWith('zh-TW') || browserLocale.startsWith('zh-HK')) {
    return 'zh-TW'
  }
  if (browserLocale.startsWith('ja')) {
    return 'ja-JP'
  }
  if (browserLocale.startsWith('en')) {
    return 'en-US'
  }
  return 'zh-CN'
}

const locale = ref<Locale>(getInitialLocale())

const syncDocumentLocale = () => {
  if (typeof document !== 'undefined') {
    document.documentElement.lang = locale.value
  }
}

syncDocumentLocale()

export const setLocale = (nextLocale: Locale) => {
  locale.value = nextLocale
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(storageKey, nextLocale)
  }
  syncDocumentLocale()
}

export const t = (key: string, params: Record<string, string | number> = {}) => {
  const template = messages[locale.value][key] ?? messages['zh-CN'][key] ?? key
  return Object.entries(params).reduce((text, [name, value]) => {
    return text.split(`{${name}}`).join(String(value))
  }, template)
}

export const useI18n = () => {
  const currentLanguage = computed(() => {
    return languageOptions.find((option) => option.locale === locale.value) ?? languageOptions[0]
  })

  return {
    locale,
    languageOptions,
    currentLanguage,
    setLocale,
    t,
  }
}
