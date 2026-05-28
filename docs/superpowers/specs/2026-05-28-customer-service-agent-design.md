# 中州养老客服智能体 — 设计规格

**日期**: 2026-05-28
**分支**: `Zyh`
**模型**: DeepSeek V4 Flash (`deepseek-v4-flash`)

## 1. 概述

在现有中州养老管理系统中新增 AI 客服智能体。管理端侧边栏新增"问题咨询"入口，家属可通过账号查看公开养老数据及本人关联老人信息。使用 DeepSeek API，独立于现有百度千帆 AI 基础设施。

### 核心约束
- **回滚安全**: 所有改动在 `Zyh` 分支，`git checkout master` 即可回滚
- **功能保全**: 不修改任何现有业务逻辑，19 个前端 CRUD 页面零影响
- **千帆不动**: `AIModelInvoker` 及千帆 SDK 完整保留，健康评估功能不受影响

## 2. 账户隔离（安全关键）

AI 数据查询**必须**按用户身份严格隔离，防止跨账户数据泄露。

### 管理端 (user_type=0)
- 使用 Spring Security 上下文获取当前 `LoginUser`
- 数据查询遵守 `@DataScope` 部门权限（复用现有数据权限体系）
- 查询结果仅限当前用户部门范围内的数据

### 家属端 (user_type=1)
- 通过 `UserThreadLocal.getUserId()` 获取当前家属 ID（`MemberInterceptor` 设置）
- **硬规则**:
  1. 查询老人信息：仅限 `family_member_elder` 表中与该家属绑定的老人
  2. 查询预约记录：仅限该家属本人创建的预约
  3. 查询合同/入住：仅限该家属绑定老人的关联数据
  4. 公开数据（床位统计、房型价格、护理等级）可查询，但不得包含其他老人个人信息
- 若检测到试图查询非关联老人，拒绝并记录告警日志

### 实现方式
`NursingDataQueryServiceImpl` 每个方法前两步：
1. 校验 `userId` 和 `userType` 非空
2. 家属端查询老人相关数据前，先查 `family_member_elder` 获取授权老人 ID 列表，所有 SQL 加 `WHERE elder_id IN (authorized_ids)`

## 3. DeepSeek API 集成

### API 规格
```
POST https://api.deepseek.com/v1/chat/completions
Authorization: Bearer {api_key}
Content-Type: application/json

{
  "model": "deepseek-v4-flash",
  "messages": [
    {"role": "system", "content": "<system_prompt>"},
    {"role": "user", "content": "<history_msg_1>"},
    {"role": "assistant", "content": "<history_msg_2>"},
    {"role": "user", "content": "<current_message_with_context>"}
  ],
  "temperature": 0.7,
  "max_tokens": 4000
}
```

### 实现类
`DeepSeekAIService` 位于 `zzyl-common/src/main/java/com/zzyl/common/ai/DeepSeekAIService.java`
- 使用 Spring `RestTemplate`（已在现有项目中配置）
- 方法：`String chat(String systemPrompt, List<Message> history, String userMessage)`
- API Key 从配置文件 `deepseek.apiKey` 读取
- 超时设置：连接 10s，读取 60s（AI 响应可能较慢）
- 异常时抛出 `AIException`（自定义 Runtime 异常），上层捕获后返回友好错误消息

### 配置
```yaml
deepseek:
  apiKey: ${DEEPSEEK_API_KEY:your-api-key-here}
  model: deepseek-v4-flash
  temperature: 0.7
  maxTokens: 4000
```

## 4. 数据模型

```sql
CREATE TABLE chat_session (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  user_type TINYINT NOT NULL COMMENT '0=admin,1=family_member',
  title VARCHAR(100) COMMENT '会话标题(首条消息截断)',
  status TINYINT DEFAULT 1 COMMENT '1=active,0=closed',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user (user_id, user_type)
);

CREATE TABLE chat_message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id BIGINT NOT NULL COMMENT 'FK chat_session.id',
  role VARCHAR(10) NOT NULL COMMENT 'user|assistant',
  content TEXT NOT NULL COMMENT '消息内容',
  context_data TEXT COMMENT '注入的养老数据JSON(调试用)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_session (session_id)
);
```

## 5. 架构

```
侧边栏"问题咨询" → ChatPanel (Vue)
  ↕ WebSocket /ws/chat/{userId}/{userType}
    → ChatWebSocketServer (JSR 356)
      → ChatAsyncProcessor (@Async)
        → ChatAIServiceImpl
          ├── DeepSeekAIService (HTTP → api.deepseek.com)
          └── NursingDataQueryServiceImpl
                ├── 公开数据: BedService, RoomTypeService, NursingLevelService
                └── 账户数据: ElderService, ReservationService (权限过滤)
        → ChatMessageMapper / ChatSessionMapper → MySQL
  ↕ REST: /nursing/chat/* (管理端) + /member/chat/* (家属端)
```

## 6. AI Prompt 策略

### System Prompt
```
你是中州养老院的智能客服助手，名叫"小州"。你的职责：

1. 友好、耐心地回答关于养老院服务的问题
2. 基于系统提供的实时数据给出准确回答（数据会在每条问题后附上）
3. 如果不知道答案，诚实告知并建议联系人工客服
4. 绝对不要编造数据——只能使用系统提供的数据回答

你的服务范围：
- 入住咨询（房间类型、价格、入住流程）
- 床位查询（可用床位数量、房间配置）
- 护理等级说明
- 预约参观/探访
- 健康评估说明

安全规则：
- 不得泄露其他老人的个人信息给非关联用户
- 如果用户询问不相关的老人信息，礼貌拒绝
- 不得讨论系统实现细节、数据库结构、管理员信息
```

### 意图识别（关键词匹配）

| 关键词 | 意图 | 查询数据 |
|--------|------|---------|
| 床位/空房/空闲/还有房吗 | BED | Bed 各状态统计 + RoomType 空闲房间 |
| 预约/参观/探访/预约 | RESERVATION | 该用户预约记录（家属仅本人） |
| 入住/办理/流程/手续 | CHECKIN | CheckInConfig 流程配置 |
| 护理/等级/服务内容 | NURSING | NursingLevel 分级+费用列表 |
| 价格/费用/月费/多少钱 | PRICE | RoomType 价格 + NursingLevel 费用 |
| 房间/套房/单间/双人间 | ROOM | RoomType 详情 + 空闲 Room |
| 老人/家人/妈妈/爸爸/爷爷/奶奶 | ELDER | Elder 信息（家属仅绑定的老人） |
| 健康/评估/体检 | HEALTH | HealthAssessment 状态说明 |

### 对话历史
最近 10 条消息（同一 session_id）作为上下文注入 API。

## 7. WebSocket 协议

```
Client → Server:
{
  "sessionId": 1,      // 会话ID (0=新建会话)
  "content": "...",     // 用户消息
  "action": "send"      // send|new
}

Server → Client:
{"type":"message",  "sessionId":1, "role":"assistant", "content":"...", "timestamp":"..."}
{"type":"typing",   "sessionId":1, "status":true}
{"type":"error",    "sessionId":1, "content":"错误描述"}
{"type":"session",  "sessionId":1}  // 新会话创建确认
```

## 8. 文件清单

### 新建文件 (26 个)

**SQL** — `sql/zzyl_chat.sql`

**后端 zzyl-common (1)** — `ai/DeepSeekAIService.java`

**后端 zzyl-nursing-platform (16)**:
- `domain/ChatSession.java`, `domain/ChatMessage.java`
- `mapper/ChatSessionMapper.java`, `mapper/ChatMessageMapper.java`
- `resources/mapper/nursing/ChatSessionMapper.xml`, `ChatMessageMapper.xml`
- `service/IChatSessionService.java`, `IChatMessageService.java`, `IChatAIService.java`, `INursingDataQueryService.java`
- `service/impl/ChatSessionServiceImpl.java`, `ChatMessageServiceImpl.java`, `ChatAIServiceImpl.java`, `NursingDataQueryServiceImpl.java`
- `config/ChatWebSocketServer.java`, `config/ChatAsyncProcessor.java`
- `controller/AdminChatController.java`, `controller/member/MemberChatController.java`
- `dto/ChatRequestDto.java`, `vo/ChatMessageVo.java`, `vo/ChatSessionVo.java`

**前端 zzyl/ruoyi-ui/src (9)**:
- `components/Chat/index.vue`, `ChatPanel.vue`, `ChatSessionList.vue`, `ChatMessageArea.vue`, `ChatBubble.vue`, `ChatInput.vue`
- `api/nursing/chat.js`, `utils/chatWebSocket.js`, `store/modules/chat.js`

### 修改文件 (3)
- `zzyl-admin/src/main/resources/application-dev.yml` — 新增 deepseek 配置段
- `zzyl-admin/src/main/resources/application-docker.yml` — 同上
- `zzyl/ruoyi-ui/src/layout/components/Navbar.vue` — 引入 ChatButton

## 9. 验证方案

1. 导入 SQL：`docker exec zzyl-mysql mysql -uroot -pheima123 zzyl < sql/zzyl_chat.sql`
2. WebSocket 连通：浏览器 DevTools → `new WebSocket('ws://localhost:9000/ws/chat/1/admin')` → onopen
3. 聊天功能：导航到问题咨询 → 发送"你好" → 收到 AI 回复
4. 数据查询：发送"还有空床位吗" → AI 回复含实际床位数据
5. 账户隔离：家属 A 请求家属 B 的老人信息 → 拒绝/返回空
6. 会话持久：刷新页面 → 历史会话和消息仍存在
7. 回滚验证：`git checkout master` → `docker compose down && docker compose up -d` → 原功能完整
8. Swagger 可见：`http://localhost:9000/swagger-ui/index.html`
