# 在线考试报名系统 - 后端服务

## 项目简介

基于Spring Boot开发的在线考试报名系统后端服务，提供用户注册登录、考试管理、报名审核、在线缴费等核心功能。

## 技术栈

- **开发语言**: Java 11
- **核心框架**: Spring Boot 2.7.18
- **安全框架**: Spring Security + JWT
- **ORM框架**: MyBatis-Plus 3.5.3.1
- **数据库**: MySQL 8.0
- **工具类**: Hutool 5.8.20
- **构建工具**: Maven

## 项目结构

```
backend/
├── src/main/java/com/exam/
│   ├── ExamRegistrationSystemApplication.java    # 启动类
│   ├── common/                                   # 通用类
│   │   ├── Result.java                          # 统一响应结果
│   │   └── PageResult.java                      # 分页结果
│   ├── config/                                  # 配置类
│   │   ├── SecurityConfig.java                  # 安全配置
│   │   └── MybatisPlusConfig.java               # MyBatis Plus配置
│   ├── controller/                              # 控制器层
│   │   ├── UserController.java                  # 用户控制器
│   │   ├── ExamController.java                  # 考试控制器
│   │   ├── NoticeController.java                # 公告控制器
│   │   └── FileController.java                  # 文件控制器
│   ├── dto/                                     # 数据传输对象
│   │   ├── LoginRequest.java                    # 登录请求
│   │   ├── RegisterRequest.java                 # 注册请求
│   │   ├── UserUpdateRequest.java               # 用户更新请求
│   │   ├── ExamCreateRequest.java               # 考试创建请求
│   │   └── ExamUpdateRequest.java               # 考试更新请求
│   ├── entity/                                  # 实体类
│   │   ├── SysUser.java                         # 用户实体
│   │   ├── Exam.java                            # 考试实体
│   │   ├── ExamSite.java                        # 考点实体
│   │   ├── Registration.java                    # 报名实体
│   │   ├── PaymentOrder.java                    # 订单实体
│   │   └── Notice.java                          # 公告实体
│   ├── exception/                               # 异常处理
│   │   ├── BusinessException.java               # 业务异常
│   │   └── GlobalExceptionHandler.java          # 全局异常处理器
│   ├── mapper/                                  # 数据访问层
│   │   ├── SysUserMapper.java                   # 用户Mapper
│   │   ├── ExamMapper.java                      # 考试Mapper
│   │   ├── ExamSiteMapper.java                  # 考点Mapper
│   │   ├── RegistrationMapper.java              # 报名Mapper
│   │   ├── PaymentOrderMapper.java              # 订单Mapper
│   │   └── NoticeMapper.java                    # 公告Mapper
│   ├── security/                                # 安全相关
│   │   ├── JwtTokenProvider.java                # JWT Token提供者
│   │   ├── JwtAuthenticationFilter.java         # JWT认证过滤器
│   │   ├── CustomUserDetails.java               # 自定义用户详情
│   │   └── CustomUserDetailsService.java        # 自定义用户详情服务
│   ├── service/                                 # 业务逻辑层
│   │   ├── SysUserService.java                  # 用户服务接口
│   │   ├── ExamService.java                     # 考试服务接口
│   │   ├── NoticeService.java                   # 公告服务接口
│   │   └── impl/                                # 服务实现
│   │       ├── SysUserServiceImpl.java          # 用户服务实现
│   │       ├── ExamServiceImpl.java             # 考试服务实现
│   │       └── NoticeServiceImpl.java           # 公告服务实现
│   └── utils/                                   # 工具类
│       └── FileUtils.java                       # 文件工具类
├── src/main/resources/
│   ├── application.yml                          # 应用配置
│   └── sql/                                     # SQL脚本
│       └── init.sql                             # 数据库初始化脚本
└── pom.xml                                      # Maven配置
```

## 快速开始

### 1. 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE exam_registration_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p exam_registration_system < sql/init.sql
```

### 3. 配置文件

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_registration_system?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: 你的数据库密码
```

### 4. 启动应用

```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

应用启动后访问：http://localhost:8080/api

## API接口

### 用户相关

- `POST /api/v1/users/register` - 用户注册
- `POST /api/v1/users/login` - 用户登录
- `GET /api/v1/users/info` - 获取用户信息
- `PUT /api/v1/users/info` - 更新用户信息
- `PUT /api/v1/users/password` - 修改密码

### 考试相关

- `GET /api/v1/exams/available` - 获取可报名考试列表
- `GET /api/v1/exams/{id}` - 获取考试详情
- `GET /api/v1/exams` - 获取考试列表（管理员）
- `POST /api/v1/exams` - 创建考试（管理员）
- `PUT /api/v1/exams/{id}` - 更新考试（管理员）
- `DELETE /api/v1/exams/{id}` - 删除考试（管理员）

### 公告相关

- `GET /api/v1/notices` - 获取公告列表
- `GET /api/v1/notices/{id}` - 获取公告详情
- `GET /api/v1/notices/top` - 获取置顶公告

### 文件相关

- `POST /api/v1/files/avatar` - 上传头像
- `POST /api/v1/files/material` - 上传证明材料
- `POST /api/v1/files/exam` - 上传考试文件（管理员）

## 默认账号

### 管理员账号
- 用户名：`admin`
- 密码：`admin123`

### 测试考生账号
- 用户名：`13800138000`
- 密码：`123456`

## 开发说明

### 1. 数据库设计

系统包含6张核心表：
- `sys_user` - 用户表
- `exam` - 考试表
- `exam_site` - 考点表
- `registration` - 报名表
- `payment_order` - 缴费订单表
- `notice` - 公告表

### 2. 安全机制

- 使用JWT Token进行身份认证
- 密码使用BCrypt加密存储
- 敏感信息（身份证号、手机号）使用AES加密
- 支持登录失败锁定机制

### 3. 文件上传

- 支持头像、证明材料、考试文件上传
- 文件按日期分目录存储
- 支持文件类型和大小验证

### 4. 异常处理

- 全局异常处理器统一处理异常
- 自定义业务异常类
- 统一的响应格式

## 部署说明

### 1. 打包应用

```bash
mvn clean package -DskipTests
```

### 2. 运行JAR包

```bash
java -jar target/exam-registration-system-1.0.0.jar
```

### 3. Docker部署

```dockerfile
FROM openjdk:11-jre-slim
COPY target/exam-registration-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 注意事项

1. 生产环境请修改JWT密钥
2. 文件上传路径需要确保有写入权限
3. 建议使用HTTPS协议
4. 定期备份数据库
5. 监控应用日志

## 许可证

本项目仅用于学习和研究目的。
