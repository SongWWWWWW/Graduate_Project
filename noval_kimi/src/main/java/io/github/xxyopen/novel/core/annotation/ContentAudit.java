package io.github.xxyopen.novel.core.annotation;

import java.lang.annotation.*;

/**
 * 内容审核注解
 * 标注在需要审核的方法上，通过 AOP 切面自动进行内容安全检测
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ContentAudit {

    /**
     * 业务类型
     */
    AuditBizType value();

    /**
     * SpEL 表达式，指定方法参数中哪个字段是待审核文本
     * 例如："#dto.chapterContent" 或 "#content"
     */
    String textExpr();

    /**
     * SpEL 表达式，指定业务ID字段
     * 例如："#dto.bookId" 或 "#id"
     */
    String bizIdExpr() default "";

    /**
     * 审核策略：是否启用本地敏感词过滤
     */
    boolean localCheck() default true;

    /**
     * 审核策略：是否启用云 API 审核
     */
    boolean cloudCheck() default true;

    /**
     * 业务类型枚举
     */
    enum AuditBizType {
        BOOK_CHAPTER,   // 小说章节
        COMMENT,        // 用户评论
        BOOK_INFO,      // 小说信息
        AI_OUTPUT       // AI 生成内容
    }
}
