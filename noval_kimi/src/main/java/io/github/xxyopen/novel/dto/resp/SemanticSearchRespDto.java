package io.github.xxyopen.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 语义搜索结果 DTO
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Data
@Builder
@Schema(description = "语义搜索结果")
public class SemanticSearchRespDto {

    @Schema(description = "小说ID")
    private Long bookId;

    @Schema(description = "小说名")
    private String bookName;

    @Schema(description = "作者名")
    private String authorName;

    @Schema(description = "分类名")
    private String categoryName;

    @Schema(description = "小说简介")
    private String bookDesc;

    @Schema(description = "封面地址")
    private String picUrl;

    @Schema(description = "相似度得分")
    private Double score;

    @Schema(description = "相关章节内容摘要")
    private String relevantSnippet;
}
