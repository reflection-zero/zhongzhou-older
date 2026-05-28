FROM node:18-alpine AS frontend-builder

WORKDIR /ui
COPY ruoyi-ui/package.json ruoyi-ui/package-lock.json* ./
RUN npm install
COPY ruoyi-ui/ .
RUN npm run build:prod

FROM maven:3-openjdk-11 AS backend-builder

WORKDIR /build

# 先复制所有 POM，利用 Docker 层缓存加速依赖下载
COPY pom.xml ./
COPY zzyl-admin/pom.xml ./zzyl-admin/
COPY zzyl-framework/pom.xml ./zzyl-framework/
COPY zzyl-system/pom.xml ./zzyl-system/
COPY zzyl-quartz/pom.xml ./zzyl-quartz/
COPY zzyl-generator/pom.xml ./zzyl-generator/
COPY zzyl-common/pom.xml ./zzyl-common/
COPY zzyl-nursing-platform/pom.xml ./zzyl-nursing-platform/
COPY zzyl-oss/pom.xml ./zzyl-oss/

RUN mvn dependency:go-offline -B -DskipTests || true

# 复制源码
COPY . .

# 复制前端构建产物到 static 目录
COPY --from=frontend-builder /ui/dist/. ./zzyl-admin/src/main/resources/static/

# 只构建 zzyl-admin 及其依赖模块
RUN mvn clean package -B -DskipTests -pl zzyl-admin -am

FROM openjdk:11.0-jre-buster

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=backend-builder /build/zzyl-admin/target/zzyl-admin.jar /app.jar

EXPOSE 9000

ENTRYPOINT ["java", \
    "-Xms256m", \
    "-Xmx1024m", \
    "-XX:MetaspaceSize=128m", \
    "-XX:MaxMetaspaceSize=512m", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app.jar"]
