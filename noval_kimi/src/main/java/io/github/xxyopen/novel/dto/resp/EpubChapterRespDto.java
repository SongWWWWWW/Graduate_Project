package io.github.xxyopen.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * EPUB 章节预览响应 DTO
 */
@Data
public class EpubChapterRespDto {

    @Schema(description = "章节序号")
    private Integer chapterNum;

    @Schema(description = "章节标题")
    private String chapterName;

    @Schema(description = "章节内容预览（前200字）")
    private String preview;

    @Schema(description = "章节字数")
    private Integer wordCount;
}
