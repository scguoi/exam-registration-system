# Docker 环境使用说明

本项目使用 Docker Compose 管理开发环境，目前包含 MySQL 8.4 数据库。

---

## 📋 前置要求

确保已安装以下软件:

- **Docker:** 20.10+ ([安装指南](https://docs.docker.com/get-docker/))
- **Docker Compose:** 2.0+ (Docker Desktop 已内置)

验证安装:
```bash
docker --version
docker-compose --version
```

---

## 🚀 快速开始

### 1. 启动所有服务

```bash
# 在项目根目录执行
docker-compose up -d
```

**参数说明:**
- `-d`: 后台运行 (detached mode)

### 2. 查看服务状态

```bash
# 查看所有容器
docker-compose ps

# 查看容器日志
docker-compose logs mysql

# 实时查看日志
docker-compose logs -f mysql
```

### 3. 停止服务

```bash
# 停止所有服务
docker-compose stop

# 停止并删除容器
docker-compose down

# 停止并删除容器、网络、数据卷（⚠️ 会删除所有数据）
docker-compose down -v
```

---

## 📊 MySQL 服务

### 连接信息

| 配置项 | 值 |
|--------|-----|
| **主机** | localhost (或 127.0.0.1) |
| **端口** | 3306 |
| **用户名** | root |
| **密码** | 123456 |
| **数据库名** | exam_registration_system |
| **字符集** | utf8mb4 |

### 连接方式

**方式1: 命令行连接**
```bash
# 进入 MySQL 容器
docker-compose exec mysql bash

# 登录 MySQL
mysql -uroot -p123456

# 使用数据库
USE exam_registration_system;

# 查看所有表
SHOW TABLES;
```

**方式2: 本地客户端连接**

使用 Navicat、DataGrip、MySQL Workbench 等工具:
- Host: `localhost`
- Port: `3306`
- Username: `root`
- Password: `123456`
- Database: `exam_registration_system`

**方式3: IDEA 数据库工具连接**

在 IntelliJ IDEA 中:
1. 打开 Database 工具窗口 (View → Tool Windows → Database)
2. 点击 `+` → Data Source → MySQL
3. 填入上述连接信息
4. Test Connection → OK

### 数据初始化

容器首次启动时会自动执行 `sql/init.sql` 脚本，创建所有表并插入初始数据。

**手动重新初始化数据库:**
```bash
# 1. 停止并删除容器和数据
docker-compose down -v

# 2. 删除本地数据目录
rm -rf data/mysql

# 3. 重新启动（会自动执行初始化脚本）
docker-compose up -d

# 4. 查看初始化日志
docker-compose logs mysql
```

### 数据持久化

数据存储在 `data/mysql/` 目录中（已加入 .gitignore）:
```
data/
└── mysql/          # MySQL 数据文件
    ├── auto.cnf
    ├── binlog.*
    ├── exam_registration_system/  # 数据库文件
    └── ...
```

**⚠️ 重要提示:**
- `data/` 目录已被 Git 忽略，不会提交到仓库
- 删除 `data/` 目录会丢失所有数据
- 建议定期备份重要数据

---

## 🔧 配置说明

### MySQL 配置文件

配置文件位置: `docker/mysql/conf/my.cnf`

**主要配置:**
```ini
# 字符集
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 时区
default-time-zone = '+08:00'

# 连接数
max_connections = 200

# 缓存
innodb_buffer_pool_size = 512M

# 慢查询日志
slow_query_log = 1
long_query_time = 2
```

**修改配置后重启:**
```bash
docker-compose restart mysql
```

### 环境变量

在 `docker-compose.yml` 中配置:

```yaml
environment:
  MYSQL_ROOT_PASSWORD: 123456        # root 密码
  MYSQL_DATABASE: exam_registration_system  # 数据库名
  TZ: Asia/Shanghai                  # 时区
```

---

## 🛠️ 常用命令

### 容器管理

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose stop

# 重启服务
docker-compose restart mysql

# 查看服务状态
docker-compose ps

# 查看资源使用
docker stats exam-mysql
```

### 日志查看

```bash
# 查看所有日志
docker-compose logs

# 查看 MySQL 日志
docker-compose logs mysql

# 实时查看日志（Ctrl+C 退出）
docker-compose logs -f mysql

# 查看最近 100 行日志
docker-compose logs --tail=100 mysql
```

### 数据库操作

```bash
# 进入 MySQL 容器
docker-compose exec mysql bash

# 在容器内执行 SQL
docker-compose exec mysql mysql -uroot -p123456 -e "SHOW DATABASES;"

# 导出数据库
docker-compose exec mysql mysqldump -uroot -p123456 exam_registration_system > backup.sql

# 导入数据库
docker-compose exec -T mysql mysql -uroot -p123456 exam_registration_system < backup.sql
```

### 清理命令

```bash
# 删除停止的容器
docker-compose rm

# 删除未使用的镜像
docker image prune

# 删除所有未使用的资源（慎用）
docker system prune -a
```

---

## ❓ 常见问题

### 1. 端口 3306 被占用

**错误信息:**
```
Error: bind: address already in use
```

**解决方法:**
```bash
# 方法1: 停止本地 MySQL 服务
# macOS
brew services stop mysql

# Linux
sudo systemctl stop mysql

# 方法2: 修改 docker-compose.yml 中的端口映射
ports:
  - "3307:3306"  # 改为 3307
```

### 2. 容器启动失败

**查看错误日志:**
```bash
docker-compose logs mysql
```

**常见原因:**
- 端口冲突
- 数据目录权限问题
- 配置文件错误

**解决方法:**
```bash
# 完全重置
docker-compose down -v
rm -rf data/mysql
docker-compose up -d
```

### 3. 无法连接数据库

**检查清单:**
1. 容器是否正常运行: `docker-compose ps`
2. 端口是否映射正确: `docker-compose port mysql 3306`
3. 防火墙是否阻止连接
4. 用户名密码是否正确

**测试连接:**
```bash
docker-compose exec mysql mysql -uroot -p123456 -e "SELECT 1;"
```

### 4. 数据丢失

**原因:**
- 执行了 `docker-compose down -v`
- 删除了 `data/` 目录

**预防措施:**
```bash
# 定期备份
docker-compose exec mysql mysqldump -uroot -p123456 exam_registration_system > backup_$(date +%Y%m%d).sql

# 停止服务时不要删除数据卷
docker-compose down  # ✅ 正确
docker-compose down -v  # ❌ 会删除数据
```

---

## 📚 扩展配置

### 添加其他服务（未来）

后续可以添加 Redis、后端服务等:

```yaml
# 在 docker-compose.yml 中添加
services:
  mysql:
    # ... 现有配置

  redis:
    image: redis:7-alpine
    container_name: exam-redis
    ports:
      - "6379:6379"
    volumes:
      - ./data/redis:/data
    networks:
      - exam-network

  backend:
    build: ./backend
    container_name: exam-backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    networks:
      - exam-network
```

---

## 🔗 相关链接

- [Docker 官方文档](https://docs.docker.com/)
- [Docker Compose 文档](https://docs.docker.com/compose/)
- [MySQL 8.4 文档](https://dev.mysql.com/doc/refman/8.4/en/)

---

**最后更新:** 2025-10-16
