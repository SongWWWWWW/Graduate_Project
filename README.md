# 小说阅读与创作平台（毕业设计）

基于 Spring Boot + Vue.js 的小说阅读与创作平台，包含作者管理系统、读者门户网站和后台管理系统三大模块。

---

## 项目结构

```
Graduate_Project/
├── noval_kimi/          # 后端服务（Spring Boot）
│   ├── src/main/java/.../controller/admin    # 后台管理接口
│   ├── src/main/java/.../controller/author   # 作者管理接口
│   ├── src/main/java/.../controller/front    # 读者门户接口
│   ├── doc/sql/                              # 数据库脚本
│   └── pom.xml
└── novel-front-web/     # 前端项目（Vue.js）
    ├── src/views/admin     # 后台管理页面
    ├── src/views/author    # 作者管理页面
    ├── src/views/          # 读者门户页面
    └── package.json
```

---

## 三大核心系统

### 1. 读者门户网站
- 小说分类浏览、排行榜、最新更新
- 章节在线阅读、书架收藏
- 用户注册/登录、评论互动
- AI 智能搜索与语义检索
- 支付充值、阅读记录

### 2. 作者管理系统
- 作者注册与实名认证
- 作品创建、章节发布与修改
- AI 智能写作助手
- 作品数据统计与收入查询
- 评论管理与读者互动

### 3. 后台管理系统
- 用户与作者管理
- 小说内容与章节审核
- 敏感词管理与内容安全审计
- 系统日志与运营数据统计
- 书籍批量导入

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot、MyBatis-Plus、MySQL、Redis、RabbitMQ、Elasticsearch、Milvus |
| 前端 | Vue 2.x、Element Plus、Axios |
| AI 能力 | 大语言模型（LLM）对话、向量检索、智能审核 |
| 部署 | Maven、Docker（可选） |

---

## 快速开始

### 后端启动
```bash
cd noval_kimi
# 配置数据库、Redis、MQ 等（application.yml）
mvn spring-boot:run
```

### 前端启动
```bash
cd novel-front-web
npm install
npm run serve
```

### 数据库初始化
执行 `noval_kimi/doc/sql/` 目录下的 SQL 脚本：
- `novel_struc.sql` — 表结构
- `novel_data.sql` — 基础数据（如有，需单独导入，文件较大）
- `ai_module.sql` — AI 模块表
- `audit_module.sql` — 审计模块表

---

## 开发环境

- JDK 17+
- Node.js 16+
- MySQL 8.0
- Redis 6+
- RabbitMQ 3.9+
- Elasticsearch 7.x（可选）

---

## 联系方式

如有问题请联系项目作者。
