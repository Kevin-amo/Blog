# Blog

一个前后端分离的博客系统，后端基于 Spring Boot 3，前端基于 Vue 3 + Vite，包含文章管理、分类管理、评论、用户与管理员权限、头像上传，以及基于 DashScope 的 AI 文章摘要能力。

## 项目结构

```text
blog/
├─ src/main/java/         # Spring Boot 后端源码
├─ src/main/resources/    # 配置文件与 MyBatis Mapper XML
├─ frontend/              # Vue 3 前端项目
├─ sql/                   # 数据库初始化脚本
├─ docker-compose.yml     # Docker Compose 部署文件
├─ Dockerfile             # 前后端镜像构建文件
└─ DEPLOY.md              # 服务器部署说明
```

## 技术栈

### 后端

- Java 17
- Spring Boot 3.5.12
- MyBatis
- MySQL 8
- Redis
- JWT
- BCrypt
- PageHelper
- 阿里云 OSS
- Spring AI Alibaba / DashScope
- Knife4j + SpringDoc OpenAPI
- sensitive-word 敏感词过滤

### 前端

- Vue 3
- Vue Router
- Vite
- Axios

## 核心功能

- 用户注册、登录、登出
- JWT 鉴权与基于角色的权限控制
- 用户资料维护、密码修改、头像上传
- 文章新增、编辑、删除、分页查询
- 公开文章列表与文章详情页
- 分类管理与分类选项查询
- 评论发布、回复与删除
- 管理员用户管理、重置密码、状态控制
- 管理员文章审核
- AI 流式生成文章摘要
- 评论敏感词过滤
- Swagger / Knife4j 接口文档

## 角色说明

系统当前至少包含两类角色：

- 普通用户：撰写文章、编辑个人资料、发表评论
- 管理员：用户管理、文章审核、分类管理

从路由与接口实现看：

- 普通用户端主页：`/user`
- 管理员端主页：`/admin`
- 公共博客首页：`/`

## 后端接口概览

主要接口分组如下：

- `POST /user/login`：登录
- `POST /user/register`：注册
- `GET /user/me`：获取当前用户信息
- `PUT /user/profile`：更新资料
- `PUT /user/password`：修改密码
- `POST /user/avatar`：上传头像
- `GET /article/public/list`：公开文章列表
- `GET /article/public/{id}`：公开文章详情
- `GET /article/page`：文章分页
- `POST /article`：新增文章
- `PUT /article`：更新文章
- `DELETE /article/{id}`：删除文章
- `GET /category/options`：分类选项
- `POST /comment/add`：新增评论
- `GET /comment/list/{articleId}`：文章评论列表
- `GET /admin/users/page`：管理员分页查询用户
- `GET /admin/articles/review/page`：管理员分页查看待审核文章
- `PUT /admin/articles/{id}/audit`：审核文章
- `POST /ai/article-summary/stream`：AI 流式生成文章摘要（SSE）

更完整的接口定义请以在线接口文档为准。

## 数据库

仓库内已包含初始化脚本：

- `sql/blog.sql`

默认数据库名为：

- `blog`

脚本中包含以下核心表：

- `user`
- `article`
- `category`
- `comment`

## 本地开发环境要求

建议环境：

- JDK 17
- Maven 3.9+
- Node.js 18+
- MySQL 8.0
- Redis 7

## 后端配置说明

后端默认配置文件为：

- `src/main/resources/application.yaml`

当前已在配置中声明：

- 服务端口：`8081`
- MySQL 默认地址：`localhost:3306/blog`
- Redis 默认地址：`localhost:6379`
- 文件上传大小限制：单文件 5MB
- AI 摘要限流、缓存与等待策略

该项目还会尝试加载私有配置文件：

- `src/main/resources/application-private.yaml`

建议在本地创建该文件，用来填写敏感信息，不要提交到仓库。

可参考以下内容：

```yaml
spring:
  datasource:
    username: root
    password: 你的数据库密码
  ai:
    dashscope:
      api-key: 你的DashScopeKey
      base-url: https://dashscope.aliyuncs.com
      chat:
        options:
          model: qwen3.5-flash

jwt:
  secret: 至少32位的JWT密钥字符串

aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    bucket-name: 你的Bucket名称
    domain: 你的OSS访问域名
    access-key-id: 你的AccessKeyId
    access-key-secret: 你的AccessKeySecret
```

说明：

- `jwt.secret` 为必填，否则登录签发 token 会失败
- `spring.datasource.password` 为必填，当前公共配置中未写死数据库密码
- `spring.ai.dashscope.api-key` 用于 AI 摘要功能
- `aliyun.oss.*` 用于头像上传等对象存储功能

## 本地启动步骤

### 1. 初始化数据库

创建数据库：

```sql
CREATE DATABASE blog DEFAULT CHARACTER SET utf8mb4;
```

然后导入：

```bash
mysql -uroot -p blog < sql/blog.sql
```

### 2. 启动 Redis

确保本机 Redis 已启动，默认端口：`6379`。

### 3. 启动后端

在项目根目录执行：

```bash
mvn spring-boot:run
```

后端默认地址：

- `http://localhost:8081`

### 4. 启动前端

进入前端目录并安装依赖：

```bash
cd frontend
npm install
```

启动开发服务器：

```bash
npm run dev
```

前端默认地址：

- `http://localhost:5173`

开发环境下，Vite 已将 `/api` 代理到：

- `http://localhost:8081`

因此前端本地访问一般无需额外改接口地址。

## 接口文档

启动后端后可访问：

- Swagger UI：`http://localhost:8081/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8081/v3/api-docs`

如果通过 Docker + Nginx 部署，并由前端统一代理 `/api`，则通常对应为：

- `http://服务器IP/api/swagger-ui.html`
- `http://服务器IP/api/doc.html`

## AI 文章摘要

项目已实现基于 DashScope 的文章摘要功能。

接口：

- `POST /ai/article-summary/stream`

请求体示例：

```json
{
  "articleId": 1,
  "maxLength": 180
}
```

约束说明：

- 仅支持对已发布文章生成摘要
- `maxLength` 范围为 `100 ~ 300`
- 使用 SSE 流式返回摘要片段
- 基于 Redis 做了缓存、限流与并发锁控制

如果未配置 DashScope Key，该功能将不可用。

## Docker 部署

仓库已提供：

- `Dockerfile`
- `docker-compose.yml`

可通过 Docker Compose 启动 MySQL、Redis、后端与前端服务。

更完整的服务器部署说明见：

- `DEPLOY.md`

快速启动示例：

```bash
docker compose up -d --build
```

## 默认数据说明

`sql/blog.sql` 中已经包含示例数据，包括：

- 分类数据
- 测试文章
- 评论数据
- 用户数据

这适合本地联调，但不建议直接用于生产环境。

## 开发说明

### 后端

常用命令：

```bash
mvn clean package
mvn spring-boot:run
```

### 前端

常用命令：

```bash
npm install
npm run dev
npm run build
```