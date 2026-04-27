package io.github.xxyopen.novel.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作者作品统计响应DTO
 */
@Data
@Builder
public class AuthorBookStatsRespDto {

    @Schema(description = "小说ID")
    private Long id;

    @Schema(description = "小说名")
    private String bookName;

    @Schema(description = "封面")
    private String picUrl;

    @Schema(description = "类别名")
    private String categoryName;

    @Schema(description = "字数")
    private Integer wordCount;

    @Schema(description = "点击量")
    private Long visitCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "书籍状态;0-连载中 1-已完结")
    private Integer bookStatus;

    @Schema(description = "最新章节名")
    private String lastChapterName;

    @Schema(description = "最新章节更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastChapterUpdateTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
