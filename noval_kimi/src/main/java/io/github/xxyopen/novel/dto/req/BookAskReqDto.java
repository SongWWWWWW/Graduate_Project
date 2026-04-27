package io.github.xxyopen.novel.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 小说内容问答请求 DTO
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Data
@Schema(description = "小说内容问答请求")
public class BookAskReqDto {

    @NotBlank(message = "问题不能为空")
    @Schema(description = "用户问题", requiredMode = Schema.RequiredMode.REQUIRED,
        example = "主角什么时候突破筑基期？")
    private String question;

    @Schema(description = "限定小说ID（为空则搜索全部）")
    private Long bookId;

    @Schema(description = "召回相关章节数", example = "5")
    private Integer topK = 5;
}
