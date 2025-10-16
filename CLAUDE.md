# 在线考试报名系统 - Claude Code 工作指南

这是一个**成人函授本科毕业设计项目**，采用前后端分离架构的在线考试报名系统。

**重要提示：** 这是一个简化的本科毕业设计项目，不是企业级系统。避免建议过度复杂的架构或功能。

---

## 📋 项目概览

### 技术栈
- **前端**: React 18 + Ant Design 5.x + Vite
- **后端**: Spring Boot 2.7.x + Spring Security + JWT + MyBatis-Plus 3.5.x
- **数据库**: MySQL 8.4
- **开发工具**: IntelliJ IDEA
- **容器化**: Docker Compose

### 用户角色（仅2种）
1. **考生 (user)**: 报名、查询、支付
2. **管理员 (admin)**: 审核、管理考试、发布公告

### 性能要求
- 支持 500 并发用户（不是企业级的数万并发）
- 响应时间 < 3秒
- 数据库单表设计（无分库分表）

---

## 🚀 快速启动

### 1. 启动 MySQL 数据库

```bash
# 启动 MySQL 8.4 容器
docker-compose up -d

# 查看容器状态
docker-compose ps

# 查看日志（如果有问题）
docker-compose logs mysql

# 停止服务
docker-compose down
```

### 2. 数据库连接信息

| 配置项 | 值 |
|--------|-----|
| 主机 | `localhost` (或 `127.0.0.1`) |
| 端口 | `3306` |
| 用户名 | `root` |
| 密码 | `123456` |
| 数据库名 | `exam_registration_system` |
| 字符集 | `utf8mb4` |

**连接命令:**
```bash
mysql -h 127.0.0.1 -P 3306 -u root -p123456
```

**在容器内连接:**
```bash
docker-compose exec mysql mysql -uroot -p123456
```

### 3. 测试账号

| 用户名 | 密码 | 角色 | 真实姓名 |
|--------|------|------|----------|
| `admin` | `admin123` | 管理员 | 系统管理员 |
| `13800138000` | `123456` | 考生 | 张三 |

**注意:** 数据库中密码使用 BCrypt 加密存储。

---

## 📁 项目结构

```
exam-registration-system/
├── docs/                          # 📚 完整的设计文档（必看）
│   ├── 需求文档.md                  # 功能需求、性能需求
│   ├── 系统设计文档.md              # 架构设计、API设计
│   └── 数据库设计文档.md            # 数据表设计、索引设计
├── sql/
│   └── init.sql                   # 数据库初始化脚本（6张表 + 测试数据）
├── docker-compose.yml             # MySQL 8.4 容器配置
├── docker/
│   └── README.md                  # Docker 详细使用说明
├── data/                          # ⚠️ Docker 数据持久化目录（已gitignore）
├── frontend/                      # 🚧 待开发：React 前端
├── backend/                       # 🚧 待开发：Spring Boot 后端
├── .gitignore                     # 全面的忽略规则
└── README.md                      # 项目说明

总文档量: 1500+ 行详细设计文档
```

---

## 🗄️ 数据库设计

### 核心表结构（6张表）

1. **sys_user** - 用户表
   - 字段: id, username(手机号), password(BCrypt), real_name, role, status, created_at
   - 索引: uk_username(唯一), idx_role
   - 角色: `user` 或 `admin`

2. **exam** - 考试信息表
   - 字段: id, exam_name, exam_type, exam_date, registration_start/end, fee, status
   - 索引: idx_exam_date, idx_status
   - 状态: `draft`, `published`, `registration_open`, `registration_closed`, `completed`, `cancelled`

3. **exam_site** - 考点信息表
   - 字段: id, exam_id, site_name, province, city, district, address, capacity, contact_info
   - 索引: idx_exam_id, idx_province_city
   - 外键: exam_id → exam(id)

4. **registration** - 报名记录表
   - 字段: id, exam_id, user_id, site_id, id_card(AES加密), phone(AES加密), status, audit_result
   - 索引: uk_exam_user(唯一), idx_user_id, idx_exam_id
   - 状态: `pending`, `approved`, `rejected`, `cancelled`
   - 外键: exam_id → exam(id), user_id → sys_user(id), site_id → exam_site(id)

5. **payment_order** - 支付订单表
   - 字段: id, registration_id, order_no(唯一), amount, status, payment_method, paid_at
   - 索引: uk_order_no, idx_registration_id
   - 状态: `unpaid`, `paid`, `refunded`, `cancelled`
   - 外键: registration_id → registration(id)

6. **notice** - 公告表
   - 字段: id, title, content(TEXT), type, status, published_at, publisher_id
   - 索引: idx_published_at, idx_type
   - 类型: `system`, `exam`
   - 外键: publisher_id → sys_user(id)

### E-R 关系图

```
sys_user (1) ───< (N) registration
                      │
exam (1) ───────────< (N) registration
    │
    └───< (N) exam_site (1) ───< (N) registration
                                      │
                                      └───< (1) payment_order

sys_user (1) ───< (N) notice
```

---

## ⚠️ 关键技术决策

### 1. MySQL 8.4 兼容性问题

**重要:** MySQL 8.4 已废弃以下参数，不要在配置中使用：
- ❌ `default-authentication-plugin=mysql_native_password` (已废弃)
- ✅ 使用默认的 `caching_sha2_password` 认证插件

**docker-compose.yml 中正确的配置:**
```yaml
command:
  - --character-set-server=utf8mb4
  - --collation-server=utf8mb4_unicode_ci
  # 不要添加 default-authentication-plugin 参数
```

### 2. 数据库自动初始化

- Docker Compose 通过 `MYSQL_DATABASE` 环境变量自动创建数据库
- `sql/init.sql` 脚本在容器首次启动时自动执行
- **不需要** 在 init.sql 中写 `CREATE DATABASE` 或 `USE` 语句

### 3. 数据持久化

- MySQL 数据存储在 `./data/mysql/` 目录
- 该目录已加入 `.gitignore`，不会提交到 Git
- ⚠️ 执行 `docker-compose down -v` 会删除所有数据

### 4. 简化设计原则

**已移除的复杂功能（用户明确要求简化）:**
- ❌ 人脸识别、OCR 证件识别
- ❌ 第三方登录（微信、支付宝）
- ❌ 复杂的权限管理系统（只有2种角色）
- ❌ 发票管理
- ❌ 微服务架构
- ❌ 分库分表

**保留的核心功能:**
- ✅ 考生报名、审核、支付
- ✅ 管理员管理考试、考点、公告
- ✅ 基础的 JWT 认证
- ✅ 敏感信息加密（身份证、手机号用 AES）

### 5. 🚨 数据库变更管理规范（重要）

**所有数据库变更必须通过 SQL 文件，不允许直接执行数据库命令！**

**适用范围:**
- ✅ 调整表结构（ALTER TABLE）
- ✅ 增加/修改索引
- ✅ 增加测试数据（INSERT）
- ✅ 修改现有数据（UPDATE）
- ✅ 删除数据（DELETE）

**标准流程:**

1. **创建 SQL 变更文件**
   ```bash
   # 在 sql/ 目录下创建带时间戳的 SQL 文件
   sql/
   ├── init.sql                           # 初始化脚本（不要修改）
   ├── 20251016_add_exam_data.sql         # 示例：添加测试考试数据
   ├── 20251017_alter_user_table.sql      # 示例：修改用户表结构
   └── 20251018_add_more_sites.sql        # 示例：添加更多考点
   ```

2. **编写 SQL 变更内容**
   ```sql
   -- 20251016_add_exam_data.sql
   -- 描述：添加2025年成人高考测试数据

   INSERT INTO `exam` (`exam_name`, `exam_type`, `exam_date`, ...) VALUES
   ('2025年成人高考', '统考', '2025-10-24 09:00:00', ...);
   ```

3. **执行变更并重启容器**
   ```bash
   # 方式1: 直接在容器中执行SQL文件
   docker-compose exec -T mysql mysql -uroot -p123456 exam_registration_system < sql/20251016_add_exam_data.sql

   # 方式2: 如果需要重建数据库（慎用）
   docker-compose down -v
   rm -rf data/mysql
   docker-compose up -d
   ```

4. **提交到 Git**
   ```bash
   git add sql/20251016_add_exam_data.sql
   git commit -m "feat: 添加2025年成人高考测试数据"
   git push
   ```

**❌ 禁止的做法:**
```bash
# ❌ 不要直接执行 SQL 命令
docker-compose exec mysql mysql -uroot -p123456 -e "INSERT INTO exam ..."

# ❌ 不要在数据库客户端（Navicat等）中直接手动操作
# ❌ 不要在 MySQL 命令行中直接执行变更
```

**✅ 正确的做法:**
```bash
# ✅ 所有变更都通过 SQL 文件
# ✅ 文件名包含时间戳和描述
# ✅ 执行后提交到 Git 进行版本控制
# ✅ 保持数据库变更可追溯、可重现
```

**原因说明:**
- 📝 **版本控制**: 所有数据库变更都在 Git 中可追溯
- 🔄 **可重现性**: 任何人都可以通过 SQL 文件重建相同的数据库状态
- 👥 **团队协作**: 其他开发者可以看到你做了什么变更
- 🐛 **问题排查**: 出问题时可以快速定位是哪个 SQL 文件导致的
- 🎓 **毕设答辩**: 可以向答辩老师展示完整的数据库变更历史

---

## 🔐 安全设计

1. **密码加密**: BCrypt（强度10）
   - 初始化脚本中已包含加密后的测试密码

2. **敏感数据加密**: AES-256
   - `id_card` 和 `phone` 字段在 registration 表中加密存储

3. **身份认证**: JWT Token
   - 后端开发时使用 Spring Security + JWT

4. **数据脱敏**:
   - 日志中不输出敏感信息
   - API 响应中手机号脱敏显示（138****8000）

---

## 📝 常用操作

### 数据库操作

```bash
# 查看所有表
docker-compose exec mysql mysql -uroot -p123456 -e "USE exam_registration_system; SHOW TABLES;"

# 查看用户数据
docker-compose exec mysql mysql -uroot -p123456 -e "USE exam_registration_system; SELECT * FROM sys_user;"

# 重新初始化数据库（⚠️ 会删除所有数据）
docker-compose down -v
rm -rf data/mysql
docker-compose up -d
```

### Docker 操作

```bash
# 查看容器资源使用
docker stats exam-mysql

# 进入容器
docker-compose exec mysql bash

# 导出数据库备份
docker-compose exec mysql mysqldump -uroot -p123456 exam_registration_system > backup.sql

# 导入数据库
docker-compose exec -T mysql mysql -uroot -p123456 exam_registration_system < backup.sql
```

### Git 提交规范

遵循 **Conventional Commits** 规范:

```bash
# 功能开发
git commit -m "feat: 实现用户登录功能"

# Bug 修复
git commit -m "fix: 修复报名时考点选择错误"

# 文档更新
git commit -m "docs: 更新 API 接口文档"

# 代码重构
git commit -m "refactor: 优化数据库查询性能"

# 测试
git commit -m "test: 添加支付模块单元测试"

# 样式调整
git commit -m "style: 格式化代码"

# 性能优化
git commit -m "perf: 优化列表分页查询"

# 构建相关
git commit -m "build: 升级 Spring Boot 版本"
```

---

## 🛠️ 开发指南

### 后端开发（待开始）

**技术选型:**
- Spring Boot 2.7.x
- Spring Security + JWT
- MyBatis-Plus 3.5.x
- Hutool 工具库
- Lombok

**项目结构建议:**
```
backend/
├── src/main/java/com/exam/
│   ├── controller/        # REST API 控制器
│   ├── service/           # 业务逻辑层
│   ├── mapper/            # MyBatis-Plus Mapper
│   ├── entity/            # 实体类（对应数据库表）
│   ├── dto/               # 数据传输对象
│   ├── vo/                # 视图对象
│   ├── config/            # 配置类（Security, CORS等）
│   ├── common/            # 通用类（Result, Constants等）
│   └── util/              # 工具类（加密、JWT等）
└── src/main/resources/
    ├── application.yml
    └── mapper/            # MyBatis XML 映射文件
```

**数据库连接配置:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_registration_system?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 前端开发（待开始）

**技术选型:**
- React 18
- Ant Design 5.x
- Vite
- React Router
- Axios
- ECharts（数据可视化）

**项目结构建议:**
```
frontend/
├── src/
│   ├── components/        # 公共组件
│   ├── pages/             # 页面组件
│   │   ├── login/         # 登录页
│   │   ├── student/       # 考生页面
│   │   └── admin/         # 管理员页面
│   ├── api/               # API 接口封装
│   ├── utils/             # 工具函数
│   ├── router/            # 路由配置
│   ├── store/             # 状态管理（Context/Redux）
│   └── App.jsx
└── vite.config.js
```

---

## 📚 详细文档位置

| 文档 | 位置 | 行数 | 内容 |
|------|------|------|------|
| 需求文档 | `docs/需求文档.md` | 525行 | 功能需求、用户角色、性能指标 |
| 系统设计 | `docs/系统设计文档.md` | 600+行 | 架构图、流程图、API设计 |
| 数据库设计 | `docs/数据库设计文档.md` | 550+行 | E-R图、表结构、索引设计 |
| Docker说明 | `docker/README.md` | 378行 | Docker详细使用、故障排查 |

---

## ❓ 常见问题

### 1. 端口 3306 被占用

**检查本地 MySQL 服务:**
```bash
# macOS
brew services list
brew services stop mysql

# Linux
sudo systemctl status mysql
sudo systemctl stop mysql
```

### 2. 容器启动失败

**查看日志:**
```bash
docker-compose logs mysql
```

**完全重置:**
```bash
docker-compose down -v
rm -rf data/mysql
docker-compose up -d
```

### 3. 无法连接数据库

**检查清单:**
- [ ] 容器是否运行: `docker-compose ps`
- [ ] 端口是否正确: 3306
- [ ] 使用 `127.0.0.1` 而不是 `localhost`（避免 Unix socket 连接）
- [ ] 密码是否正确: `123456`

**测试连接:**
```bash
docker-compose exec mysql mysql -uroot -p123456 -e "SELECT 1;"
```

### 4. 初始化脚本未执行

**原因:** 数据目录已存在时不会重新执行初始化脚本

**解决:**
```bash
docker-compose down -v    # 删除数据卷
rm -rf data/mysql         # 删除本地数据
docker-compose up -d      # 重新启动（会执行init.sql）
```

---

## 🎯 开发路线图

- [x] 项目初始化和 Git 仓库
- [x] 需求文档、系统设计文档、数据库设计文档
- [x] Docker Compose 配置（MySQL 8.4）
- [x] 数据库表结构和初始化脚本
- [ ] 后端项目搭建（Spring Boot）
- [ ] 前端项目搭建（React + Vite）
- [ ] 用户认证模块（JWT）
- [ ] 考试管理模块
- [ ] 报名审核模块
- [ ] 支付模块（模拟）
- [ ] 公告管理模块
- [ ] 数据统计和报表
- [ ] 系统测试
- [ ] 部署文档

---

## 💡 给 Claude Code 的提示

1. **这是毕业设计项目，不是生产系统**
   - 避免建议微服务、分库分表、Redis 集群等过度设计
   - 功能简单实用即可，重点是完整性和可演示性

2. **数据库已完整设计**
   - 6张表的设计已经过深思熟虑
   - 除非用户明确要求，不要建议大幅修改表结构

3. **文档优先**
   - 修改功能前先查看 `docs/` 下的文档
   - 保持代码实现与文档一致

4. **安全性要适度**
   - 密码加密（BCrypt）是必须的
   - 敏感数据加密（AES）是必须的
   - 但不需要实现复杂的安全审计、入侵检测等

5. **MySQL 8.4 特性**
   - 不要使用已废弃的参数
   - 参考 docker-compose.yml 中的配置

6. **代码风格**
   - 后端: 遵循阿里巴巴 Java 开发手册
   - 前端: 使用 ESLint + Prettier
   - Git 提交: Conventional Commits 规范

7. **用户反馈很重要**
   - 如果用户说"太复杂"，立即简化
   - 用户要求用中文回答和交流

---

## 📞 联系信息

- **Git 仓库**: https://github.com/scguoi/exam-registration-system
- **开发者**: 用户正在使用 IntelliJ IDEA 开发
- **操作系统**: macOS (Darwin 25.0.0)

---

**最后更新:** 2025-10-16
**文档版本:** 1.0
