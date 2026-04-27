package io.github.xxyopen.novel.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 语义搜索请求 DTO
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Data
@Schema(description = "语义搜索请求")
public class SemanticSearchReqDto {

    @NotBlank(message = "搜索内容不能为空")
    @Schema(description = "搜索描述语句", requiredMode = Schema.RequiredMode.REQUIRED,
        example = "主角逆天改命的修仙小说")
    private String query;

    @Schema(description = "作品方向过滤;0-男频 1-女频")
    private Integer workDirection;

    @Schema(description = "分类ID过滤")
    private Long categoryId;

    @Schema(description = "返回数量", example = "10")
    private Integer topK = 10;
}
