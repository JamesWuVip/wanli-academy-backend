# 使用OpenJDK 17作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制Maven构建的JAR文件
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
CMD ["java", "-Xmx512m", "-Dserver.port=${PORT:-8080}", "-Dspring.profiles.active=production", "-Dmanagement.server.port=${PORT:-8080}", "-jar", "app.jar"]