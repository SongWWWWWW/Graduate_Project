package io.github.xxyopen.novel.core.audit;

import lombok.Data;

import java.util.List;

/**
 * LLM 内容审核结果
 */
@Data
public class AiAuditResult {

    /** 审核结果: PASS-通过 BLOCK-阻断 REVIEW-需人工复审 */
    private String result;

    /** 审核结论说明 */
    private String reason;

    /** 违规分类列表，如 ["色情", "暴力"] */
    private List<String> categories;

    public boolean isBlock() {
        return "BLOCK".equals(result);
    }

    public boolean isReview() {
        return "REVIEW".equals(result);
    }

    public boolean isPass() {
        return "PASS".equals(result);
    }
}
