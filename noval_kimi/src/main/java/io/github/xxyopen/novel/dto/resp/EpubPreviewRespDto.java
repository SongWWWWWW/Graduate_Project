package io.github.xxyopen.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * EPUB 文件预览响应 DTO
 */
@Data
public class EpubPreviewRespDto {

    @Schema(description = "书名")
    private String bookName;

    @Schema(description = "作者")
    private String author;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "总章节数")
    private Integer totalChapters;

    @Schema(description = "总字数")
    private Integer totalWords;

    @Schema(description = "章节列表")
    private List<EpubChapterRespDto> chapters;
}
