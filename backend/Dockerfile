# 使用官方的 OpenJDK 17 镜像作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制 Maven 构建的 JAR 文件到容器中
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置 JVM 参数和启动命令
ENTRYPOINT ["java", "-Xmx512m", "-Dspring.profiles.active=production", "-jar", "app.jar"]