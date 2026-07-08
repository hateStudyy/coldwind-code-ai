# 后端部署说明

本文档用于把 Spring Boot 后端从手动 `java -jar` 迁移到可自动化部署的服务形态。

## 目录约定

服务器当前站点目录：

```bash
/opt/1panel/www/sites/coldwind-code-ai
```

建议保留以下结构：

```bash
/opt/1panel/www/sites/coldwind-code-ai/
├── coldwind-code-ai-0.0.1-SNAPSHOT.jar
├── coldwind-code-ai-0.0.1-SNAPSHOT.jar.bak
├── index/dist/                 # 前端静态文件
├── tmp/code_output/            # AI 生成代码
├── tmp/code_deploy/            # 用户部署作品
└── log/                        # Nginx 站点日志
```

## 环境变量文件

不要把生产密钥提交到 Git。建议在服务器创建：

```bash
sudo mkdir -p /etc/coldwind-code-ai
sudo chmod 700 /etc/coldwind-code-ai
sudo nano /etc/coldwind-code-ai/coldwind-code-ai.env
```

示例：

```env
SPRING_PROFILES_ACTIVE=prod

DB_URL=jdbc:mysql://127.0.0.1:3306/coldwind_code_ai
DB_USERNAME=coldwind_code_ai_user
DB_PASSWORD=change_me

REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=change_me

DEEPSEEK_API_KEY=change_me
DASHSCOPE_API_KEY=change_me
COS_SECRET_ID=change_me
COS_SECRET_KEY=change_me
PEXELS_API_KEY=change_me

CODE_DEPLOY_HOST=http://134.175.119.8/dist
```

后续应把 `application-prod.yml` 改成 `${DB_URL}`、`${DEEPSEEK_API_KEY}` 这类占位，真实值只留在服务器。

## systemd 服务模板

创建服务文件：

```bash
sudo nano /etc/systemd/system/coldwind-code-ai.service
```

内容：

```ini
[Unit]
Description=Coldwind Code AI backend
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/1panel/www/sites/coldwind-code-ai
EnvironmentFile=/etc/coldwind-code-ai/coldwind-code-ai.env
ExecStart=/root/.sdkman/candidates/java/21.0.8-amzn/bin/java -jar /opt/1panel/www/sites/coldwind-code-ai/coldwind-code-ai-0.0.1-SNAPSHOT.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}
Restart=always
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

启用服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable coldwind-code-ai
sudo systemctl restart coldwind-code-ai
sudo systemctl status coldwind-code-ai
```

查看日志：

```bash
sudo journalctl -u coldwind-code-ai -f
```

## 手动发布流程

本地构建：

```bash
./mvnw -DskipTests package
```

上传：

```bash
scp target/coldwind-code-ai-0.0.1-SNAPSHOT.jar ubuntu@134.175.119.8:/tmp/coldwind-code-ai.jar
```

服务器替换并重启：

```bash
sudo cp /opt/1panel/www/sites/coldwind-code-ai/coldwind-code-ai-0.0.1-SNAPSHOT.jar /opt/1panel/www/sites/coldwind-code-ai/coldwind-code-ai-0.0.1-SNAPSHOT.jar.bak
sudo cp /tmp/coldwind-code-ai.jar /opt/1panel/www/sites/coldwind-code-ai/coldwind-code-ai-0.0.1-SNAPSHOT.jar
sudo systemctl restart coldwind-code-ai
curl -f http://127.0.0.1:8999/api/health/
```

回滚：

```bash
sudo cp /opt/1panel/www/sites/coldwind-code-ai/coldwind-code-ai-0.0.1-SNAPSHOT.jar.bak /opt/1panel/www/sites/coldwind-code-ai/coldwind-code-ai-0.0.1-SNAPSHOT.jar
sudo systemctl restart coldwind-code-ai
curl -f http://127.0.0.1:8999/api/health/
```

## GitHub Actions 接入建议

后端 CI/CD 可以后续新增独立 workflow：

1. `actions/checkout`
2. `actions/setup-java` 使用 Java 21
3. `./mvnw -DskipTests package`
4. 上传 jar 到服务器 `/tmp`
5. 服务器备份旧 jar
6. 替换 jar
7. `sudo systemctl restart coldwind-code-ai`
8. `curl -f http://127.0.0.1:8999/api/health/`
9. 失败时复制 `.bak` 回滚并再次重启

GitHub Secrets 只应保存部署用 SSH 信息，不保存数据库、Redis、AI、COS 等业务密钥。
