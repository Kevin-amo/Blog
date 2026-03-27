# Blog Frontend (Vue3)

## 运行步骤

1. 启动后端服务（默认 `http://localhost:8081`）。
2. 安装依赖：

```bash
npm install
```

3. 启动前端：

```bash
npm run dev
```

4. 打开浏览器访问：

```text
http://localhost:5173
```

## 接口说明

- 登录：`POST /auth/login`
- 获取当前用户：`GET /user/me`
- 退出登录：`POST /auth/logout`

开发环境默认通过 Vite 代理将 `/api/*` 转发到 `http://localhost:8081`。
