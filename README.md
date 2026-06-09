# Coldwind Code AI Platform

一个基于人工智能的全栈代码生成平台，支持通过自然语言描述生成各种类型的应用程序代码。

## 项目简介

Coldwind Code AI 是一个创新的 AI 代码生成平台，旨在通过自然语言交互的方式帮助开发者快速创建应用程序。平台整合了多种 AI 模型，能够理解用户需求并自动生成相应的代码，大幅提高开发效率。

### 核心特性

- **智能代码生成**：基于自然语言描述生成完整的应用程序代码
- **多模型支持**：集成 DeepSeek、Qwen 等多种 AI 模型以满足不同场景需求
- **实时对话交互**：通过 SSE 流式传输实现与 AI 的实时对话
- **应用管理**：完整的应用生命周期管理（创建、编辑、部署、下载）
- **权限控制**：细粒度的用户权限管理系统
- **微服务架构**：基于 Spring Boot 和 Dubbo 的分布式微服务架构
- **前后端分离**：Vue 3 + TypeScript 前端与 Java 后端完全解耦

## 技术架构

### 后端技术栈

- **核心框架**：Spring Boot 3.x、Spring Cloud Alibaba
- **微服务**：Dubbo 3.x 服务治理
- **注册中心**：Nacos 服务注册与发现
- **数据库**：MySQL 8.x、MyBatis-Flex ORM
- **缓存**：Redis
- **AI 集成**：LangChain4j、DeepSeek API、阿里千问 API
- **API 文档**：Knife4j + OpenAPI 3.0
- **构建工具**：Maven

### 前端技术栈

- **框架**：Vue 3 Composition API
- **语言**：TypeScript
- **UI 库**：Ant Design Vue 4.x
- **状态管理**：Pinia
- **路由**：Vue Router 4
- **构建工具**：Vite
- **代码规范**：ESLint + Prettier

### 系统模块

```
coldwind-code-ai/
├── coldwind-code-ai-frontend/     # 前端项目
├── coldwind-code-ai-microservice/ # 微服务后端
│   ├── coldwind-code-ai-app/      # 应用主服务
│   ├── coldwind-code-ai-user/     # 用户服务
│   ├── coldwind-code-ai-ai/       # AI 服务
│   ├── coldwind-code-ai-client/   # 内部服务客户端
│   ├── coldwind-code-ai-common/   # 公共模块
│   ├── coldwind-code-ai-model/    # 数据模型
│   └── coldwind-code-ai-screenshot/# 截图服务
└── sql/                          # 数据库脚本
```

## 核心功能

### 1. AI 代码生成

平台支持多种类型的代码生成：
- 单页面应用（SPA）
- 多页面网站
- RESTful API 服务
- 移动端页面
- 数据可视化图表
- 管理后台系统

### 2. 应用管理

- 应用创建与编辑
- 版本历史管理
- 一键部署功能
- 代码下载功能
- 应用分享与展示

### 3. 用户系统

- 用户注册与登录
- 权限分级管理（普通用户/管理员）
- 个人作品管理
- 使用统计与限额控制

### 4. AI 模型路由

平台内置智能路由机制，根据不同任务类型自动选择最适合的 AI 模型：
- **DeepSeek Chat**：通用对话任务
- **DeepSeek Reasoner**：复杂推理任务
- **Qwen Turbo**：轻量级分类与路由任务

## 部署说明

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+
- Nacos 2.x
- Maven 3.8+

### 后端部署步骤

1. 导入数据库脚本：
   ```bash
   mysql -u root -p < sql/create_tables.sql
   ```

2. 修改各服务配置文件：
   ```yaml
   # application.yml
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

3. 启动 Nacos 注册中心

4. 依次启动各微服务模块

### 前端部署步骤

1. 安装依赖：
   ```bash
   cd coldwind-code-ai-frontend
   npm install
   ```

2. 构建生产版本：
   ```bash
   npm run build
   ```

3. 部署 dist 目录到 Web 服务器

### 生产环境启动

```shell
nohup java -jar coldwind-code-ai-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > app.log 2>&1 &
```

## API 接口

主要 RESTful API 接口：

### 应用管理接口
- `POST /api/app/add` - 创建应用
- `GET /api/app/get/vo?id={id}` - 获取应用详情
- `POST /api/app/update` - 更新应用信息
- `POST /api/app/delete` - 删除应用
- `POST /api/app/deploy` - 部署应用
- `GET /api/app/chat/gen/code?appId={appId}&message={message}` - AI 代码生成（SSE）

### 用户接口
- `POST /api/user/login` - 用户登录
- `POST /api/user/register` - 用户注册
- `POST /api/user/logout` - 用户登出
- `GET /api/user/get/login` - 获取当前登录用户

## 开发指南

### 后端开发

1. 克隆项目代码
2. 导入 IDE（推荐 IntelliJ IDEA）
3. 配置本地开发环境
4. 启动各服务模块进行调试

### 前端开发

1. 进入前端目录：
   ```bash
   cd coldwind-code-ai-frontend
   ```

2. 安装依赖：
   ```bash
   npm install
   ```

3. 启动开发服务器：
   ```bash
   npm run dev
   ```

## 贡献指南

欢迎提交 Issue 和 Pull Request 来帮助我们改进项目。

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 发起 Pull Request

## 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。

## 联系方式

如有任何问题或建议，请通过以下方式联系我们：
- 提交 [Issues](https://github.com/hateStudyy/issues)