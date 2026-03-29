# ============================================================
#  博客后端 Dockerfile — 多阶段构建
#  Stage 1: Maven 编译打包
#  Stage 2: 精简 JRE 运行
#  dockerfile是用于构建可以一键运行的docker镜像的文件，用于将博客后端打包成一个镜像，然后通过docker run命令运行
#  1. 构建阶段：使用maven编译打包博客后端
#  2. 运行阶段：使用精简的jre运行博客后端
#  3. 健康检查：使用curl命令检查博客后端是否健康
#  4. 暴露端口：暴露博客后端端口
#  5. 入口点：使用java命令运行博客后端
#  6. 时区：设置时区为Asia/Shanghai
#  7. 安装curl：安装curl命令用于健康检查
# ============================================================

# ---- 构建阶段 ----
# 从官方拉一个 Maven3.8 + JDK11 的镜像，起名builder
FROM maven:3.8-openjdk-11 AS builder
# 切换到/build目录
WORKDIR /build
# 先拷贝 pom.xml 到当前目录
COPY blog-springboot/pom.xml .
# 使用mvn命令下载依赖，-B参数表示批量模式，加快下载速度，go-offline表示离线模式，不在线下载依赖
RUN mvn dependency:go-offline -B
# 再拷贝源码编译
COPY blog-springboot/src ./src
# 使用mvn命令打包，-DskipTests表示跳过测试，-B参数表示批量模式，加快打包速度
RUN mvn package -DskipTests -B

# ---- 运行阶段 ----
# 从官方拉一个 JDK11 的精简镜像，起名runner
FROM eclipse-temurin:11-jre
# 切换到/app目录
WORKDIR /app
# 设置时区为Asia/Shanghai
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 安装 curl（健康检查需要）
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
# 从构建阶段拷贝 jar
COPY --from=builder /build/target/blog-springboot-*.jar app.jar
# 健康检查（端口与 docker-compose 中 SERVER_PORT=8000 一致）
HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=60s \
  CMD curl -f http://localhost:8000/ || exit 1
# 暴露8000端口 
EXPOSE 8000
# 使用java命令运行博客后端，-Djava.security.egd=file:/dev/./urandom表示使用随机数生成器，-jar表示运行jar包
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
