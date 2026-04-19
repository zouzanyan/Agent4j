# AI Agent MVP 企业级智能对话系统

## 项目概述

基于 Java 17 + Spring Boot 3.x + LangChain4j + MyBatis 的企业级 AI 对话系统 MVP。

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.2.0
- **LangChain4j**: 0.25.0 (LLM调用框架)
- **MyBatis**: 3.0.3 (ORM框架)
- **MySQL**: 8.0+
- **构建工具**: Maven

## 项目结构

```
com.example.ai
├── AiAgentApplication.java      # 启动类
├── config
│   └── LangChain4jConfig.java   # LangChain4j配置
├── controller
│   ├── ChatController.java      # 聊天接口
│   ├── ConversationController.java  # 会话管理接口
│   ├── FeedbackController.java  # 反馈接口
│   └── GlobalExceptionHandler.java  # 全局异常处理
├── dto                          # 数据传输对象
├── entity                       # 实体类
├── mapper                       # MyBatis Mapper接口
└── service                      # 业务逻辑层
    └── impl                     # 实现类
```

## 数据库设计

### 表结构

1. **conversation** - 会话表
   - id: BIGINT (主键)
   - user_id: VARCHAR(64) (用户ID)
   - title: VARCHAR(255) (会话标题)
   - created_at: DATETIME

2. **message** - 消息表
   - id: BIGINT (主键)
   - conversation_id: BIGINT (外键)
   - role: VARCHAR(16) (user/assistant)
   - content: TEXT
   - created_at: DATETIME

3. **feedback** - 反馈表
   - id: BIGINT (主键)
   - message_id: BIGINT (外键)
   - type: VARCHAR(16) (like/dislike)
   - created_at: DATETIME

## 快速开始

### 1. 环境准备

- JDK 17+
- MySQL 8.0+
- Maven 3.8+

### 2. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source src/main/resources/schema.sql
```

### 3. 配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_agent
    username: root
    password: your_password

langchain4j:
  open-ai:
    api-key: your-openai-api-key
    model-name: gpt-3.5-turbo
```

### 4. 运行

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run
```

## API接口

### 1. 同步聊天

```http
POST /chat
Content-Type: application/json
X-User-Id: user123

{
    "conversationId": null,
    "message": "你好"
}
```

响应：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "conversationId": 1,
        "answer": "你好！我是企业AI助手..."
    }
}
```

### 2. 流式聊天（SSE）

```http
POST /chat/stream
Content-Type: application/json
X-User-Id: user123

{
    "conversationId": 1,
    "message": "请介绍一下Spring Boot"
}
```

SSE响应：
```
event: message
data: {"type":"token","content":"Spring","conversationId":1}

event: message
data: {"type":"token","content":" Boot","conversationId":1}

event: message
data: {"type":"complete","content":"Spring Boot是...","conversationId":1}
```

### 3. 查询会话列表

```http
GET /conversations
X-User-Id: user123
```

### 4. 查询会话详情

```http
GET /conversations/{id}
X-User-Id: user123
```

### 5. 提交反馈

```http
POST /feedback
Content-Type: application/json

{
    "messageId": 1,
    "type": "like"
}
```

## 核心特性

1. **多轮对话**: 支持上下文记忆，自动关联历史消息
2. **用户隔离**: 通过 X-User-Id 请求头实现数据隔离
3. **流式响应**: 使用 SSE 实现 token 级实时输出
4. **数据持久化**: 会话、消息、反馈全部持久化到 MySQL
5. **企业级架构**: 分层清晰，易于扩展

## 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| OPENAI_API_KEY | OpenAI API密钥 | - |
| OPENAI_MODEL_NAME | 模型名称 | gpt-3.5-turbo |
| OPENAI_BASE_URL | 自定义API地址 | - |

## 许可证

MIT



## 小记

type	含义	关键字段

reasoning	思维链片段	content
tool_call	工具调用	toolName + toolArgs
token	文本片段	content
complete	生成完成	content
error	出错	content

