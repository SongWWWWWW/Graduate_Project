package io.github.xxyopen.novel.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 对话请求 DTO
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Data
@Schema(description = "AI对话请求")
public class AiChatReqDto {

    @NotBlank(message = "提示词不能为空")
    @Schema(description = "用户输入的提示词", requiredMode = Schema.RequiredMode.REQUIRED)
    private String prompt;

    @Schema(description = "会话ID（为空则创建新会话）")
    private String sessionId;

    @Schema(description = "关联小说ID")
    private Long bookId;

    @Schema(description = "会话类型;0-通用 1-续写 2-大纲 3-润色")
    private Integer sessionType;

    @Schema(description = "角色设定ID")
    private Long characterId;

    @Schema(description = "前文内容（续写场景）")
    private String context;

    @Schema(description = "期望输出字数（续写/扩写场景）")
    private Integer targetLength;
}
