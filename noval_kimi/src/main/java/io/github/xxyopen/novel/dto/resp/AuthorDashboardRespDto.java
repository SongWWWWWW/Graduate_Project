package io.github.xxyopen.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 作者工作台响应DTO
 */
@Data
@Builder
public class AuthorDashboardRespDto {

    @Schema(description = "笔名")
    private String penName;

    @Schema(description = "作品数量")
    private Integer bookCount;

    @Schema(description = "总字数")
    private Integer totalWordCount;

    @Schema(description = "总点击量")
    private Long totalVisitCount;

    @Schema(description = "总评论数")
    private Integer totalCommentCount;

    @Schema(description = "最近作品列表")
    private List<RecentBook> recentBooks;

    @Data
    @Builder
    public static class RecentBook {
        @Schema(description = "小说ID")
        private Long id;

        @Schema(description = "小说名")
        private String bookName;

        @Schema(description = "封面")
        private String picUrl;

        @Schema(description = "字数")
        private Integer wordCount;

        @Schema(description = "点击量")
        private Long visitCount;

        @Schema(description = "最新章节名")
        private String lastChapterName;

        @Schema(description = "最新章节更新时间")
        private String lastChapterUpdateTime;
    }
}
