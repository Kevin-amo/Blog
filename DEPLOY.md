# 项目部署说明

本文档对应当前仓库中的 Docker 部署方案，适用于 Linux 服务器使用 Docker Compose 部署博客系统。

## 1. 部署结构

部署后会启动 3 个服务：

- `mysql`：MySQL 8.0 数据库
- `backend`：Spring Boot 后端服务，容器内端口 `8081`
- `frontend`：Nginx 静态站点，负责提供前端页面，并将 `/api/*` 代理到后端

对外访问入口只有前端容器：

- 前台页面：`http://服务器IP/`
- 后端接口：`http://服务器IP/api/...`
- Swagger 文档：`http://服务器IP/api/swagger-ui.html`
- Knife4j 文档：`http://服务器IP/api/doc.html`

## 2. 服务器要求

建议服务器至少满足以下条件：

- Linux 服务器一台
- 已安装 Docker
- 已安装 Docker Compose Plugin（支持 `docker compose` 命令）
- 建议内存 2GB 及以上
- 服务器已放行 `80` 端口

如果你希望直接用域名访问，还需要提前把域名解析到服务器公网 IP。

## 3. 上传项目

将整个项目上传到服务器，例如：

```bash
mkdir -p /opt/blog
cd /opt/blog
```

然后把本项目代码上传到这个目录。

最终目录中至少应包含以下文件：

- `Dockerfile`
- `docker-compose.yml`
- `.env.example`
- `frontend/nginx.conf`
- `src/`
- `frontend/`
- `pom.xml`

## 4. 配置环境变量

先复制示例配置：

```bash
cp .env .env
```

然后编辑 `.env`：

```bash
vim .env
```

建议至少修改以下内容：

```env
APP_PORT=80

MYSQL_DATABASE=blog
MYSQL_ROOT_PASSWORD=请改成你的MySQL强密码

JWT_SECRET=请改成至少32位的随机字符串

DASHSCOPE_API_KEY=你的DashScope密钥
DASHSCOPE_MODEL=qwen-plus

ALIYUN_OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
ALIYUN_OSS_BUCKET_NAME=你的OSS桶名
ALIYUN_OSS_DOMAIN=你的OSS访问域名
ALIYUN_OSS_ACCESS_KEY_ID=你的OSS AccessKeyId
ALIYUN_OSS_ACCESS_KEY_SECRET=你的OSS AccessKeySecret

JAVA_OPTS=-Xms256m -Xmx512m
```

说明：

- `APP_PORT`：服务器对外暴露端口，默认是 `80`
- `MYSQL_ROOT_PASSWORD`：MySQL root 密码，必须修改
- `JWT_SECRET`：JWT 签名密钥，建议至少 32 位
- `DASHSCOPE_API_KEY`：项目中 AI 功能使用的 DashScope Key
- `ALIYUN_OSS_*`：头像上传等对象存储功能依赖的 OSS 配置
- `JAVA_OPTS`：Java 启动参数，可按服务器内存调整

## 5. 启动项目

在项目根目录执行：

```bash
docker compose up -d --build
```

首次启动会进行以下操作：

- 拉取基础镜像
- 构建前端镜像
- 构建后端镜像
- 创建 MySQL 数据卷
- 启动 3 个容器

查看运行状态：

```bash
docker compose ps
```

查看日志：

```bash
docker compose logs -f
```

只看后端日志：

```bash
docker compose logs -f backend
```

只看前端日志：

```bash
docker compose logs -f frontend
```

只看数据库日志：

```bash
docker compose logs -f mysql
```

## 6. 初始化数据库

当前仓库中我没有发现自动初始化数据库的 SQL 文件，因此 `docker-compose.yml` 只会自动创建数据库 `blog`，不会自动创建表结构。

你需要手动导入项目现有的 SQL 脚本或数据库备份。

如果你手头已经有 `blog.sql`，可以这样导入：

```bash
docker exec -i blog-mysql mysql -uroot -p你的密码 blog < blog.sql
```

导入完成后，再重启后端：

```bash
docker compose restart backend
```

## 7. 访问与验证

部署成功后，可以按下面顺序检查：

1. 打开 `http://服务器IP/`，确认前端页面能访问
2. 打开 `http://服务器IP/api/swagger-ui.html`，确认后端接口正常
3. 如果启用了 Knife4j，打开 `http://服务器IP/api/doc.html`
4. 尝试登录、注册、文章列表等核心流程
5. 测试头像上传，确认 OSS 配置正确

如果首页能打开，但接口报错，优先检查：

- 后端容器是否启动成功
- MySQL 是否已完成初始化
- `.env` 中的数据库密码是否一致
- `JWT_SECRET` 是否已配置
- `DASHSCOPE_API_KEY` 是否有效
- `ALIYUN_OSS_*` 是否填写正确

## 8. 常用运维命令

重新构建并启动：

```bash
docker compose up -d --build
```

停止服务：

```bash
docker compose down
```

停止并删除数据卷：

```bash
docker compose down -v
```

注意：执行 `docker compose down -v` 会删除 MySQL 数据，请谨慎使用。

重启某个服务：

```bash
docker compose restart backend
```

进入后端容器：

```bash
docker exec -it blog-backend sh
```

进入数据库容器：

```bash
docker exec -it blog-mysql mysql -uroot -p
```

查看镜像：

```bash
docker images
```

## 9. 更新部署

如果你后续修改了代码，可以这样更新：

```bash
cd /opt/blog
# 拉取或上传最新代码

docker compose up -d --build
```

如果只是修改了 `.env`，通常也建议执行一次：

```bash
docker compose up -d --build
```

## 10. 反向代理与 HTTPS

当前方案已经内置了一个 Nginx 容器用于提供前端和转发 `/api`。

如果你后续要接入：

- 域名
- HTTPS 证书
- 宝塔/Nginx/Caddy/Traefik

可以把当前 `frontend` 容器继续作为内部站点使用，再由服务器上的外层反向代理统一转发到它。

如果你希望我继续帮你做正式上线方案，我可以下一步直接补：

- 域名 + HTTPS 的 Nginx 配置
- 宝塔面板部署步骤
- 数据库初始化 SQL 接入 compose
- 自动更新脚本

## 11. 当前部署文件位置

本次新增和使用到的部署文件如下：

- `Dockerfile`
- `docker-compose.yml`
- `frontend/nginx.conf`
- `.env.example`
- `.dockerignore`
