# 构建阶段
FROM maven:3.8.6-openjdk-11-slim AS build

# 设置工作目录
WORKDIR /app

# 复制pom.xml文件
COPY pom.xml .

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 从构建阶段复制jar包
COPY --from=build /app/target/exam-registration-system-1.0.0.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads/avatar /app/uploads/materials /app/uploads/exam-files /app/uploads/tickets

# 设置目录权限
RUN chown -R appuser:appuser /app

# 切换到应用用户
USER appuser

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dspring.profiles.active=docker"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
