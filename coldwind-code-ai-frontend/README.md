# Coldwind Code AI Frontend

一个基于 Vue 3 + TypeScript + Ant Design Vue 的 AI 代码生成平台前端项目。

## 功能特性

### 🏠 主页功能

- **AI 对话创建应用**：用户可以通过输入提示词来创建新的应用
- **我的作品展示**：登录用户可以查看自己创建的应用列表
- **精选案例展示**：展示平台精选的应用案例
- **应用管理**：支持编辑、删除个人应用

### 💬 应用对话页面

- **AI 对话界面**：与 AI 进行实时对话，生成应用代码
- **流式响应**：支持 Server-Sent Events (SSE) 实时显示 AI 回复
- **代码预览**：实时预览生成的代码内容
- **应用部署**：一键部署应用到服务器

### ✏️ 应用编辑页面

- **信息编辑**：用户可以编辑应用名称等基本信息
- **权限控制**：普通用户只能编辑自己的应用，管理员可以编辑所有应用
- **操作历史**：显示应用的创建、更新、部署等操作记录

### 🔧 应用管理页面（管理员专用）

- **全局应用管理**：管理员可以查看和管理所有应用
- **高级搜索**：支持按应用名称、创建者、应用类型、用户角色等条件搜索
- **批量操作**：支持编辑、删除、设置精选等操作
- **权限验证**：只有管理员可以访问此页面

### 👥 用户管理

- **用户登录/注册**：支持用户账户管理
- **权限控制**：区分普通用户和管理员权限
- **用户管理**：管理员可以管理所有用户账户

## 技术栈

- **前端框架**：Vue 3 + Composition API
- **类型系统**：TypeScript
- **UI 组件库**：Ant Design Vue 4.x
- **状态管理**：Pinia
- **路由管理**：Vue Router 4
- **HTTP 客户端**：Axios
- **构建工具**：Vite
- **代码规范**：ESLint + Prettier

## 项目结构

```
src/
├── api/                    # API 接口定义
│   ├── appController.ts    # 应用相关接口
│   ├── userController.ts   # 用户相关接口
│   └── typings.d.ts       # TypeScript 类型定义
├── components/             # 公共组件
│   ├── GlobalHeader.vue   # 全局头部导航
│   └── GlobalFooter.vue   # 全局底部
├── layouts/                # 布局组件
│   └── BasicLayout.vue    # 基础布局
├── pages/                  # 页面组件
│   ├── HomePage.vue       # 主页
│   ├── app/               # 应用相关页面
│   │   ├── AppChatPage.vue    # 应用对话页面
│   │   └── AppEditPage.vue    # 应用编辑页面
│   ├── admin/             # 管理员页面
│   │   ├── AppManagePage.vue  # 应用管理页面
│   │   └── UserManagePage.vue # 用户管理页面
│   └── user/              # 用户页面
│       ├── UserLoginPage.vue  # 用户登录页面
│       └── UserRegisterPage.vue # 用户注册页面
├── stores/                 # 状态管理
│   ├── loginUser.ts       # 登录用户状态
│   └── counter.ts         # 计数器状态（示例）
├── router/                 # 路由配置
│   └── index.ts           # 路由定义
├── App.vue                # 根组件
└── main.ts                # 应用入口
```

## 快速开始

### 环境要求

- Node.js >= 20.19.0 或 >= 22.12.0
- npm 或 yarn

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 代码检查

```bash
npm run lint
npm run type-check
```

## API 接口

项目使用 OpenAPI 规范自动生成 TypeScript 类型定义和 API 客户端代码。主要接口包括：

### 应用管理

- `POST /api/app/add` - 创建应用
- `GET /api/app/get/vo` - 获取应用详情
- `POST /api/app/update` - 更新应用信息
- `POST /api/app/delete` - 删除应用
- `POST /api/app/deploy` - 部署应用
- `GET /api/app/chat/gen/code` - AI 代码生成（SSE）

### 用户管理

- `POST /api/user/login` - 用户登录
- `POST /api/user/register` - 用户注册
- `POST /api/user/logout` - 用户登出
- `GET /api/user/get/login` - 获取当前登录用户

## 权限控制

### 普通用户权限

- 创建、编辑、删除自己的应用
- 查看精选应用列表
- 与 AI 对话生成代码
- 部署自己的应用

### 管理员权限

- 所有普通用户权限
- 管理所有应用（编辑、删除、设置精选）
- 管理所有用户账户
- 查看应用管理页面

## 开发指南

### 添加新页面

1. 在 `src/pages/` 目录下创建新的 Vue 组件
2. 在 `src/router/index.ts` 中添加路由配置
3. 在 `src/components/GlobalHeader.vue` 中添加菜单项（如需要）

### 添加新 API 接口

1. 在 `src/api/` 目录下创建新的控制器文件
2. 在 `src/api/index.ts` 中导出新接口
3. 在 `src/api/typings.d.ts` 中添加类型定义

### 状态管理

使用 Pinia 进行状态管理，在 `src/stores/` 目录下创建新的 store 文件。

## 部署说明

### 构建

```bash
npm run build
```

### 部署到静态服务器

将 `dist/` 目录下的文件部署到 Web 服务器即可。

### 环境变量

项目支持以下环境变量：

- `VITE_API_BASE_URL` - API 服务器地址
- `VITE_APP_TITLE` - 应用标题

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目地址：[GitHub](https://github.com/hateStudyy)
- 问题反馈：[Issues](https://github.com/hateStudyy/issues)
