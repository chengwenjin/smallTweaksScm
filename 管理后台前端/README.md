# RBAC管理后台 - 前端

基于 Vue 3 + Vite + TypeScript + Element Plus 的管理后台前端项目。

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Vite** - 下一代前端构建工具
- **TypeScript** - JavaScript的超集
- **Element Plus** - Vue 3组件库
- **Vue Router** - 官方路由管理器
- **Pinia** - Vue状态管理
- **Axios** - HTTP客户端

## 快速开始

### 安装依赖
```bash
npm install
```

### 开发模式
```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本
```bash
npm run build
```

## 项目结构

```
frontend/
├── src/
│   ├── api/          # API接口
│   ├── components/   # 公共组件
│   ├── layout/       # 布局组件
│   ├── router/       # 路由配置
│   ├── stores/       # 状态管理
│   ├── types/        # 类型定义
│   ├── utils/        # 工具函数
│   ├── views/        # 页面组件
│   ├── App.vue       # 根组件
│   └── main.ts       # 入口文件
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## 功能模块

- ✅ 登录页面（含验证码）
- ✅ 主布局（侧边栏 + 顶部导航）
- ✅ 首页Dashboard
- ⏳ 用户管理
- ⏳ 角色管理
- ⏳ 菜单管理

## 默认账号

- 用户名：admin
- 密码：admin123

## 代理配置

前端开发服务器已配置代理，所有 `/api` 请求会转发到 `http://localhost:8080`

在 `vite.config.ts` 中修改代理配置。
