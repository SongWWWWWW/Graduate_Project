-- ============================================================
-- AI 审核模块数据库表
-- 在 novel 库中执行
-- ============================================================

-- ----------------------------
-- 敏感词库表
-- ----------------------------
DROP TABLE IF EXISTS `audit_sensitive_word`;
CREATE TABLE `audit_sensitive_word` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `word` varchar(100) NOT NULL COMMENT '敏感词',
  `category` varchar(20) NOT NULL DEFAULT 'other' COMMENT '类别;porn-色情 politics-政治 violence-暴力 abuse-辱骂 ad-广告 other-其他',
  `level` tinyint(3) unsigned NOT NULL DEFAULT '1' COMMENT '等级;1-提示 2-警告 3-阻断',
  `is_enabled` tinyint(3) unsigned NOT NULL DEFAULT '1' COMMENT '是否启用;0-禁用 1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='敏感词库';

-- ----------------------------
-- 内容审核日志表
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_type` varchar(20) NOT NULL COMMENT '业务类型;BOOK_CHAPTER/COMMENT/BOOK_INFO/AI_OUTPUT',
  `biz_id` bigint(20) unsigned NOT NULL COMMENT '业务ID',
  `content_type` tinyint(3) unsigned NOT NULL DEFAULT '1' COMMENT '内容类型;1-文本 2-图片',
  `content` text COMMENT '审核内容（存储前N字）',
  `local_result` varchar(20) DEFAULT NULL COMMENT '本地审核结果;PASS/BLOCK/REVIEW',
  `local_hits` varchar(500) DEFAULT NULL COMMENT '本地命中词（JSON数组）',
  `cloud_result` varchar(20) DEFAULT NULL COMMENT '云审核结果;PASS/BLOCK/REVIEW',
  `cloud_detail` varchar(1000) DEFAULT NULL COMMENT '云审核详情（JSON）',
  `final_result` varchar(20) NOT NULL DEFAULT 'PASS' COMMENT '最终审核结果;PASS-通过 BLOCK-拒绝 REVIEW-人工复审',
  `status` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '处理状态;0-待处理 1-已处理',
  `operator_id` bigint(20) unsigned DEFAULT NULL COMMENT '处理人ID',
  `operator_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_status` (`status`),
  KEY `idx_result` (`final_result`),
  KEY `idx_createTime` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容审核日志';

-- ----------------------------
-- 初始化敏感词（示例）
-- ----------------------------
INSERT INTO `audit_sensitive_word` (`word`, `category`, `level`) VALUES
('色情', 'porn', 3),
('赌博', 'other', 3),
('毒品', 'other', 3),
('枪支', 'violence', 3),
('翻墙', 'politics', 2),
('vpn', 'politics', 2),
('台独', 'politics', 3),
('法轮功', 'politics', 3),
('傻逼', 'abuse', 2),
('他妈的', 'abuse', 2),
('加微信', 'ad', 1),
('扫码进群', 'ad', 1),
('www.', 'ad', 1),
('.com', 'ad', 1);
