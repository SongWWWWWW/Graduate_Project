-- ============================================================
-- AI 模块数据库表
-- 在 novel 库中执行
-- ============================================================

-- ----------------------------
-- AI 对话会话表
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_session`;
CREATE TABLE `ai_chat_session` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id` varchar(64) NOT NULL COMMENT '会话唯一标识',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '用户ID',
  `book_id` bigint(20) unsigned DEFAULT NULL COMMENT '关联小说ID',
  `session_type` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '会话类型;0-通用 1-续写 2-大纲 3-润色',
  `title` varchar(100) DEFAULT NULL COMMENT '会话标题（自动提取或手动命名）',
  `model` varchar(50) DEFAULT NULL COMMENT '使用模型',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sessionId` (`session_id`),
  KEY `idx_userId` (`user_id`),
  KEY `idx_bookId` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI对话会话';

-- ----------------------------
-- AI 对话消息表（持久化记忆）
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `role` varchar(20) NOT NULL COMMENT '角色;system/user/assistant',
  `content` text NOT NULL COMMENT '消息内容',
  `token_count` int(10) unsigned DEFAULT '0' COMMENT 'Token 数量估算',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sessionId` (`session_id`),
  KEY `idx_createTime` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI对话消息';

-- ----------------------------
-- AI 角色/文风设定表
-- ----------------------------
DROP TABLE IF EXISTS `ai_character`;
CREATE TABLE `ai_character` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '用户ID',
  `book_id` bigint(20) unsigned DEFAULT NULL COMMENT '关联小说ID',
  `name` varchar(50) DEFAULT NULL COMMENT '设定名（如：热血玄幻风）',
  `persona` text COMMENT '角色人设/世界观设定',
  `style` varchar(500) DEFAULT NULL COMMENT '文风描述（如：语言热血，节奏明快，对话生动）',
  `is_default` tinyint(3) unsigned DEFAULT '0' COMMENT '是否默认设定;0-否 1-是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_userId_bookId` (`user_id`,`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI角色设定';

-- ----------------------------
-- 作家 AI 配额表（可选，也可直接扩展 author_info）
-- ----------------------------
DROP TABLE IF EXISTS `ai_author_quota`;
CREATE TABLE `ai_author_quota` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `author_id` bigint(20) unsigned NOT NULL COMMENT '作家ID',
  `quota_date` date NOT NULL COMMENT '配额日期',
  `daily_limit` int(10) unsigned NOT NULL DEFAULT '100' COMMENT '每日限额',
  `used_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '已使用次数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_authorId_date` (`author_id`,`quota_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI作者配额';
