# 在线考试报名系统

> 本科毕业设计项目 - 基于 Spring Boot + React 的在线考试报名管理系统

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.x-blue.svg)](https://reactjs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

---

## 📖 项目简介

本系统是一个面向成人函授类考试的在线报名管理平台，实现了考生在线报名、管理员审核、在线缴费、准考证打印等完整业务流程。采用前后端分离架构，适合作为本科毕业设计项目。

### 核心功能

**考生端:**
- 用户注册与登录
- 个人信息管理
- 考试浏览与在线报名
- 在线缴费 (支付宝沙箱)
- 准考证打印
- 消息通知

**管理端:**
- 考试管理 (创建、编辑、发布)
- 考点管理
- 报名审核 (单个/批量)
- 缴费记录管理
- 数据统计与报表
- 用户管理
- 公告管理

---

## 🛠️ 技术栈

### 后端
- **开发语言:** Java 11
- **核心框架:** Spring Boot 2.7.x
- **安全框架:** Spring Security + JWT
- **ORM 框架:** MyBatis-Plus 3.5.x
- **数据库:** MySQL 8.0
- **缓存:** Redis (可选)
- **构建工具:** Maven 3.6+

### 前端
- **框架:** React 18
- **UI 组件库:** Ant Design 5.x
- **状态管理:** React Hooks
- **HTTP 请求:** Axios
- **构建工具:** Vite
- **图表库:** ECharts

### 第三方服务
- **支付:** 支付宝沙箱环境
- **短信:** 阿里云短信服务 (可选)
- **文件存储:** 本地存储 / 阿里云 OSS (可选)

---

## 📁 项目结构

```
exam-registration-system/
├── docs/                       # 文档目录
│   ├── 需求文档.md             # 需求分析文档
│   ├── 系统设计文档.md         # 系统设计文档
│   ├── 数据库设计文档.md       # 数据库详细设计
│   └── README.md              # 文档汇总
├── sql/                       # SQL脚本
│   └── init.sql               # 数据库初始化脚本
├── backend/                   # 后端项目 (待创建)
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── frontend/                  # 前端项目 (待创建)
│   ├── src/
│   ├── package.json
│   └── README.md
├── .gitignore                 # Git忽略配置
└── README.md                  # 项目说明 (本文件)
```

---

## 🚀 快速开始

### 环境要求

- **JDK:** 11+
- **Node.js:** 16+
- **MySQL:** 8.0+
- **Maven:** 3.6+
- **Git:** 2.0+

### 1. 克隆项目

```bash
git clone https://github.com/scguoi/exam-registration-system.git
cd exam-registration-system
```

### 2. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source sql/init.sql

# 或者直接执行
mysql -u root -p < sql/init.sql
```

### 3. 启动后端 (待开发)

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务默认运行在: `http://localhost:8080`

### 4. 启动前端 (待开发)

```bash
cd frontend
npm install
npm run dev
```

前端服务默认运行在: `http://localhost:5173`

### 5. 访问系统

- **考生端:** http://localhost:5173
- **管理端:** http://localhost:5173/admin
- **接口文档:** http://localhost:8080/doc.html (Knife4j)

---

## 👤 默认账号

执行 `sql/init.sql` 后会自动创建以下测试账号:

**管理员账号:**
- 用户名: `admin`
- 密码: `admin123`

**测试考生账号:**
- 用户名: `13800138000`
- 密码: `123456`

---

## 📊 数据库设计

### 6张核心表

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `sys_user` | 用户表 | username, password, role |
| `exam` | 考试表 | exam_name, exam_date, fee, status |
| `exam_site` | 考点表 | site_name, address, capacity |
| `registration` | 报名表 | exam_id, user_id, audit_status, payment_status |
| `payment_order` | 缴费订单表 | order_no, amount, status |
| `notice` | 公告表 | title, content, is_top |

### 表关系图

```
sys_user (1) ───── (N) registration (N) ───── (1) exam
                        │                           │
                        │ (N)                  (1) │
                        │                           │
                   exam_site                   payment_order
```

详细设计见: [数据库设计文档](docs/数据库设计文档.md)

---

## 🏗️ 系统架构

### 总体架构

```
┌─────────────┐         ┌─────────────┐
│  考生端      │         │  管理端      │
│  (React)    │         │  (React)    │
└──────┬──────┘         └──────┬──────┘
       │                       │
       └───────────┬───────────┘
                   │ HTTP/HTTPS
            ┌──────▼──────┐
            │   Nginx     │ (反向代理)
            └──────┬──────┘
                   │
            ┌──────▼──────┐
            │ Spring Boot │ (应用服务)
            └──────┬──────┘
                   │
            ┌──────▼──────┐
            │    MySQL    │ (数据存储)
            └─────────────┘
```

详细架构见: [系统设计文档](docs/系统设计文档.md)

---

## 📝 核心业务流程

### 考生报名流程

```
注册/登录 → 完善信息 → 浏览考试 → 在线报名 → 等待审核
   → 审核通过 → 在线缴费 → 下载准考证 → 参加考试
```

### 管理员审核流程

```
登录后台 → 查看待审核列表 → 查看报名详情 → 审核判断
   → 通过(生成准考证号) / 驳回(填写原因) → 通知考生
```

---

## 🔐 安全设计

- **认证方式:** JWT Token (有效期7天)
- **密码加密:** BCrypt (不可逆)
- **敏感数据加密:** AES (身份证号、手机号)
- **传输加密:** HTTPS (生产环境)
- **防护机制:**
  - SQL注入防护 (MyBatis预编译)
  - XSS攻击防护 (前端转义)
  - CSRF防护 (Token验证)
  - 登录保护 (失败5次锁定30分钟)

---

## 📚 文档

- [📋 需求文档](docs/需求文档.md) - 详细功能需求和非功能需求
- [🏗️ 系统设计文档](docs/系统设计文档.md) - 架构设计、流程图、接口设计
- [💾 数据库设计文档](docs/数据库设计文档.md) - 表结构、索引、E-R图
- [📖 文档汇总](docs/README.md) - 所有文档导航

---

## 🗓️ 开发计划

| 阶段 | 周期 | 状态 |
|------|------|------|
| 需求分析 | 第1-2周 | ✅ 已完成 |
| 系统设计 | 第2-3周 | ✅ 已完成 |
| 数据库设计 | 第3周 | ✅ 已完成 |
| 后端开发 | 第4-6周 | ⏳ 待开始 |
| 前端开发 | 第7-9周 | ⏳ 待开始 |
| 测试优化 | 第10-11周 | ⏳ 待开始 |
| 论文撰写 | 第12周 | ⏳ 待开始 |

---

## 🎯 功能特点

### 适合毕业设计的优点

1. ✅ **功能完整** - 覆盖完整业务流程
2. ✅ **技术主流** - Spring Boot + React 是当前主流技术栈
3. ✅ **架构清晰** - 前后端分离,层次分明
4. ✅ **有一定深度** - 包含认证授权、支付、文件上传等
5. ✅ **文档齐全** - 需求、设计、数据库文档完整
6. ✅ **代码规范** - 遵循阿里巴巴Java开发手册
7. ✅ **易于演示** - 界面友好,操作流程清晰

### 相比企业级系统的简化

- ❌ 不使用微服务架构 (采用单体应用)
- ❌ 不做分库分表 (单库足够)
- ❌ 不集成人脸识别、OCR等AI功能
- ❌ 不做高并发优化 (支持500人在线即可)
- ❌ Redis缓存可选 (不是必须)
- ❌ 支付使用沙箱环境 (不需真实接入)

---

## 🤝 贡献指南

本项目为毕业设计项目,暂不接受外部贡献。

---

## 📄 开源协议

本项目采用 MIT 协议开源,详见 [LICENSE](LICENSE) 文件。

---

## 👨‍💻 作者

- **姓名:** [你的姓名]
- **学号:** [你的学号]
- **学校:** [你的学校]
- **专业:** [你的专业]
- **指导老师:** [老师姓名]

---

## 🙏 致谢

感谢以下开源项目:

- [Spring Boot](https://spring.io/projects/spring-boot)
- [React](https://reactjs.org/)
- [Ant Design](https://ant.design/)
- [MyBatis-Plus](https://baomidou.com/)
- [ECharts](https://echarts.apache.org/)

---

## 📧 联系方式

如有问题,请联系: [你的邮箱]

---

**最后更新:** 2025-10-16
