# 在线考试报名系统 - 前端

基于 React 18 + TypeScript + Ant Design + Vite 构建的现代化前端应用。

## 技术栈

- **框架**: React 18 + TypeScript
- **构建工具**: Vite
- **UI 组件库**: Ant Design 5.x
- **路由**: React Router v6
- **状态管理**: React Hooks + Context
- **HTTP 客户端**: Axios
- **图表库**: ECharts
- **日期处理**: Day.js

## 项目结构

```
src/
├── components/          # 通用组件
│   └── ProtectedRoute.tsx
├── contexts/           # React Context
│   └── AuthContext.tsx
├── hooks/              # 自定义 Hooks
│   └── useAuth.ts
├── layouts/            # 布局组件
│   ├── UserLayout.tsx
│   └── AdminLayout.tsx
├── pages/              # 页面组件
│   ├── Login.tsx
│   ├── Register.tsx
│   ├── UserDashboard.tsx
│   └── AdminDashboard.tsx
├── services/           # API 服务
│   ├── api.ts
│   ├── user.ts
│   ├── exam.ts
│   ├── registration.ts
│   ├── payment.ts
│   └── notice.ts
├── types/              # TypeScript 类型定义
│   └── index.ts
├── utils/              # 工具函数
│   ├── auth.ts
│   └── format.ts
├── assets/             # 静态资源
│   ├── images/
│   └── styles/
├── App.tsx             # 根组件
├── main.tsx            # 入口文件
└── index.css           # 全局样式
```

## 功能特性

### 考生端功能
- ✅ 用户注册登录
- ✅ 个人信息管理
- ✅ 考试浏览与报名
- ✅ 报名状态查询
- ✅ 在线缴费
- ✅ 准考证下载

### 管理端功能
- ✅ 考试管理
- ✅ 报名审核
- ✅ 缴费管理
- ✅ 数据统计
- ✅ 公告管理
- ✅ 用户管理

## 开发指南

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0

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

### 预览生产构建

```bash
npm run preview
```

### 代码检查

```bash
npm run lint
```

## 环境配置

创建 `.env` 文件并配置以下环境变量：

```env
# API基础URL
VITE_API_BASE_URL=http://localhost:8080/api/v1

# 应用标题
VITE_APP_TITLE=在线考试报名系统

# 应用版本
VITE_APP_VERSION=1.0.0

# 是否开启Mock数据
VITE_USE_MOCK=false
```

## 路由结构

### 公开路由
- `/login` - 登录页面
- `/register` - 注册页面

### 考生端路由
- `/user/dashboard` - 个人中心
- `/user/exams` - 考试列表
- `/user/registrations` - 我的报名
- `/user/orders` - 缴费记录
- `/user/profile` - 个人信息
- `/user/settings` - 账户设置

### 管理端路由
- `/admin/dashboard` - 数据概览
- `/admin/exams` - 考试管理
- `/admin/registrations` - 报名审核
- `/admin/payments` - 缴费管理
- `/admin/statistics` - 数据统计
- `/admin/notices` - 公告管理
- `/admin/users` - 用户管理

## 权限控制

系统采用基于角色的权限控制（RBAC）：

- **考生 (user)**: 只能访问考生端功能
- **管理员 (admin)**: 可以访问管理端功能

使用 `ProtectedRoute` 组件进行路由保护：

```tsx
<ProtectedRoute requireAuth requireAdmin>
  <AdminComponent />
</ProtectedRoute>
```

## API 接口

所有 API 接口都通过 `services` 目录下的模块进行管理：

- `user.ts` - 用户相关接口
- `exam.ts` - 考试相关接口
- `registration.ts` - 报名相关接口
- `payment.ts` - 缴费相关接口
- `notice.ts` - 公告相关接口

## 状态管理

使用 React Context + Hooks 进行状态管理：

- `AuthContext` - 用户认证状态
- `useAuth` - 认证相关操作

## 样式规范

- 使用 Ant Design 组件库
- 自定义主题配置
- 响应式设计
- 统一的颜色和字体规范

## 测试账号

### 管理员账号
- 用户名: `admin`
- 密码: `admin123`

### 考生账号
- 用户名: `13800138000`
- 密码: `123456`

## 部署说明

### 开发环境部署

1. 确保后端服务已启动（默认端口 8080）
2. 配置 `.env` 文件中的 API 地址
3. 运行 `npm run dev` 启动开发服务器

### 生产环境部署

1. 运行 `npm run build` 构建生产版本
2. 将 `dist` 目录部署到 Web 服务器
3. 配置 Nginx 反向代理到后端 API

## 开发规范

### 代码规范
- 使用 TypeScript 严格模式
- 遵循 ESLint 规则
- 使用 Prettier 格式化代码
- 组件使用函数式组件 + Hooks

### 提交规范
- 使用语义化提交信息
- 提交前运行代码检查
- 保持提交历史清晰

## 常见问题

### Q: 如何添加新的页面？
A: 在 `pages` 目录下创建新组件，然后在 `App.tsx` 中添加对应路由。

### Q: 如何调用后端 API？
A: 在 `services` 目录下创建对应的 API 服务文件，使用 `api.ts` 中的 axios 实例。

### Q: 如何添加新的权限控制？
A: 在 `ProtectedRoute` 组件中添加新的权限检查逻辑。

## 更新日志

### v1.0.0 (2024-01-15)
- ✅ 初始版本发布
- ✅ 完成基础架构搭建
- ✅ 实现用户认证功能
- ✅ 完成考生端和管理端基础页面