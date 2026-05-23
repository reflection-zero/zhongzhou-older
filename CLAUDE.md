# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**中州养老 (Zhongzhou Elderly Care)** — an elderly care facility management system forked from the **RuoYi** rapid-development framework. Java 11, Spring Boot 2.5.15, Maven multi-module project. Manages elders (residents), rooms/beds, nursing plans, IoT device data, contracts, reservations, and alerts.

## Build & Run

```bash
# Build all modules (skip tests)
mvn clean install -DskipTests

# Run the app (dev profile, port 8080)
cd zzyl-admin/target
java -jar zzyl-admin.jar

# Run with specific profile
java -jar zzyl-admin.jar --spring.profiles.active=dev

# Run tests for a specific module
mvn test -pl zzyl-nursing-platform

# Run a single test class
mvn test -pl zzyl-admin -Dtest=RedisTest
```

The main class is [RuoYiApplication.java](zzyl-admin/src/main/java/com/zzyl/RuoYiApplication.java). It excludes `DataSourceAutoConfiguration` because the project uses Druid with a custom dynamic datasource, and enables `@EnableScheduling`.

Profiles: `dev` (port 8080), `test`, `prod` (port 9000). Active profile is set in [application.yml](zzyl-admin/src/main/resources/application.yml).

## Module Architecture

```
zzyl-admin              Web entry point — controllers, config, resources
  ├── zzyl-framework     Security (JWT, Spring Security, aspects, interceptors, dynamic DS)
  │     └── zzyl-system  System domain & mappers (users, roles, menus, depts, dicts)
  │           └── zzyl-common  Shared base: annotations, BaseEntity, utils, constants, enums
  ├── zzyl-nursing-platform  Core business: elders, beds, nursing plans, IoT, alerts, contracts
  │     ├── zzyl-oss     Alibaba Cloud OSS file upload (AliyunOSSOperator)
  │     └── zzyl-framework (transitive)
  ├── zzyl-quartz        Quartz job scheduler (SysJob, job CRUD)
  └── zzyl-generator     Velocity-based code generator from DB tables
```

## Request Processing Pipeline

1. **Filters**: `CorsFilter` → `XssFilter` (URLs: `/system/*`, `/monitor/*`, `/tool/*`, excludes `/system/notice`) → `RepeatableFilter` (wraps request for multi-read)
2. **Spring Security**: `JwtAuthenticationTokenFilter` extracts JWT from `Authorization` header, loads `LoginUser` from Redis, sets `SecurityContext`. Session is STATELESS, CSRF disabled.
3. **Interceptors**: `RepeatSubmitInterceptor` (form duplicate prevention via `@RepeatSubmit`), `MemberInterceptor` (mobile/member API auth on `/member/**`)
4. **AOP Aspects**: `@Log` (operation logging), `@DataScope` (row-level data permission SQL injection), `@DataSource` (master/slave routing), `@RateLimiter` (Redis sliding-window rate limiting)
5. **Controller → Service → MyBatis-Plus Mapper** (MyBatis-Plus configured in `MybatisPlusConfig`, auto-fills `createBy`/`createTime`/`updateBy`/`updateTime` via `MyMetaObjectHandler`)

## Key Architecture Patterns

**Dual authentication**: Admin users go through Spring Security JWT filter. Mobile/member users go through `MemberInterceptor` on `/member/**` paths — separate JWT parsing, separate Redis user context (`UserThreadLocal`).

**Dynamic datasource**: `@DataSource` annotation + `DataSourceAspect` route to master or slave. Enum values: `MASTER`, `SLAVE`.

**Data scope**: `@DataScope` annotation + `DataScopeAspect` inject row-level SQL filters based on user's role data permissions (5 levels: all data, custom, dept-only, dept-and-children, self-only). Works via the `params` map on `BaseEntity` combined with MyBatis XML `${params.dataScope}`.

**IoT pipeline**: Huawei IoTDA platform → AMQP 1.0 (Apache Qpid JMS client, `AmqpClient` implements `ApplicationRunner`, starts on boot) → `DeviceDataService.batchInsertDeviceData()` → database.

**Real-time alerts**: `AlertService` → `WebSocketServer` (`@ServerEndpoint("/ws/{sid}")`) pushes `AlertNotifyVo` to connected clients by userId.

**Nursing business automation**: Quartz jobs in `zzyl-nursing-platform/job/` — `AlertJob`, `ContractJob`, `CreateNursingTaskJob`, `DeviceDataJob`, `ReservationJob`.

## Domain Model (nursing-platform)

- **Spatial**: Floor → RoomType → Room → Bed
- **Residents**: Elder, FamilyMember, FamilyMemberElder (linking table)
- **Admission flow**: Reservation → CheckInConfig → CheckIn → Contract
- **Nursing**: NursingLevel → NursingPlan → NursingProject → NursingTask. Also NursingElder (assignment), HealthAssessment.
- **IoT**: Device → DeviceData (sensor readings), AlertRule → AlertData

All entities extend `BaseEntity` (createBy, createTime, updateBy, updateTime, remark, params map) and use Lombok `@Data`.

## Configuration Notes

- **Database**: Druid connection pool with master DB configured in profile YAMLs. The `application-secret.yml` file is gitignored for local secrets.
- **Redis**: Used for caching, JWT token storage, rate limiting, session management.
- **MyBatis-Plus**: Mapper XMLs at `classpath*:mapper/**/*Mapper.xml`. Type aliases package: `com.zzyl.**.domain`. ID strategy: ASSIGN_ID.
- **Swagger**: Enabled in dev, mapped to `/dev-api` path prefix.
- **Baidu AI**: Qianfan (ERNIE-4.0-8K-Preview) integration via `AIModelInvoker` in zzyl-common.
- **Dept config**: `nursingId: 201` — the department ID used for nursing staff data scope.
