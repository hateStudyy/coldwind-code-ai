# Coldwind Code AI Platform

一个基于人工智能的全栈代码生成平台，支持通过自然语言描述自动生成应用程序代码。

## 项目简介

Coldwind Code AI 是一个创新的 AI 代码生成工具，用户通过自然语言描述需求，平台自动生成完整的应用程序代码。

### 核心特性

- **AI 代码生成** — 支持单文件 HTML、多文件项目、完整 Vue 3 + Vite 项目三种生成模式
- **多模型智能路由** — 集成 DeepSeek Chat、DeepSeek Reasoner、Qwen Turbo，根据任务复杂度自动选择最优模型
- **LangGraph4j 工作流** — 6 节点编排管道（图片收集 → 提示词增强 → 模型路由 → 代码生成 → 质量检查 → 项目构建），支持质检失败自动重试
- **实时流式输出** — 基于 SSE 技术，AI 生成过程实时推送到前端
- **并发工作流** — 将图片收集拆分为 4 个并行收集器（内容图片、插图、图表、Logo）
- **应用管理** — 完整的应用生命周期管理（创建、编辑、一键部署、代码下载）
- **分布式限流** — 基于 Redisson，支持用户级别、IP 级别、API 级别限流
- **AI 可观测性** — Micrometer 指标 + Prometheus + Grafana 监控面板
- **提示词安全** — 输入护栏系统，检测提示词注入和越狱攻击
- **截图服务** — 基于 Selenium 自动截取生成应用的预览图

## 技术架构

### 后端

| 类别 | 技术 |
|---|---|
| 核心框架 | Spring Boot 3.5.5, Java 21 (虚拟线程) |
| 数据库 | MySQL 8, MyBatis-Flex 1.11.1, HikariCP |
| 缓存 | Redis, Redisson |
| AI 引擎 | LangChain4j 1.1.0, LangGraph4j 1.6.0 |
| AI 模型 | DeepSeek API, 阿里千问 API (DashScope) |
| 监控 | Micrometer, Prometheus, Grafana |
| API 文档 | Knife4j + OpenAPI 3.0 |

### 前端

| 类别 | 技术 |
|---|---|
| 框架 | Vue 3 (Composition API, `<script setup>`) |
| 语言 | TypeScript |
| UI 库 | Ant Design Vue 4.x |
| 状态管理 | Pinia |
| 路由 | Vue Router 4 |
| 构建 | Vite |

### 项目结构

```
coldwind-code-ai/
├── src/                              # 后端代码 (Spring Boot 单体应用)
│   └── main/java/in/yumi/coldwindcodeai/
│       ├── ai/                       # AI 模型集成层
│       ├── controller/               # RESTful API 控制器
│       ├── service/                  # 业务逻辑层
│       ├── mapper/                   # MyBatis-Flex 数据访问
│       ├── generator/                # 代码生成器
│       ├── langgraph4j/              # LangGraph4j 工作流引擎
│       ├── ratelimiter/              # 分布式限流
│       ├── monitor/                  # AI 可观测性指标
│       └── model/                    # 实体/DTO/VO
├── coldwind-code-ai-frontend/        # Vue 3 前端项目
├── coldwind-code-ai-microservice/    # (开发中) 微服务版本重构
├── sql/                              # 数据库初始化脚本
├── grafana/                          # Grafana 监控面板配置
└── prometheus.yml                    # Prometheus 采集配置
```

## 功能模块

### AI 代码生成

平台支持三种代码生成模式：

- **HTML 单页** — 生成单个 HTML 文件，适合工具类页面、表单
- **多文件项目** — 生成多文件结构的前端项目
- **Vue 3 项目** — 生成完整的 Vue 3 + Vite 脚手架项目，含路由、组件、样式

生成流程经过 LangGraph4j 工作流编排：图片收集 → 提示词增强 → 路由 → 代码生成 → 质量检查 → 项目构建，确保输出质量。

### 应用管理

- 应用的创建、编辑、删除
- 一键部署到指定主机
- 生成代码打包下载
- 运行截图预览

### 用户系统

- 注册与登录（会话认证）
- 普通用户 / 管理员权限分级

### 智能模型路由

根据任务类型自动选择 AI 模型：

| 模型 | 适用场景 |
|---|---|
| DeepSeek Chat | 代码生成、通用对话 |
| DeepSeek Reasoner | 复杂逻辑推理 |
| Qwen Turbo | 轻量分类、路由决策 |

## 快速开始

### 环境要求

- JDK 21+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+
- Maven 3.8+

### 后端启动

1. 创建数据库并导入表结构：

```sql
mysql -u root -p < sql/create_tables.sql
```

2. 配置 `application-local.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/coldwind_code_ai
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

3. 启动应用：

```bash
mvn spring-boot:run -Dspring.profiles.active=local
```

### 前端启动

```bash
cd coldwind-code-ai-frontend
npm install
npm run dev
```

## API 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/user/login` | 用户登录 |
| POST | `/api/user/register` | 用户注册 |
| GET | `/api/app/get/vo?id={id}` | 获取应用详情 |
| POST | `/api/app/add` | 创建应用 |
| POST | `/api/app/update` | 更新应用 |
| POST | `/api/app/delete` | 删除应用 |
| POST | `/api/app/deploy` | 部署应用 |
| GET | `/api/app/chat/gen/code?appId={appId}&message={message}` | AI 代码生成（SSE 实时流） |

完整 API 文档启动后访问 `http://localhost:8080/doc.html`。
