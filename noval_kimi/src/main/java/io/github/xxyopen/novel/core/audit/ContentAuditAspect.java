package io.github.xxyopen.novel.core.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xxyopen.novel.core.annotation.ContentAudit;
import io.github.xxyopen.novel.core.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.core.common.exception.BusinessException;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.dao.entity.AuditLog;
import io.github.xxyopen.novel.dao.mapper.AuditLogMapper;
import io.github.xxyopen.novel.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容审核 AOP 切面
 * 拦截标注了 @ContentAudit 的方法，进行敏感词过滤和内容安全检测
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ContentAuditAspect {

    private final SensitiveWordFilter sensitiveWordFilter;
    private final AuditLogMapper auditLogMapper;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(contentAudit)")
    public Object around(ProceedingJoinPoint point, ContentAudit contentAudit) throws Throwable {
        // 1. 解析待审核文本
        Object textObj = parseExpression(point, contentAudit.textExpr());
        String text = textObj != null ? textObj.toString() : null;
        if (text == null || text.isBlank()) {
            return point.proceed();
        }

        // 2. 解析业务ID
        Long bizId = null;
        if (!contentAudit.bizIdExpr().isEmpty()) {
            try {
                Object bizIdObj = parseExpression(point, contentAudit.bizIdExpr());
                if (bizIdObj != null) {
                    bizId = Long.valueOf(bizIdObj.toString());
                }
            } catch (Exception e) {
                log.warn("解析业务ID失败: {}", contentAudit.bizIdExpr());
            }
        }

        // 3. 创建审核日志
        AuditLog auditLog = new AuditLog();
        auditLog.setBizType(contentAudit.value().name());
        auditLog.setBizId(bizId != null ? bizId : 0L);
        auditLog.setContentType((byte) 1);
        auditLog.setContent(text);
        auditLog.setCreateTime(LocalDateTime.now());

        String localResult = "PASS";
        String localHits = null;
        String finalResult = "PASS";

        // 4. 本地敏感词过滤
        if (contentAudit.localCheck()) {
            SensitiveWordFilter.FilterResult filterResult = sensitiveWordFilter.match(text);
            List<SensitiveWordFilter.HitResult> hits = filterResult.getHits();
            if (!hits.isEmpty()) {
                localResult = "HIT";
                localHits = hits.stream()
                    .map(h -> h.getWord() + "(" + h.getCategory() + ")")
                    .collect(java.util.stream.Collectors.joining(", "));

                // 如果有 level >= 3 的高危敏感词，直接阻断
                boolean hasBlockLevel = hits.stream().anyMatch(h -> h.getLevel() >= 3);
                if (hasBlockLevel) {
                    finalResult = "BLOCK";
                }
            }
        }

        auditLog.setLocalResult(localResult);
        auditLog.setLocalHits(localHits);

        // 5. 云 API 审核（LLM 内容审核）
        if (contentAudit.cloudCheck()) {
            AiAuditResult aiResult = aiService.auditContent(text, contentAudit.value().name());
            auditLog.setCloudResult(aiResult.getResult());
            try {
                auditLog.setCloudDetail(objectMapper.writeValueAsString(aiResult));
            } catch (Exception e) {
                auditLog.setCloudDetail(aiResult.getReason());
            }
            // 综合判断最终结论
            if ("BLOCK".equals(finalResult) || aiResult.isBlock()) {
                finalResult = "BLOCK";
            } else if (aiResult.isReview()) {
                finalResult = "REVIEW";
            }
        }

        auditLog.setFinalResult(finalResult);
        auditLogMapper.insert(auditLog);

        // 6. 根据审核结果处理
        if ("BLOCK".equals(finalResult)) {
            throw new BusinessException(ErrorCodeEnum.USER_REQUEST_PARAM_ERROR);
        }

        // REVIEW 状态也允许通过，但标记为待复审
        return point.proceed();
    }

    private Object parseExpression(ProceedingJoinPoint point, String expr) {
        Object[] args = point.getArgs();
        String[] paramNames = discoverer.getParameterNames(
            ((org.aspectj.lang.reflect.MethodSignature) point.getSignature()).getMethod());

        StandardEvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        // 同时支持按索引访问
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
        }

        return parser.parseExpression(expr).getValue(context);
    }
}
