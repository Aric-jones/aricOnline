# ============================================================
#  博客后端 Dockerfile — 多阶段构建
#  Stage 1: Maven 编译打包
#  Stage 2: 精简 JRE 运行
# ============================================================

# ---- 构建阶段 ----
FROM maven:3.8-openjdk-11 AS builder
WORKDIR /build
# 先拷贝 pom.xml 缓存依赖层
COPY blog-springboot/pom.xml .
RUN mvn dependency:go-offline -B
# 再拷贝源码编译
COPY blog-springboot/src ./src
RUN mvn package -DskipTests -B

# ---- 运行阶段 ----
FROM eclipse-temurin:11-jre
WORKDIR /app
# 时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 安装 curl（健康检查需要）
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
# 从构建阶段拷贝 jar
COPY --from=builder /build/target/blog-springboot-*.jar app.jar
# 健康检查（端口与 docker-compose 中 SERVER_PORT=8000 一致）
HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=60s \
  CMD curl -f http://localhost:8000/ || exit 1
EXPOSE 8000
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
