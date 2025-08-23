# 全局基础布局使用说明

## 概述

本项目已实现了一个通用的全局基础布局，采用上中下三段式布局结构，支持响应式设计。

## 文件结构

```
src/
├── layouts/
│   └── BasicLayout.vue          # 主布局组件
├── components/
│   ├── GlobalHeader.vue         # 全局头部导航组件
│   └── GlobalFooter.vue         # 全局底部版权组件
└── App.vue                      # 应用入口，已引入BasicLayout
```

## 布局特性

### 1. 响应式设计
- 桌面端：完整显示所有元素
- 平板端：菜单项适当调整
- 移动端：隐藏导航菜单，保留核心功能

### 2. 头部导航 (GlobalHeader)
- **左侧**：Logo + 网站标题 "Coldwind Code AI"
- **中间**：水平导航菜单，支持多级菜单
- **右侧**：登录按钮（可扩展为用户头像和昵称）

### 3. 内容区域
- 根据路由自动切换页面内容
- 自适应高度，确保页面填满视口
- 适当的内边距，提供良好的阅读体验

### 4. 底部版权 (GlobalFooter)
- 固定在底部
- 包含版权信息和GitHub链接
- 链接指向：https://github.com/hateStudyy

## 菜单配置

在 `GlobalHeader.vue` 中可以轻松配置菜单项：

```typescript
const menuItems = [
  {
    key: 'home',
    label: '首页',
    onClick: () => router.push('/')
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
  }
]
```

## 样式定制

### 主题色彩
- 主色调：#1890ff (Ant Design 默认蓝)
- 背景色：#f0f2f5 (底部)
- 文字色：#333 (主要文字)

### 响应式断点
- 桌面端：> 768px
- 平板端：768px - 480px
- 移动端：< 480px

## 使用说明

1. 布局已自动应用到整个应用
2. 新增页面只需在路由中配置，会自动显示在内容区域
3. 如需修改头部菜单，编辑 `GlobalHeader.vue` 中的 `menuItems` 配置
4. 如需修改底部信息，编辑 `GlobalFooter.vue`

## 技术栈

- Vue 3 + TypeScript
- Ant Design Vue 4.x
- Vue Router 4.x
- 响应式CSS

## 注意事项

1. 头部导航固定在顶部，内容区域会自动调整上边距
2. 底部版权信息始终固定在底部
3. 所有组件都支持响应式，无需额外配置
4. 已移除默认的 main.css 样式文件，使用 Ant Design 的样式系统
